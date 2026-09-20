# Distributed Pattern Evaluation

> This document evaluates Circuit Breaker, Saga, Outbox, and CQRS against
> the concrete scope of the SynkroTech SAS MVP, as required by the
> Distributed Systems course. Each pattern receives an explicit
> **adopted** or **rejected** decision with its technical justification.
>
> This is not a generic pattern catalog — it is a project-specific
> evaluation. For reference material on what each pattern is, see the
> teaching notes preserved at the bottom of this file.

---

## Summary of Decisions

| Pattern | Decision | Justification (one line) |
|---------|----------|--------------------------|
| Circuit Breaker | **Adopted (simplified)** | sales-service depends synchronously on two services; a fail-fast mechanism prevents cascading timeouts |
| Saga | **Rejected for MVP** | The only cross-service write is a 2-step flow simple enough to handle with inline compensation — a Saga framework adds complexity without proportional benefit |
| Outbox Pattern | **Rejected for MVP** | Requires a message broker the MVP does not have; there are no domain events to publish atomically |
| CQRS | **Rejected for MVP** | Read and write models are nearly identical; reports use aggregation queries, not a separate read store |

---

## 1. Circuit Breaker — Adopted (Simplified)

### Why It Applies

ADR-001 Consequences (Negative) identifies temporal coupling as a risk: if products-service is unavailable, sales-service cannot create sales. Without a Circuit Breaker, sales-service would hold connections open for the full TCP timeout (typically 30–60 seconds), exhausting its thread/goroutine pool and becoming unresponsive to all requests — not just the ones that depend on the failing service.

### What We Adopt

A lightweight Circuit Breaker on every outgoing HTTP call from sales-service to customers-service and products-service. The mechanism follows the standard three-state model:

```
CLOSED (normal)
  │ call succeeds → stays CLOSED, reset failure count
  │ call fails    → increment failure count
  │ failure count >= threshold → switch to OPEN
  │
OPEN (circuit tripped)
  │ all calls fail immediately with SERVICE_UNAVAILABLE (503)
  │ after cooldown period → switch to HALF-OPEN
  │
HALF-OPEN (probing)
  │ allow 1 call through
  │ if it succeeds → switch to CLOSED
  │ if it fails    → switch to OPEN
```

### Configuration

| Parameter | Value | Rationale |
|-----------|-------|-----------|
| Failure threshold | 3 consecutive failures | Low traffic MVP — 3 failures in a row is a strong signal |
| Cooldown period | 15 seconds | Short enough to recover quickly from a service restart |
| Timeout per call | 3 seconds | See §5 below |
| Monitored calls | `GET /api/customers/{id}`, `GET /api/products/{id}`, `PATCH /api/products/{id}/stock` | The only cross-service calls in the MVP |

### What We Do NOT Adopt

A full resiliency library with bulkheads, rate limiters, and retry policies managed by an external configuration server. The MVP has a single instance of each service; the goal is fail-fast, not self-healing at scale.

### Implementation Guidance

**Go (sales-service):** `sony/gobreaker` — a mature, minimal Circuit Breaker library. Wrap each HTTP client call in a `gobreaker.CircuitBreaker` instance.

**Java (if needed in future inter-service calls):** Resilience4j `CircuitBreaker` module — Spring Boot starter available.

### Fallback Behavior

When the circuit is open, sales-service returns `503 SERVICE_UNAVAILABLE` with the standard error format defined in `cross-cutting.md`:

```json
{
  "error": "SERVICE_UNAVAILABLE",
  "message": "The customers service is temporarily unavailable. Please try again shortly.",
  "traceId": "abc123-def456"
}
```

There is no cached fallback, because creating a sale with stale customer or stock data would violate business invariants. The only safe fallback is to reject the operation and let the user retry.

---

## 2. Saga — Rejected for MVP

### Why It Was Evaluated

ADR-001 Consequences (Negative) states there are no ACID transactions across services. The sale creation flow modifies data in two services (sales schema + products schema), and those two writes cannot be wrapped in a single database transaction. This is the textbook scenario where a Saga applies.

### Why It Is Rejected

The sale creation flow has **exactly 2 steps** across services:

