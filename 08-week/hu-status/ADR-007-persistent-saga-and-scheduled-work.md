# ADR-007 — Persistent Saga Execution and Scheduled Work

| Field | Value |
|-------|-------|
| **ID** | ADR-007 |
| **Date** | 2026-09-27 |
| **Status** | Accepted |
| **Authors** | Sergio Andrés Ordóñez Díaz |
| **Reviewers** | Angel Gustavo Solano Trujillo — Tech Lead, Jordan Ramirez Gallego, Fredman Santiago Plazas Artunduaga — Development team |
| **Modifies** | ADR-003 Decision 1, "who receives `POST /api/sales`" paragraph; ADR-003 Decision 3 (Message Broker); ADR-003 Decision 4 (stateless orchestration and step signatures); ADR-003 Decision 5 (Outbox Pattern) |

---

## Context

ADR-003 Decision 4 made `synkro-workflow` a stateless orchestrator: it runs the three steps of the sale in one HTTP request and keeps compensation in memory. Decisions 3 and 5 adopted RabbitMQ and an outbox in the `sales` schema, so that `synkro-worker` receives `SaleCompleted` and `SaleFailed`. Five problems have surfaced since:

1. **A restart loses the saga.** If `synkro-workflow` stops after reserving stock and before registering the sale, nothing remembers that the stock was reserved. ADR-003 accepts that "the salesperson sees a network error and retries", but the retry reserves stock a second time while the first reservation is never released.
2. **Retried steps are not safe.** Stock is changed through `PATCH /products/{id}/stock` with a `RESERVE` or `RELEASE` operation. A retry after a timeout applies the change twice, and a compensation that runs twice gives back twice the stock.
3. **`SaleFailed` cannot be published by anyone.** The only outbox lives in `sales` and is written in step 3. When step 1 or 2 fails, `sales-api` is never called; when step 3 fails, the transaction that would write the outbox row is the one that failed. `synkro-workflow` has no outbox of its own.
4. **The broker has no consumer that a requirement needs.** The only consumer of `SaleCompleted` is a hypothetical confirmation email, and `sales_summary` recalculation was dropped by ADR-005. `01-context/scope.md` still lists asynchronous communication through RabbitMQ as a future candidate, not as MVP scope.
5. **The worker has no real job.** ADR-003 describes it as a queue consumer, but no requirement gives it anything to consume.

ADR-003 Decision 4 also argued that, because no `synkro-workflow-db` repository was created, the workflow must not persist anything. A persistent store does not need a new repository: the workflow can own its store inside its own repository, as internal state of the orchestrator, not as a domain schema.

**Constraints:**
- ADR-003 is immutable; this ADR replaces sections of it without editing it.
- Every internal call uses the caller's service token (ADR-006).
- Each domain keeps its own database instance, and no service connects to another's (ADR-005).
- The low-stock alert on the INVENTORY dashboard is already designed in `12-ux-ui/navigation-map.md` (Flow 5), under FR-004.

---

## Decision 1 — Saga state persisted in the workflow's own store

### Options

**Option A — Stateless orchestration in memory (current).**
- **Pros:** no database for the workflow.
- **Cons:** a restart loses reserved stock; a saga cannot be resumed or compensated after a crash.

**Option B — A PostgreSQL instance owned by `synkro-workflow`.**
- **Pros:** state survives restarts; same engine and migration tool as the domains; transactional updates of each step.
- **Cons:** one more database container, and its schema is maintained inside `synkro-workflow`.

**Option C — An embedded file store (SQLite in Go, H2 in Java) on a volume.**
- **Pros:** no extra container.
- **Cons:** limits the workflow to a single instance; a different engine from the rest of the system.

### Decision

**We decided: Option B.** `synkro-workflow` declares its own PostgreSQL instance (`workflow-db`) in its `deploy/compose.yml`, with its own volume. Its schema is migrated with Flyway from the `synkro-workflow` repository, following the conventions of ADR-005. No other service connects to it.

- The saga state is written **after every step and before the next one**: status, completed steps, failed step, the initiating user and the step results the next steps need.
- Statuses: `RUNNING`, `COMPLETED`, `COMPENSATED`, `FAILED`. A `RUNNING` saga with a failed step recorded is compensating.
- On startup, the workflow resumes every `RUNNING` saga: forward from the next step, or backward if it was compensating.
- The workflow's health check covers its own store only. Participant availability is handled per call (retries and step failure), not in the health check.

