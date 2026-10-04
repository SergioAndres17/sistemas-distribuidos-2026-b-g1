# Technical Backlog

> Technical debt and deferred improvements identified during architecture
> decisions. Each item traces to the ADR that originated it and records
> the trigger that would justify picking it up. Items here are NOT bugs
> or product features — they are architectural improvements the team
> chose to defer in order to keep the MVP scope proportional to its
> requirements.

---

## Active items

### TD-001 — Daily sales closing job in the worker

| Field | Value |
|-------|-------|
| **ID** | TD-001 |
| **Description** | Add a second scheduled job to `synkro-worker` that produces a frozen daily snapshot of sales totals. Currently, reports aggregate `sale` and `sale_detail` live (ADR-002); a closing would write a daily summary row that reports can read instead of re-aggregating |
| **Reference** | ADR-007 Decision 6, Option C (evaluated and deferred) |
| **Impact if not resolved** | Reports always query live data, which is acceptable at current volume but may become slow as sales grow |
| **Trigger to pick up** | PO decision, or report response time exceeds the 5-second statement timeout |
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
| **Reference** | `cross-cutting.md` §5 "Future candidate" |
| **Impact if not resolved** | Key rotation requires redeploying every validating service with the new `JWT_PUBLIC_KEY`. Acceptable for the MVP (no real end users, frequent deployments) |
| **Trigger to pick up** | The system moves to a production-like environment with real users, or the team adopts automated deployments |
| **Estimated effort** | 3 SP |
| **Priority** | Medium |
| **Sprint** | Post-MVP |

---

### TD-005 — Rename DDL schema prefixes to `<domain>_schema`

| Field | Value |
|-------|-------|
| **ID** | TD-005 |
| **Description** | The SQL blocks in `models.md` still use the short schema prefix (`auth.system_user`, `customers.customer`, etc.), while the actual schema names are `auth_schema`, `customers_schema`, etc. (ADR-009, deployment.md §4). Rename all DDL references to match: `auth_schema.system_user`, `customers_schema.customer`, etc. Also update `V001__create_schema_*.sql` and `GRANT` statements |
| **Reference** | HU-DOCS-71, identified during HU-DOCS-55 |
| **Impact if not resolved** | `models.md` contradicts itself: the header says `<domain>_schema` but the SQL says `<domain>.`. Developers implementing the real migrations will need to choose one — the document does not tell them which |
| **Trigger to pick up** | Before the first real `-db` repository writes its migrations |
| **Estimated effort** | 2 SP |
| **Priority** | High |
| **Sprint** | Next sprint (pre-implementation) |

---

### TD-006 — Operational monitoring for the shared instance

| Field | Value |
|-------|-------|
| **ID** | TD-006 |
| **Description** | AT-002 (reopened in ADR-009) accepts the shared instance as a single point of failure with mitigations: connection-pool sizing, CI GRANT verification, and operational monitoring. The monitoring part (Prometheus alerts for connection saturation, query duration and instance health) is designed (deployment.md §8) but not yet configured |
| **Reference** | ADR-009 "What must be watched", `overview.md` AT-002 |
| **Impact if not resolved** | A slow query or connection leak would be detected only when a service starts returning 503, not proactively |
| **Trigger to pick up** | When `synkro-infra` is built and Prometheus/Grafana are running |
| **Estimated effort** | 2 SP |
| **Priority** | Medium |
| **Sprint** | First implementation sprint of `synkro-infra` |

---

## Resolved items

| ID | Description | Resolved in | Date |
|----|-------------|-------------|------|
| — | No resolved items yet | — | — |

---

## Correlations

- Risk register (risks these items mitigate) → `15-project-control/risks.md`
- Architectural technical debt table → `05-architecture/overview.md` §8 (AT-001 to AT-005)
- Planned evolution → `05-architecture/overview.md` §9
- Architecture decisions that originated these items → ADR-005, ADR-006, ADR-007, ADR-009
- Pattern evaluation → `05-architecture/pattern-guide.md`