```
Step 1: Create sale + sale_details in schema: sales       (sales-service)
Step 2: Deduct stock via PATCH /api/products/{id}/stock    (products-service)
```

A Saga pattern (whether choreographed with events or orchestrated with a coordinator) introduces infrastructure the MVP does not have (a message broker or a saga state machine) to solve a 2-step problem that can be handled with a simpler inline compensation strategy (see §6 below).

### When It Would Be Adopted

If the system grows to include payment processing, shipping, or any flow with 3+ services participating in a single business transaction, a Saga becomes justified. This is recorded as a future candidate in `05-architecture/overview.md` → Planned Evolution.

### The Compensation Strategy We Use Instead

See §6 — Failure Path and Compensation.

---

## 3. Outbox Pattern — Rejected for MVP

### Why It Was Evaluated

The Outbox pattern guarantees that a database write and an event publication happen atomically: the event is written to an `outbox` table in the same transaction as the business data, and a separate relay process reads and publishes it. This prevents the "write succeeded but event was lost" problem.

### Why It Is Rejected

The Outbox pattern requires two things the MVP does not have:

1. **A message broker** (RabbitMQ, Kafka) to publish the events to. The MVP uses synchronous REST exclusively (ADR-001 §5).
2. **Domain events that need to be published.** The MVP's cross-service communication is request-response: sales-service calls products-service directly to deduct stock. There is no event like `SaleCreated` that other services need to react to asynchronously.

Without a consumer for the events, writing them to an outbox table is overhead with no benefit.

### When It Would Be Adopted

If the system adopts asynchronous communication (the "Future Candidate" in `01-context/scope.md`), Outbox becomes the mechanism to guarantee event delivery. The `outbox` table structure from the teaching reference (see bottom of this file) would be added to the `sales` schema, and a relay process would publish to RabbitMQ.

---

## 4. CQRS — Rejected for MVP

### Why It Was Evaluated

CQRS separates the write model (commands) from the read model (queries), allowing each to be optimized independently. In the SynkroTech context, the potential use case is reports: the write model is `sales` + `sale_details`, and the read model could be a pre-aggregated `sales_summary`.

### Why It Is Rejected

The read and write models in this system are nearly identical. The entities the API exposes for reading (customers, products, sales) are the same entities the API writes to — there is no transformation, no denormalization, and no different schema. The reporting queries (daily sales, monthly sales, top products) are simple `SUM` + `GROUP BY` aggregations over `sales` and `sale_details`, executable in milliseconds at the MVP's data volume.

Introducing CQRS would mean maintaining two models (a write model and a read projection) with a synchronization mechanism between them, for a system where a single `SELECT` already answers the query.

### The Reporting Strategy We Use Instead

Reports are computed on demand using aggregation queries directly over `sales` and `sale_details`. The `sales_summary` table defined in ADR-001 §7 exists in the schema but **is not populated by any process in the MVP** — this is consistent with the decision in `02-domain/entities-and-rules.md` to treat `SalesSummary` as a derived projection, not a domain entity. The status of this table will be formalized in ADR-002.

### When It Would Be Adopted

If the transaction volume grows to the point where aggregation queries degrade the write path's performance (contention on `sales`/`sale_details` during reporting windows), CQRS with a materialized read model becomes justified. This is not expected within the academic scope of the project.

---

## 5. Timeout and Retry Policy

### Timeouts

Every outgoing HTTP call from sales-service has a hard timeout. The values are intentionally conservative for an MVP that runs on localhost or a local Docker network.

| Call | Timeout | Rationale |
|------|---------|-----------|
| `GET /api/customers/{id}` | 3 seconds | Single-row lookup by primary key; if it takes longer than 3s, the service is likely down |
| `GET /api/products/{id}` | 3 seconds | Same rationale |
| `PATCH /api/products/{id}/stock` | 5 seconds | Write operation; slightly more generous to account for lock contention |

If the timeout is exceeded, the call is treated as a failure for Circuit Breaker purposes.

### Retry Policy