### Dominant criterion

**A saga interrupted halfway always ends in a known state**: completed, compensated, or flagged for a person.

### Accepted cost

- One more database container, one more credential set and a schema maintained inside `synkro-workflow`.

---

## Decision 2 — The sale as a saga resource with idempotent steps

### Options

**Option A — One request runs the whole saga and answers only at the end (current).**
- **Pros:** simple for the client.
- **Cons:** a timeout leaves the client without knowing whether the sale exists; a retry starts a new saga.

**Option B — The saga is a resource: `POST` starts it, `GET` reads it, and every step is idempotent.**
- **Pros:** a retry returns the same saga; the client can always ask for the outcome; steps and compensations are safe to repeat.
- **Cons:** the portal must handle a saga that is still `RUNNING` when the response arrives.

### Decision

**We decided: Option B.** The sale registration saga is `register-sale`:

- `POST /api/v1/sagas/register-sale` requires `Idempotency-Key`. It answers `201` with `Location`; the same key answers `200` with the same saga and runs no step again. The request carries the customer and the lines (product and quantity).
- `GET /api/v1/sagas/{id}` returns the saga. The representation shows `status`, `completedSteps`, `failedStep` and, when completed, `saleId`. It never shows internal details.
- The `POST` runs the steps before answering whenever they finish within its request timeout; otherwise it answers with the saga in `RUNNING`, and the portal reads it with `GET` until it ends.
- The gateway routes `/api/v1/sagas/*` to `synkro-workflow`. `POST /api/v1/sales` stays on `sales-api` and is accepted only from callers holding `sales:register` (ADR-006).

Steps, in order:

| # | Step | Participant | Compensation |
|---|---|---|---|
| 1 | `validate-customer` | `customers-api`: the customer exists and is active | None: it has no side effect |
| 2 | `reserve-stock` | `products-api`: creates one stock reservation with every line; returns the current unit price of each line | `release-stock` |
| 3 | `register-sale` | `sales-api`: registers the sale with the lines, their unit prices and `createdBy`; computes subtotals and total | None: it is the last step |

- Each step sends `Idempotency-Key: <sagaId>:<step>`, so a retried step returns the same result without repeating its effect.
- Compensations run in reverse order and are idempotent. If a compensation still fails after its bounded retries, the saga ends in `FAILED`, with the failed step recorded, for a person to decide.
- Participants are called with the workflow's service token and the same `X-Correlation-Id` as the original request.
- Only network errors, `429` and `5xx` are retried, with exponential backoff, jitter and a bounded number of attempts. A business rejection (`4xx`) is never retried: it starts compensation.

### Dominant criterion

**A sale is never registered twice, and a retry never repeats a side effect**, whether the retry comes from the portal or from the workflow itself.

### Accepted cost

- Every participant operation used by the saga must honor `Idempotency-Key`.
- The portal must handle three outcomes (`COMPLETED`, `COMPENSATED` with the failed step, and `RUNNING`) instead of a single response.

---

## Decision 3 — Stock reservation as a resource of `products-api`

### Options

**Option A — `PATCH /products/{id}/stock` with `RESERVE` / `RELEASE` operations (current contract).**
- **Pros:** one endpoint per product.
- **Cons:** a retried request changes stock twice; one sale with several lines needs several calls that can partially fail; releasing twice gives back twice the stock.

**Option B — A stock reservation resource with lines and a status.**
- **Pros:** all lines of a sale are reserved in one transaction; releasing is naturally idempotent because the reservation knows whether it was already released.
- **Cons:** two more tables and a new resource in `products-api`.

### Decision

**We decided: Option B.** `products-api` exposes stock reservations:

- A reservation holds every line of a sale. Creating it decreases the stock of every line **in one transaction**; if any line lacks stock, nothing is reserved and the request is rejected with a business-rule error.
- Statuses: `RESERVED` and `RELEASED`, with a `CHECK` constraint. Releasing a reservation restores its stock once; releasing it again succeeds without changing anything.
- Creating a reservation requires `stock:reserve`, and releasing it requires `stock:release` (ADR-006). No person holds those permissions.
- Tables `stock_reservation` and `stock_reservation_line` follow ADR-005.
- Manual stock changes by INVENTORY are a separate operation (a stock adjustment), not a reservation.

