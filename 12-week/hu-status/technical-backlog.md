# Technical Backlog

> Technical debt and deferred improvements are listed separately from unresolved
> delivery defects and decision gaps. Deferred improvements record the accepted
> cost and trigger; an open discrepancy names its owner and required closing
> evidence. Listing a defect here does not accept it, defer required MVP behavior,
> create a new HU or establish that the affected implementation is correct.

---

## Active items

### TD-001 — Daily sales closing job in the worker

| Field | Value |
|-------|-------|
| **ID** | TD-001 |
| **Description** | Add a second scheduled job to `synkro-worker` that produces a frozen daily snapshot of sales totals. Currently, reports aggregate the `sale` collection live (ADR-002, ADR-010 Decision 4); a closing would write a daily summary document that reports can read instead of re-aggregating |
| **Reference** | ADR-007 Decision 6, Option C (evaluated and deferred) |
| **Impact if not resolved** | Reports always query live data, which is acceptable at current volume but may become slow as sales grow |
| **Trigger to pick up** | PO decision, or report response time exceeds the 5-second statement timeout (`cross-cutting.md` §8) |
| **Estimated effort** | 3 SP |
| **Priority** | Low |
| **Sprint** | Post-MVP |

---

### TD-002 — Message broker and outbox pattern

| Field | Value |
|-------|-------|
| **ID** | TD-002 |
| **Description** | Adopt RabbitMQ (or equivalent) and apply the outbox pattern to the publishing service when an event gets a consumer that a requirement needs. Currently, no event is published in the MVP — consistency is handled by the saga only |
| **Reference** | ADR-007 Decision 5 (deferred), `pattern-guide.md` "Outbox Pattern: Deferred" |
| **Impact if not resolved** | Other contexts cannot react to business events (e.g. email on sale, analytics). Not a problem until a requirement asks for it |
| **Trigger to pick up** | A requirement appears that needs a context to react to a business event from another context |
| **Estimated effort** | 8 SP |
| **Priority** | Low |
| **Sprint** | Post-MVP |

---

### TD-003 — Per-product stock threshold

| Field | Value |
|-------|-------|
| **ID** | TD-003 |
| **Description** | Replace the global `LOW_STOCK_THRESHOLD` (default 5) with a per-product threshold stored in the `product` table. Currently a laptop and a cable trigger a low-stock alert at the same level |
| **Reference** | ADR-007 Decision 6, accepted cost |
| **Impact if not resolved** | False positives for high-volume products, missed alerts for low-volume ones |
| **Trigger to pick up** | The product catalog grows beyond a few dozen items, or the PO reports noise from irrelevant alerts |
| **Estimated effort** | 2 SP |
| **Priority** | Medium |
| **Sprint** | Post-MVP |

---

### TD-004 — JWKS endpoint for key rotation without redeployment

| Field | Value |
|-------|-------|
| **ID** | TD-004 |
| **Description** | Add a `GET /api/v1/auth/.well-known/jwks.json` endpoint to `synkro-auth-api` (RFC 7517) so that validating services can fetch the public key at startup or on a schedule, instead of receiving it as an environment variable. This enables key rotation without redeploying every service |
| **Reference** | ADR-006 (chose environment variable over JWKS); cross-cutting.md §5 "Future candidate" |
| **Impact if not resolved** | Key rotation requires redeploying every validating service with the new `JWT_PUBLIC_KEY`. Acceptable for the MVP (no real end users, frequent deployments) |
| **Trigger to pick up** | The system moves to a production-like environment with real users, or the team adopts automated deployments |
| **Estimated effort** | 3 SP |
| **Priority** | Medium |
| **Sprint** | Post-MVP |

---

### TD-006 — Operational monitoring for the shared instance and the MongoDB instance

| Field | Value |
|-------|-------|
| **ID** | TD-006 |
| **Description** | AT-002 (reopened in ADR-009) accepts the shared PostgreSQL instance as a single point of failure with mitigations: connection-pool sizing, CI GRANT verification, and operational monitoring. ADR-010 later created a second, equally single-instance point of failure for Sales on MongoDB (`risks.md` R-007). The monitoring part — Prometheus alerts for connection saturation, query duration and instance health, on both engines — is designed (`deployment.md` §8) but not yet configured |
| **Reference** | ADR-009 "What must be watched", ADR-010 "Accepted cost", `overview.md` AT-002, `risks.md` R-001 and R-007 |
| **Impact if not resolved** | A slow query, connection leak, or Mongo instance issue would be detected only when a service starts returning 503, not proactively |
| **Trigger to pick up** | When `synkro-infra-postgres` and `synkro-infra-mongo` are built and Prometheus/Grafana are running |
| **Estimated effort** | 3 SP |
| **Priority** | Medium |
| **Sprint** | First implementation sprint of `synkro-infra-postgres` / `synkro-infra-mongo` |

