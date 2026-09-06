# Data Dictionary

> Exact meaning of every field that is ambiguous on its own or carries a
> business rule. Purely self-explanatory fields (like a table's own `id`)
> are not repeated here — see `models.md` for the full column list per table.

| Field | Service | Table | Type | Detailed description | Possible values |
|-------|---------|-------|------|----------------------|------------------|
| role | auth | users | VARCHAR(20) | The single role a system user has. Drives every route guard and permission check across all 4 services (`security-policy.md`). A user has exactly one role, never more than one. | `ADMIN`, `SALESPERSON`, `INVENTORY` |
| active | auth, customers, products, sales (all tables) | all | BOOLEAN | Soft-delete flag, per ADR-001 §7. `false` means the row is logically deleted — it must be excluded from normal reads but never physically removed, to preserve traceability (RNF-04). Meaning shifts slightly per entity: for `users`/`customers`, `false` also blocks new sales (see `sales.customer_id`); for `products`, `false` blocks new sale lines. | `true`, `false` |
| token | auth | refresh_tokens | VARCHAR(512) | The refresh token value issued to a client session. One-time use — rotated on every refresh per `security-policy.md`. | Any opaque token string |
| identity_document | customers | customers | VARCHAR(30) | Cédula or NIT. The real-world lookup key sales staff use at the point of sale — not the internal `customer_id`. Unique across all customers, active or not. | Any valid Colombian ID/NIT format |
| price | products | products | NUMERIC(12,2) | Current unit price. Not historical — see `sale_details.unit_price` for the frozen price at the moment of a specific sale. | `> 0` |
| stock | products | products | INTEGER | Units currently available. Decremented automatically when a sale is confirmed (RF-07); never allowed to go negative. | `>= 0` |
| customer_id | sales | sales | UUID | **Looks like a foreign key but is not one.** Points to `customers.customer_id` in a different schema/service. Validated via a synchronous HTTP call to `customers-service` at sale creation — the database itself does not enforce this relationship. | Must be an existing, `active = true` customer |
| product_id | sales | sale_details | UUID | Same pattern as `sales.customer_id`: an external reference to `products.product_id`, validated over HTTP, not a DB-level FK. | Must be an existing, `active = true` product |
| unit_price | sales | sale_details | NUMERIC(12,2) | **Frozen at the moment of sale.** Copied from the product's `price` when the line is created and never updated again — even if the product's price changes afterward. This is the field most likely to be misread as "the product's current price"; it is not. | `> 0` |
| subtotal | sales | sale_details | NUMERIC(14,2) | Always equal to `quantity * unit_price`, enforced by a DB `CHECK` constraint — never computed or overridden by application code independently of that formula. | `= quantity * unit_price` |
| total | sales | sales | NUMERIC(14,2) | Sum of all `sale_details.subtotal` rows for that sale. | `>= 0` |
| date | sales | sales | TIMESTAMPTZ | When the sale was created. Named `date`, not `created_at`, per ADR-001's own field naming. | Any valid timestamp |
| daily_sales_total, monthly_sales_total, quantity_sold, product_id | sales | sales_summary | NUMERIC / INTEGER / UUID | **Not populated by any process yet.** These columns exist because ADR-001 names them, but no job or trigger currently writes to this table — reporting screens are still pending scope (see `navigation-map.md`). Do not assume this table has data. | — |

---

## Correlations

- Full column list and constraints per table → `06-data/models.md`
- Domain invariants these fields enforce → `02-domain/entities-and-rules.md`
- Role permissions that `users.role` drives → `00-governance/security-policy.md`