### Dominant criterion

**Stock changes made by the saga are atomic per sale and safe to repeat.**

### Accepted cost

- A new resource, two tables and their endpoints in `products-api`, instead of reusing the stock update operation.

---

## Decision 4 — The saga record is the failure record

### Options

**Option A — Keep `SaleFailed` and publish it through an outbox owned by the workflow.**
- **Pros:** other services could react to failures.
- **Cons:** a second outbox and relay for an event no requirement consumes; the failure is already recorded in the saga.

**Option B — Remove `SaleFailed`; the saga record holds the failure.**
- **Pros:** one place to look for what failed and why; nothing to publish.
- **Cons:** if a future requirement needs to react to failures, an event must be added then.

### Decision

**We decided: Option B.** `SaleFailed` is removed. A failed sale is the saga in `COMPENSATED` or `FAILED`, with its failed step, readable through `GET /api/v1/sagas/{id}` and in the workflow's structured logs.

### Dominant criterion

**A domain event must be a business fact that another context needs to react to.** A copy of what the saga already records is not one.

### Accepted cost

- Failures are visible only through the saga resource and the logs, not as a published event.

---

## Decision 5 — RabbitMQ, the outbox and `SaleCompleted` deferred out of the MVP

### Options

**Option A — Keep RabbitMQ, the outbox and `SaleCompleted` (current).**
- **Pros:** demonstrates broker-based communication.
- **Cons:** a broker, an outbox table and a relay process whose only consumer is a hypothetical email that no requirement asks for.

**Option B — Defer them until an event has a consumer that a requirement needs.**
- **Pros:** no component without a purpose; the MVP matches the scope in `01-context/scope.md`.
- **Cons:** the MVP no longer demonstrates asynchronous messaging between services.

### Decision

**We decided: Option B.** RabbitMQ, the `sales` outbox, its relay and `SaleCompleted` are removed from the MVP and return to "future candidate". Consistency between domains is handled by the saga. When an event gets a consumer that a requirement needs, the broker is adopted again through a new ADR, and the outbox pattern is applied to the service that publishes it.

### Dominant criterion

**No component without a purpose that a requirement gives it.** A broker with no real consumer is complexity without an owner.

### Accepted cost

- The MVP does not show message-broker communication; eventual consistency is shown through the saga only.

---

## Decision 6 — The worker's job: low-stock alerts

### Options

**Option A — Consume `SaleCompleted` and send confirmation emails (current).**
- **Pros:** uses the broker.
- **Cons:** no requirement asks for emails, and Decision 5 removes the event.

**Option B — A scheduled job that opens and resolves low-stock alerts.**
- **Pros:** traces to FR-004 and to the INVENTORY dashboard already designed in Flow 5; easy to demonstrate.
- **Cons:** one global threshold for every product.

**Option C — A scheduled daily sales closing.**
- **Pros:** a classic scheduled report.
- **Cons:** reports are already served live (ADR-002), so the closing adds a table and an endpoint without a requirement that asks for a frozen snapshot.

**Option D — A job that releases abandoned stock reservations.**
- **Pros:** a safety net for stock.
- **Cons:** Decision 1 already resumes interrupted sagas, so this job would duplicate that responsibility.

### Decision

**We decided: Option B.** `synkro-worker` runs the `LowStockAlerts` job:

- Every `LOW_STOCK_EVERY`, it reads the products whose stock is at or below `LOW_STOCK_THRESHOLD` (global, default `5`), up to `BATCH_SIZE` per run.
- For each one it opens an alert in `products-api` with `Idempotency-Key: low-stock:<productId>:<date>`. At most one alert per product is `OPEN`, enforced by a partial unique index.
- In the same run, it resolves the `OPEN` alerts whose product is above the threshold again. Resolving is idempotent.
- Alert statuses are `OPEN` and `RESOLVED`; the `stock_alert` table follows ADR-005. `ADMIN` and `INVENTORY` can list alerts; only the worker's service token can open and resolve them (`stock-alerts:read`, `stock-alerts:write`, `products:read`).
- Each run has its own `X-Correlation-Id` and a `RUN_TIMEOUT`. One failing product is counted and logged, and does not stop the batch; calls are retried only on network errors, `429` and `5xx`, with bounded attempts.