---

### TD-007 — Confirm ADR-001 §1 is the section the frontend-framework register cell points to

| Field | Value |
|-------|-------|
| **ID** | TD-007 |
| **Description** | `05-architecture/decisions/README.md`'s register row for ADR-001 cites `§1` as the section ADR-008 and ADR-011 modify for the frontend framework decision. That number was inferred by elimination (every other section, §2 through §8, is independently identified by other rows) while fixing an earlier unresolved placeholder the instructor's bot flagged — it was never confirmed against ADR-001's literal heading text |
| **Reference** | HU-ARQ-26's PR discussion; `05-architecture/decisions/README.md` |
| **Impact if not resolved** | A low but nonzero chance the register cites the wrong section number in an otherwise-authoritative document |
| **Trigger to pick up** | Immediately — this is a two-minute check, not a scheduled item; whoever opens `ADR-001-architecture.md` next should confirm it in passing |
| **Estimated effort** | 1 SP |
| **Priority** | Low (quick to resolve, low impact, but easy to forget precisely because it's quick) |
| **Sprint** | Whenever ADR-001 is next opened for any reason |

---

### TD-008 — Ratify or revise the frontend test coverage thresholds

| Field | Value |
|-------|-------|
| **ID** | TD-008 |
| **Description** | `11-quality/testing-strategy.md`'s "Frontend tests" section sets coverage thresholds (85% for the host's client/session, 70% for portal components) as proposed defaults, lower than the backend's 80-90% — there was no existing frontend test suite to calibrate against when they were written, only a judgment call about UI code's different risk profile |
| **Reference** | `11-quality/testing-strategy.md`, "Frontend coverage thresholds" |
| **Impact if not resolved** | Minor — the thresholds are enforced in CI (`testing-strategy.md`'s frontend pipeline) whether or not the team has explicitly discussed them, so nothing breaks; the risk is only that an unreviewed number quietly becomes the permanent bar |
| **Trigger to pick up** | After the first portal (`HU-PRO-01`/`HU-PRO-02`) has a real test suite — revisit with actual numbers in hand instead of a guess |
| **Estimated effort** | 1 SP (a team discussion, not a code change) |
| **Priority** | Low |
| **Sprint** | After Cut 6's first frontend stories close |

---

### TD-009 — Recovery when no usable application administrator remains

| Field | Value |
|---|---|
| **ID** | TD-009 |
| **Description** | Decide and review recovery after all ADMIN accounts become unusable through deactivation, role changes or exceptional removal. ADR-014 initialization is not the authorized recovery path. |
| **Reference** | ADR-014 scope boundary; initial-administrator runbook |
| **Impact if not resolved** | Administrative user creation is unavailable; the initialization existence check has no lifetime marker and cannot infer historical ADMIN roles. |
| **Trigger to pick up** | Before an environment with real users needs administrative recovery, or immediately if every usable ADMIN is lost. |
| **Priority** | High before real operation |
| **Status** | Pending decision; no recovery procedure or implementation claimed |

The ADR-014 downstream design documentation was merged through
[PR #220](https://github.com/code-corhuila/synkro-docs/pull/220), which closed
[HU-ARQ-27 — #219](https://github.com/code-corhuila/synkro-docs/issues/219).
[HU-DOCS-91 — #221](https://github.com/code-corhuila/synkro-docs/issues/221)
reconciles the remaining catalog and tracking records; that design alignment is
not deferred. TD-009 remains pending and outside HU-DOCS-91. Neither documentation
story supplies recovery implementation, schema migrations or executed Auth tests.

---

### TD-010 — Formalize the browser renewal policy

| Field | Value |
|---|---|
| **ID** | TD-010 |
| **Description** | Ratify or revise the existing proposal for host-managed proactive renewal, its margin and coordination; create a standalone ADR when new record files are in scope. No ADR identifier is assigned by HU-DOCS-93. |
| **Reference** | [Renewal policy proposal](../07-api/authentication.md#renewal-policy-proposal-and-decision-status); HU-DOCS-93 uses existing Markdown files only. |
| **Impact if not resolved** | Developers may mistake the merged proposal for an accepted architectural decision. |
| **Trigger to pick up** | Before relying on this policy as accepted architecture for production implementation. |
| **Owner / priority** | Development team / High before implementation |
| **Status** | Pending architectural formalization and team ratification; documentation proposal recorded, not accepted |

---

### TD-011 — Clarify reuse detection and stateless JWT revocation

| Field | Value |
|---|---|
| **ID** | TD-011 |
| **Description** | Reconcile the broad requirement to invalidate all active tokens after refresh reuse with local stateless JWT validation and the logout contract's explicitly non-revocable access token. Define the affected token classes and enforcement boundary without inventing a denylist, token family or service lookup. |
| **Reference** | [Refresh flow and revocation boundary](../07-api/authentication.md#refresh-token-flow), security policy, Auth logout/refresh contract. |
| **Impact if not resolved** | Tests or implementations may promise immediate rejection of an already-issued JWT without an adopted mechanism. |
| **Trigger to pick up** | Before implementation tests assert broad revocation effects. |
| **Owner / priority** | Development team / High before security implementation |
| **Status** | Pending scope decision; no new revocation mechanism or schema claimed |

---

## Unresolved delivery discrepancies

> Reconciled in Week 12 by HU-DOCS-95. These seven items remain open and are
> separate from accepted deferred improvements. Proposed implementation follow-up
> names are coordination proposals; no new GitHub issue or HU identifier is created.

### TD-012 — sale-and-detail-lifecycle-restoration

| Field | Value |
|---|---|
| **ID** | TD-012 |
| **Type / status** | Open delivery discrepancy; not accepted deferred work |
| **Description** | The storage validator and public SaleDetailResponse require active, but the domain lacks an independently supported detail lifecycle and decodeSale refuses inactive root sales/details. New details are encoded true. Reports filter inactive data through a different read path; that is not faithful aggregate restoration. |
| **Owner** | SergioAndres17 coordinates DOCS-95; proposed Sales implementation follow-up: JordanRG420, with domain/PO review. |
| **Reference / observed source** | [internal/adapter/out/persistence/sale_document.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/out/persistence/sale_document.go); [internal/adapter/out/persistence/reports.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/out/persistence/reports.go) |
| **Closing evidence** | A reviewed domain/lifecycle resolution plus regression evidence for inactive roots/details, mixed active states, frozen totals/prices, history and replay. Preserve stored facts; do not silently delete the flag, coerce true, discard lines or invent a domain attribute solely from storage. |
| **Priority / timing** | Resolve or explicitly review before claiming the affected acceptance/guarantee; planning of implementation belongs to the responsible team |

---

### TD-013 — publish-the-saga-safety-decision

| Field | Value |
|---|---|
| **ID** | TD-013 |
| **Type / status** | Open decision dependency; not accepted deferred work |
| **Description** | Delivered caller/input replay and evidence-based rejection/recovery differ from ADR-007 Decision 2's blanket business-4xx compensation wording. No modifying ADR is accepted in the inspected register. |
| **Owner** | AsolanoT owns HU-ARQ-28/#254; SergioAndres17 coordinates dependent contracts and JordanRG420 reviews. |
| **Reference / observed source** | [workflow-core/src/main/java/co/edu/corhuila/synkro/workflow/application/usecase/RegisterSale.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-core/src/main/java/co/edu/corhuila/synkro/workflow/application/usecase/RegisterSale.java); [workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/out/http/HttpSaleParticipants.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/out/http/HttpSaleParticipants.java); [ARQ-28](https://github.com/code-corhuila/synkro-docs/issues/254) |
| **Closing evidence** | An accepted modifying record (next ADR-015), exact replaced sections and reciprocal register references, then aligned reviewed downstream contracts. Preserve ADR-007 unchanged. OpenAPI syntax validation or code closure is not acceptance. |
| **Priority / timing** | Resolve or explicitly review before claiming the affected acceptance/guarantee; planning of implementation belongs to the responsible team |

---

### TD-014 — auth-and-workflow-two-key-lock-overlap

| Field | Value |
|---|---|
| **ID** | TD-014 |
| **Type / status** | Open delivery discrepancy; not accepted deferred work |
| **Description** | Workflow derives an unrestricted two-int advisory pair from each saga UUID; Auth refresh uses the same two-int key space with first key 0x52544F4B. They are distinct from Auth initialization's bigint overload but not demonstrably isolated from each other. Potential collisions serialize/block unrelated work. |
| **Owner** | AsolanoT coordinates Workflow/ARQ-28; proposed Auth follow-up: JordanRG420; SergioAndres17 owns the registry documentation. |
| **Reference / observed source** | [workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/out/persistence/JdbcSagaStore.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/out/persistence/JdbcSagaStore.java); [auth-adapters/src/main/java/co/edu/corhuila/synkro/auth/adapter/out/persistence/jdbc/JdbcRefreshTokenStore.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-adapters/src/main/java/co/edu/corhuila/synkro/auth/adapter/out/persistence/jdbc/JdbcRefreshTokenStore.java) |
| **Closing evidence** | Explicit reviewed cross-service allocation/coexistence decision and deterministic overlapping-key/concurrent-session evidence. Verify lease ownership and connection-loss behavior without weakening Auth refresh serialization; no fictitious isolated namespace. |
| **Priority / timing** | Resolve or explicitly review before claiming the affected acceptance/guarantee; planning of implementation belongs to the responsible team |

---

### TD-015 — designed-circuit-breaker-is-not-implemented

| Field | Value |
|---|---|
| **ID** | TD-015 |
| **Type / status** | Open delivery discrepancy; not accepted deferred work |
| **Description** | pattern-guide.md adopts per-participant Circuit Breakers and describes CLOSED/OPEN/HALF-OPEN, but the inspected Workflow HTTP path/composition establishes bounded timeouts/retries, not that state machine. |
| **Owner** | AsolanoT coordinates the Workflow implementation/design reconciliation; SergioAndres17 maintains the pattern and contract references. |
| **Reference / observed source** | [workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/out/http/HttpSaleParticipants.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/out/http/HttpSaleParticipants.java); [workflow-app/src/main/java/co/edu/corhuila/synkro/workflow/app/WorkflowComposition.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-app/src/main/java/co/edu/corhuila/synkro/workflow/app/WorkflowComposition.java) |
| **Closing evidence** | Implemented state-transition/fail-fast/probe tests per participant, or an explicitly reviewed design change with consequences and downstream updates. Timeout/rate-limit tests alone do not demonstrate a Circuit Breaker. |
| **Priority / timing** | Resolve or explicitly review before claiming the affected acceptance/guarantee; planning of implementation belongs to the responsible team |

---

### TD-016 — retry-classification-and-jitter-differ-from-policy

| Field | Value |
|---|---|
| **ID** | TD-016 |
| **Type / status** | Open delivery discrepancy; not accepted deferred work |
| **Description** | Observed Workflow retries HTTP 408 in addition to the approved network/429/5xx categories. Its delay is bound + random(0..bound/2), where bound=min(2000,100<<(attempt-1)); that is not full jitter from zero to the exponential bound. These are recorded deviations, not newly adopted retry policy. |
| **Owner** | AsolanoT coordinates the correction and HU-ARQ-28 decision; SergioAndres17 reviews the documented classification. |
| **Reference / observed source** | [workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/out/http/HttpSaleParticipants.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/out/http/HttpSaleParticipants.java); [workflow-app/src/main/java/co/edu/corhuila/synkro/workflow/app/WorkflowComposition.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-app/src/main/java/co/edu/corhuila/synkro/workflow/app/WorkflowComposition.java); accepted ADR-007 Decision 2 |
| **Closing evidence** | Align retry status classification and full-jitter behavior with the approved policy and test explicit nonretry cases, delays, durable budgets and restart. Any requested additional retry category requires explicit valid clarification before adoption; do not edit ADR-007 or silently accept the discrepancy. |
| **Priority / timing** | Resolve or explicitly review before claiming the affected acceptance/guarantee; planning of implementation belongs to the responsible team |

---

### TD-017 — products-health-does-not-check-its-store

| Field | Value |
|---|---|
| **ID** | TD-017 |
| **Type / status** | Open delivery discrepancy; not accepted deferred work |
| **Description** | The public Products /health handler returns 200 liveness without consulting the store. The required own-store health check and 503-on-store-failure behavior are therefore not delivered, although their response shape is declared in the contract. |
| **Owner** | Proposed Products implementation follow-up: JordanRG420; SergioAndres17 coordinates DOCS-95 contract/data reconciliation. |
| **Reference / observed source** | [internal/adapter/in/httpapi/handler.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/adapter/in/httpapi/handler.go); [Cross-cutting health requirement](../05-architecture/cross-cutting.md#4-health-check-endpoints) |
| **Closing evidence** | Bounded owning-store probe behind an application port, real available/unavailable-store tests proving 200/503 and the documented safe HealthResponse, public unversioned URL and no secret leakage. A router-only GET /health test does not close this gap. |
| **Priority / timing** | Resolve or explicitly review before claiming the affected acceptance/guarantee; planning of implementation belongs to the responsible team |

---


### TD-018 — products-physical-schema-conventions

| Field | Value |
|---|---|
| **ID** | TD-018 |
| **Type / status** | Open delivery discrepancy; not accepted deferred work |
| **Description** | Products migrations use ck_ rather than the documented chk_ names, inline UUID PRIMARY KEY declarations with generated names, and foreign keys with implicit NO ACTION rather than explicit RESTRICT. They create products_writer but not the designed products_reader. Earlier model examples also claimed name-length checks, a reservation/product unique constraint and product name/stock indexes that are absent from the inspected migrations. Correcting the documentation does not implement or waive these design differences. |
| **Owner** | SergioAndres17 coordinates the model reconciliation; proposed Products database follow-up: JordanRG420, with domain/architecture review. |
| **Reference / observed source** | [01_ddl/03_tables/V004__create_category.sql](https://github.com/code-corhuila/synkro-products-db/blob/043e485d16018fc4fb4cd60694c2a521c143ddbd/01_ddl/03_tables/V004__create_category.sql); [01_ddl/03_tables/V005__create_product.sql](https://github.com/code-corhuila/synkro-products-db/blob/043e485d16018fc4fb4cd60694c2a521c143ddbd/01_ddl/03_tables/V005__create_product.sql); [01_ddl/03_tables/V011__create_stock_reservation_line.sql](https://github.com/code-corhuila/synkro-products-db/blob/043e485d16018fc4fb4cd60694c2a521c143ddbd/01_ddl/03_tables/V011__create_stock_reservation_line.sql); [01_ddl/04_alter/V006__add_foreign_keys.sql](https://github.com/code-corhuila/synkro-products-db/blob/043e485d16018fc4fb4cd60694c2a521c143ddbd/01_ddl/04_alter/V006__add_foreign_keys.sql); [01_ddl/10_indexes/V007__create_indexes.sql](https://github.com/code-corhuila/synkro-products-db/blob/043e485d16018fc4fb4cd60694c2a521c143ddbd/01_ddl/10_indexes/V007__create_indexes.sql); [03_dcl/00_roles/V002__create_roles.sql](https://github.com/code-corhuila/synkro-products-db/blob/043e485d16018fc4fb4cd60694c2a521c143ddbd/03_dcl/00_roles/V002__create_roles.sql); [01_ddl/04_alter/V017__add_stock_after_to_stock_adjustment.sql](https://github.com/code-corhuila/synkro-products-db/blob/043e485d16018fc4fb4cd60694c2a521c143ddbd/01_ddl/04_alter/V017__add_stock_after_to_stock_adjustment.sql) |
| **Closing evidence** | Reviewed disposition of each difference against domain invariants and schema conventions, followed by additive forward migrations where required, matching rollback and fresh/upgrade/effective-permission tests. Never rewrite an applied migration or rename the documented source excerpt as though the old name never existed. Preserve existing data and verify duplicates before adding uniqueness. |
| **Priority / timing** | Resolve or explicitly review before claiming full physical-schema convention/enforcement compliance; no new HU or GitHub issue is created by this record |

---

## Resolved items

| ID | Description | Resolved in | Date |
|----|-------------|-------------|------|
| TD-005 | Rename DDL schema prefixes to `<domain>_schema` | HU-DOCS-71 (`models.md`) | 2026-10 |

---

## Correlations

- Risk register (risks these items mitigate) → `15-project-control/risks.md`
- Architectural technical debt table → `05-architecture/overview.md` §8 (AT-001 to AT-006)
- Planned evolution → `05-architecture/overview.md` §9
- Architecture decisions that originated these items → ADR-005, ADR-006, ADR-007, ADR-009, ADR-010, ADR-011
- Pattern evaluation → `05-architecture/pattern-guide.md`
