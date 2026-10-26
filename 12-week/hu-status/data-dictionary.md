# Data Dictionary

> Exact meaning of every field that is ambiguous on its own or carries a
> business rule. Purely self-explanatory fields (like a table's own `id`)
> are not repeated here — see `models.md` for the full column list per table.

| Field | Service | Table / collection | Type | Detailed description | Possible values |
|-------|---------|-------|------|----------------------|------------------|
| role | auth | system_user | text + `CHECK` | The single role a system user has. Drives every route guard and permission check (`security-policy.md`). A user has exactly one role, never more than one. `SERVICE` exists only inside service tokens and is never stored here (ADR-006). | `ADMIN`, `SALESPERSON`, `INVENTORY` |
| active | every domain | every table with a lifecycle | boolean | Soft-delete flag, per ADR-001 §7. The service's database role has no `DELETE` permission, so the database itself enforces it. `false` means the row is logically deleted — it must be excluded from normal reads but never physically removed, to preserve traceability (NFR-009). Meaning shifts slightly per entity: for `system_user`, `false` blocks login; for `customer`, `false` blocks new sales; for `product`, `false` blocks new reservations. In Sales the flag is a field of the `sale` document and of each element of `details`; the role `sales_writer` has no action to remove documents. | `true`, `false` |
| token | auth | refresh_token | text | **A hash** of the refresh token issued to a client session, never the token itself. One-time use — rotated on every refresh per `security-policy.md`. | Hash string, unique |
| token_id | auth | service_token | uuid | The `jti` of a service token, and the id used by `GET /api/v1/auth/service-tokens/{id}`. The table keeps only the metadata of the token; the signed token is never stored (ADR-006). | Unique |
| identity_document | customers | customer | text, 1–30 characters | Cédula or NIT. The real-world lookup key sales staff use at the point of sale — not the internal `customer_id`. Unique across all customers, active or not. | Any valid Colombian ID/NIT format |
| key | auth, customers | idempotency_key | text, 8–128 characters | The client creation key, written atomically with its resource. Physical column name in these two schemas; the same header uses different physical names in Products, Workflow and Sales. | Unique per owning table |
| idempotency_key | products | idempotency_key | text, 8–128 characters | Actual Products column name in V008; resource_type/resource_id identify the created resource. It is not named key in this repository. | Unique per owning table |
| idempotency_key | workflow | saga_instance | text, 8–128 characters | Creation key stored on the saga, without another key table. The unique constraint covers saga_type and idempotency_key. Caller/input replay rules remain subject to the saga decision boundary. | Unique per saga_type/key pair |
| price_cents | products | product | bigint | Current unit price, in minor units (1/100 COP). Not historical — see `stock_reservation_line.unit_price_cents` for the price frozen in a sale. | `> 0` |
| stock | products | product | integer | Units currently available. Changes only through a stock reservation or a stock adjustment, never when a product is edited; never negative. | `>= 0` |
| stock_after | products | stock_adjustment | nullable integer | Immutable resulting stock recorded atomically by V017-and-later writes; maps to response currentStock. NULL means no historical receipt exists; legacy replay currently falls back to product stock at replay time, without reconstructing history. | NULL for legacy rows, otherwise >= 0 |
| customerId | sales | sale | string (UUID) | **Looks like a reference the engine checks, but it is not.** Points to a customer in another domain. The saga checks that it exists and is active (step 1) before the sale is written. | An existing, `active = true` customer |
| createdBy | sales | sale | string (UUID) | **Salesperson who registered the sale.** External reference to `system_user.user_id`. Sent by the saga from the salesperson's validated token and accepted only from a caller holding `sales:register` (ADR-002, ADR-006). Enables the SALESPERSON "own sales" permission and resolves STRIDE threats R-3 and I-4. | A valid `user_id` |
| date | sales | sale | date (UTC) | When the sale was created. Named `date`, not `createdAt`, per ADR-001's field naming. | Any valid timestamp |
| totalCents | sales | sale | long | Sum of the `subtotalCents` of the sale's lines, enforced by the domain when the sale is registered. | `>= 0` |
| idempotencyKey | sales | sale | string, 8–128 characters | The `Idempotency-Key` the saga sent (`<sagaId>:register-sale`). Unique across sales; written in the same atomic write as the sale (ADR-010). | Unique |
| details | sales | sale | array of 1 to 100 embedded lines | The lines of the sale. They live inside the sale document, not in a collection of their own. | 1 to 100 elements |
| details[].productId | sales | sale | string (UUID) | Same pattern as `customerId`: an external reference, reserved by the saga (step 2) before the sale is written. | A product reserved for this sale |
| details[].unitPriceCents | sales | sale | long | **Frozen when the stock is reserved.** Copied from the reservation line into the sale. Never the product's current price. | `>= 1` |
| details[].subtotalCents | sales | sale | long | Always equal to `quantity * unitPriceCents`, enforced by the domain; the validator checks only its type and range. | `= quantity * unitPriceCents` |
| unit_price_cents | products | stock_reservation_line | bigint | **Frozen when the stock is reserved.** Copied from `product.price_cents` into the reservation line; the saga copies it from there into the sale (`details[].unitPriceCents`). Never the product's current price. | `> 0` |
| delta | products | stock_adjustment | integer | Units added (positive) or removed (negative) by a manual correction. Never edited; a mistake is corrected with a new adjustment. | `<> 0` |
| status | products | stock_reservation | text + `CHECK` | `RESERVED` while the stock is set aside for a sale; `RELEASED` once given back by a compensation. Final once released. | `RESERVED`, `RELEASED` |
| status | products | stock_alert | text + `CHECK` | `OPEN` while the product is at or below the worker's threshold; `RESOLVED` when it recovers. At most one `OPEN` per product. | `OPEN`, `RESOLVED` |
| resource_type | products | idempotency_key | text + `CHECK` | Which kind of resource the key created, because this domain creates several. | `PRODUCT`, `CATEGORY`, `STOCK_ADJUSTMENT`, `STOCK_RESERVATION`, `STOCK_ALERT` |
| status | workflow | saga_instance | text + `CHECK` | Public/physical status remains unchanged. RUNNING may mean executing, compensating (failed_step set), or privately suspended with OUTCOME_UNRESOLVED. Suspension is not a fifth status or evidence that no sale exists. Decision reconciliation is pending under ARQ-28. | `RUNNING`, `COMPLETED`, `COMPENSATED`, `FAILED` |
| step_results.execution | workflow | saga_instance | jsonb object | Private correlationId, pendingStep and durable attempts; persisted before effects and excluded from the public response. | Internal only |
| error_detail | workflow | saga_instance | nullable text | Internal failure evidence, including OUTCOME_UNRESOLVED; never a public status or response field. | Internal only |
| failed_step | workflow | saga_instance | text | The step that failed, returned to the client as `failedStep`. Internal error details stay in `error_detail` and are never returned. | `validate-customer`, `reserve-stock`, `register-sale`, or empty |

---

## Auth refresh credential semantics

| Field | Table | Meaning |
|---|---|---|
| token_id | refresh_token | Internal row primary key; do not conflate its meaning with the service_token column that represents a service JWT's jti. |
| expiration_date | refresh_token | Expiration timestamp checked together with lifecycle state; an active flag cannot make an expired credential valid. |
| active | refresh_token | Invalidation state for rotation/logout and security handling; false does not mean physical removal or exclusion from security lookups needed to detect reuse. |

`token` retains the hash-only meaning in the main dictionary. Types, defaults and constraints remain in [models.md](models.md#domain-auth); no new column or hashing algorithm is selected here.

## Auth user-creation evidence (ADR-014)

| Field | Table | Meaning / allowed values |
|---|---|---|
| operation | user_creation_audit | USER_REGISTERED for ordinary API creation; INITIAL_ADMIN_CREATED for controlled initialization |
| actor_kind | user_creation_audit | ADMIN refers to the trusted application caller; OPERATOR refers to the controlled deployment identity, not an application user |
| actor_user_id | user_creation_audit | ADMIN's user_id at creation time; null for initialization; no inference from the actor's current role |
| operator_identity | user_creation_audit | Deployment-session identity for initialization; null for ordinary registration; no secrets or free-form payload |
| created_user_id | user_creation_audit | Created user; unique attribution record per new user; no new User authorship column |
| outcome | user_creation_audit | SUCCESS only; failures and no-op runs are operational/security logs |
| occurred_at | user_creation_audit | Evidence creation timestamp; UTC presentation |
| correlation_id | user_creation_audit | Connects the successful creation with HTTP logs or the controlled CLI execution |

Types, constraints, permissions and the initialization lock-key reservation have
one source of truth: `06-data/models.md`. The table is append-only evidence, not
a business aggregate, initialization marker or message-broker event.

## Representation and query boundaries

- Sale and embedded detail active flags remain required by storage/API. The domain/decoder cannot faithfully restore inactive records yet; [TD-012](../15-project-control/technical-backlog.md#td-012--sale-and-detail-lifecycle-restoration) owns resolution without silently adding domain state.
- New adjustment currentStock means recorded stock_after, not the product's latest stock. The legacy NULL fallback is explicitly limited.
- Report filters use inclusive UTC calendar dates translated to an exclusive next-midnight upper bound; ownership derives from verified token subject. Daily/monthly sum active Sale totals; top-products also filters active embedded details.
- Amounts and frozen prices are integer minor units. MongoDB long storage types and domain arithmetic/range checks are separate guarantees: a validator does not prove subtotal/total arithmetic consistency.
- Advisory lock ownership and overloads are registered once in [models.md](models.md#reserved-advisory-lock-keys); shared schemas do not create separate lock spaces.

## Correlations

- Full column list and constraints per table → `06-data/models.md`
- Domain invariants these fields enforce → `02-domain/entities-and-rules.md`
- Role permissions that `system_user.role` drives → `00-governance/security-policy.md`
- Database per domain, minor units and idempotency keys → ADR-005; Sales' document model → ADR-010
- Stock reservations, stock alerts and the saga store → ADR-007
- ADR-002 (added `created_by`) → `05-architecture/decisions/records/ADR-002-sale-authorship-traceability.md`