| Call Type | Retryable? | Policy | Rationale |
|-----------|-----------|--------|-----------|
| `GET` (read) | Yes | 1 retry after 500ms | Idempotent by nature; a transient network hiccup should not fail the sale |
| `PATCH /api/products/{id}/stock` | **No** | No retries | The stock deduction is **not idempotent** — retrying could deduct stock twice. If it fails, the compensation path activates (see §6) |

### Implementation Notes

**Go (sales-service):** Use `net/http.Client` with `Timeout` set per-client (one client per downstream service). The retry logic is a simple `for` loop with a `time.Sleep`, not a retry library — for 1 retry, a library adds more complexity than it saves.

---

## 6. Stock Concurrency and Failure Compensation

### The Problem

Two concurrent sales could both read `stock = 5`, both try to sell 3 units, and both succeed — leaving stock at -1, violating the invariant "stock can never be negative."

### Chosen Mechanism: Conditional UPDATE with Row-Level Result Check

products-service implements stock deduction as a single atomic SQL statement:

```sql
UPDATE products
SET stock = stock - :quantity
WHERE product_id = :productId
  AND active = true
  AND stock >= :quantity;
```

The application checks `rows_affected`:

- **`rows_affected = 1`:** deduction succeeded; return `200 OK`.
- **`rows_affected = 0`:** either the product does not exist, is inactive, or stock is insufficient; return `409 CONFLICT`.

This mechanism works because:

1. PostgreSQL's `UPDATE` acquires a row-level lock implicitly — two concurrent UPDATEs on the same row are serialized.
2. The `CHECK (stock >= 0)` constraint on the `products` table acts as a safety net — even if the application logic has a bug, the database will reject a negative stock.
3. No explicit `SELECT FOR UPDATE` or optimistic locking (`version` column) is needed, because the read-then-write race condition is eliminated: there is no separate read step.

### Alternatives Evaluated

| Alternative | Verdict | Reason |
|-------------|---------|--------|
| **Conditional UPDATE** (chosen) | Adopted | Simplest; no extra columns; leverages PostgreSQL's implicit row lock; the `CHECK` constraint acts as a double safeguard |
| Optimistic locking (`version` column) | Rejected | Requires adding a `version` column to `products`, modifying the domain entity, and handling `StaleObjectStateException` / retry-on-conflict — more complexity for the same result at MVP traffic |
| `SELECT FOR UPDATE` (pessimistic locking) | Rejected | Holds the lock for the entire transaction duration; in a REST call where the lock crosses a network boundary (sales-service → products-service), the lock time is unpredictable |
| Application-level distributed lock (Redis) | Rejected | Introduces an infrastructure dependency (Redis) the MVP does not have |

### Failure Path and Compensation

The sale creation flow has three possible failure points after the initial validations (customer exists, products exist, stock sufficient):

```
sales-service                              products-service
     │                                            │
     │  1. INSERT sale + sale_details              │
     │     (local transaction in schema: sales)    │
     │                                             │
     │  2. For each detail line:                   │
     │     PATCH /api/products/{id}/stock ────────>│
     │                                             │
     │     Possible outcomes:                      │
     │     a) 200 OK ── stock deducted             │
     │     b) 409 CONFLICT ── insufficient stock   │
     │     c) timeout / 5xx ── service failure     │
     │                                             │
```

**Compensation rules:**

| Failure Point | What Happens | Compensation |
|---------------|-------------|--------------|
| Step 1 fails (INSERT sale) | Local transaction rolls back | None needed — nothing was written |
| Step 2, outcome (b): `409 CONFLICT` on any line | Stock was already insufficient when the UPDATE ran | Mark the sale as `active = false` in schema: sales; return `409 CONFLICT` to the client with the specific product and available stock |
| Step 2, outcome (c): timeout or 5xx on any line | Unknown whether stock was deducted or not | Mark the sale as `active = false` in schema: sales; log the incident with `ERROR` level and the `traceId`; return `503 SERVICE_UNAVAILABLE` to the client |
| Step 2 succeeds for lines 1–2 but fails on line 3 | Lines 1–2 already deducted stock | **Reverse the already-deducted lines** by calling `PATCH /api/products/{id}/stock` with a positive quantity to restore stock; then mark the sale as `active = false`; if the reversal call also fails, log it as a critical inconsistency for manual resolution |