Future work, recorded in `15-project-control/tech-backlog.md`: the daily sales closing (Option C) as a second job, and a per-product threshold.

### Dominant criterion

**The worker does work that a requirement already needs**, visible to a person, and safe to run twice.

### Accepted cost

- One threshold for every product: a laptop and a cable trigger an alert at the same stock level.
- Alerts appear only at the next run, not at the moment stock drops.

---

## Consequences

**What changes in the system:**
- `synkro-workflow` gains `workflow-db` and resumes interrupted sagas; the sale is started with `POST /api/v1/sagas/register-sale`.
- `products-api` gains stock reservations and stock alerts; `sales-api` receives sale lines with their unit prices from the saga.
- RabbitMQ, the `sales` outbox, its relay, `SaleCompleted` and `SaleFailed` leave the MVP. `02-domain/domain-events.md` lists no events published in the MVP.
- `synkro-worker` becomes a scheduled job with a service token and no broker.

**What must be watched:**
- Sagas that end in `FAILED` need a person. They are listed by status in the workflow's logs and through `GET /api/v1/sagas/{id}`.
- Resumption on startup must never repeat a completed step; the step idempotency keys guarantee it, and the workflow's integration tests cover a restart between steps.

---

## Affected documents

| Document | Required change |
|----------|-----------------|
| `02-domain/entities-and-rules.md`, `02-domain/domain-events.md`, `01-context/glossary.md` | Stock reservation and stock alert; no events published in the MVP; "Saga instance", "Stock reservation", "Stock alert" (HU-DOCS-43) |
| `05-architecture/deployment.md` | `workflow-db`; no RabbitMQ; worker settings (HU-DOCS-44, HU-DOCS-45) |
| `06-data/models.md`, `06-data/data-dictionary.md` | `stock_reservation`, `stock_reservation_line`, `stock_alert`, `saga_instance`; no `outbox` (HU-DOCS-47) |
| `05-architecture/security-threat-model.md` | Saga store; replayed saga steps (HU-DOCS-49) |
| `05-architecture/overview.md`, `05-architecture/pattern-guide.md` | Saga with persisted state; Outbox and event-driven communication deferred (HU-DOCS-51) |
| C4 Level 2 and sale-registration diagrams (Mermaid and draw.io) | `workflow-db`, no broker, saga statuses and compensation (HU-DOCS-52) |
| `07-api/contracts/openapi/` | Saga resource; stock reservations and alerts; sale lines with unit prices (HU-DOCS-58 to HU-DOCS-60) |
| ADR-004 | Saga, reservation and alert endpoints in the endpoint register (HU-DOCS-54) |
| `09-microservices/service-catalog.md` | Workflow store, worker job, no broker (HU-DOCS-70) |
| `04-requirements/functional.md` | FR-004, FR-005 and FR-007 responsible components (HU-DOCS-66) |
| `12-ux-ui/navigation-map.md` | Flow 1 saga outcomes; Flow 5 alerts produced by the worker (HU-DOCS-69) |
| `05-architecture/hexagonal-architecture.md` | Example without outbox write (HU-DOCS-68) |
| `15-project-control/tech-backlog.md`, `15-project-control/risks.md` | Daily sales closing and per-product threshold; saga store as a single instance (HU-DOCS-64) |
| `05-architecture/decisions/README.md` | ADR-007 row; ADR-003 marked as modified (this PR) |
| ADR-003 | **No changes**: immutable |

---

## Immutability rule

Once this ADR is `Accepted`, it is not edited. Any change is a new ADR that names this one, and the sections it replaces, in its **Modifies** field.

---

## References

- Stateless saga, broker and outbox → `05-architecture/decisions/records/ADR-003-gateway-saga-async.md`, Decisions 1, 3, 4 and 5
- Database per domain and schema conventions → ADR-005
- Service tokens and permissions → ADR-006
- Saga, Outbox and CQRS evaluation → `05-architecture/pattern-guide.md` §2–§4
- Current events → `02-domain/domain-events.md`
- INVENTORY dashboard with low-stock alerts → `12-ux-ui/navigation-map.md`, Flow 5
- Asynchronous communication as a future candidate → `01-context/scope.md`