**Why this is not a Saga:** a Saga implies a coordination mechanism (orchestrator or choreography through events) that manages the state machine of the transaction. Here, the compensation is inline code in sales-service's `CreateSaleUseCase` — a simple `if err != nil { compensate() }` block, not a state machine. This is proportional to a 2-step flow in an MVP.

### Idempotency of `POST /api/sales`

`POST /api/sales` is **not idempotent** in the MVP. Sending the same request twice creates two sales and deducts stock twice. This is acceptable because:

1. The frontend disables the "Register sale" button after the first click (documented in `12-ux-ui/wireframes.md`).
2. There is no retry policy on the write path (see §5).
3. The failure compensation marks failed sales as `active = false`, so they do not appear in normal queries.

If idempotency becomes necessary (e.g., unreliable network, mobile clients), the mechanism would be an `Idempotency-Key` header: the client sends a UUID with the request, sales-service stores it alongside the sale, and a duplicate key returns the original response without creating a new sale. This is recorded as a future candidate, not an MVP requirement.

---

## 7. Patterns Not Evaluated

The following patterns appear in the teaching reference but were not evaluated because they have no applicability to the current system:

| Pattern | Why Not Evaluated |
|---------|-------------------|
| API Gateway | ADR-001 decided direct frontend-to-service communication; see `overview.md` P3 |
| Event Sourcing | No audit or state-replay requirement beyond soft deletion; outside MVP scope |
| Backend for Frontend (BFF) | All 4 frontends are web SPAs with similar data needs |
| Strangler Fig | No legacy system to migrate from |
| Sidecar | No service mesh; each service handles its own concerns |

---

## Correlations

* Architecture decision → `05-architecture/decisions/records/ADR-001-architecture.md`
* Architectural overview and pattern table → `05-architecture/overview.md` §6
* Cross-cutting concerns (error format, timeouts) → `05-architecture/cross-cutting.md`
* Stock constraint and data model → `06-data/models.md` (schema: products)
* Domain invariants → `02-domain/entities-and-rules.md`
* Sale creation flow → `02-domain/entities-and-rules.md` §Resolving external references
* MVP scope and future candidates → `01-context/scope.md`

---
---

## Teaching Reference (preserved from template)

> The sections below are the original pattern catalog provided by the
> course instructor as reference material. They are preserved here for
> educational context but do not represent project decisions. Project
> decisions are in §1–§6 above.
>
> Code snippets use pseudo-TypeScript as a reference language; the
> project's actual stack is Java (Spring Boot) and Go.

### Circuit Breaker — Reference

```
CLOSED state (normal):
  Calls pass through → if N consecutive failures → switch to OPEN

OPEN state (circuit breaker):
  Calls blocked immediately (fail fast) → after T seconds → HALF-OPEN

HALF-OPEN state (testing):
  Allows 1 call → if it fails: back to OPEN | if it passes: back to CLOSED
```

### Retry with Exponential Backoff — Reference

```
Attempt 1: wait 100ms
Attempt 2: wait 200ms
Attempt 3: wait 400ms
Attempt 4: wait 800ms (max retries reached → fail)
```

### Saga — Reference

```
Orchestrated Saga:
  Coordinator ──▶ Step 1 (Service A) ──▶ Step 2 (Service B) ──▶ Step 3 (Service C)
  If Step 2 fails → Coordinator calls compensate(Step 1)

Choreographed Saga:
  Service A publishes Event1 → Service B reacts, publishes Event2 → Service C reacts
  If Service C fails → publishes CompensationEvent → Services B and A react
```

### Outbox Pattern — Reference

```sql
CREATE TABLE outbox (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  event_type   VARCHAR(100) NOT NULL,
  payload      JSONB NOT NULL,
  created_at   TIMESTAMPTZ DEFAULT NOW(),
  published_at TIMESTAMPTZ,
  published    BOOLEAN DEFAULT false
);

CREATE INDEX idx_outbox_unpublished ON outbox (created_at) WHERE published = false;
```

### CQRS — Reference

```
Write side:  Command → Aggregate → write to DB (source of truth)
Read side:   Query → read from projection/view (optimized for reads)
Sync:        DB trigger / event / scheduled job keeps the projection updated
```
