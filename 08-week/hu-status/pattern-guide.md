# Distributed Pattern Evaluation

> This document evaluates Circuit Breaker, Saga, Outbox, CQRS and
> Idempotency Key against the concrete scope of the SynkroTech SAS MVP.
> Each pattern receives an explicit **adopted**, **deferred** or
> **rejected** decision with its technical justification.
>
> This is not a generic pattern catalog — it is a project-specific
> evaluation. For reference material on what each pattern is, see the
> teaching notes preserved at the bottom of this file.

---

## Summary of Decisions

| Pattern | Decision | Justification (one line) |
|---------|----------|--------------------------|
| Circuit Breaker | **Adopted (simplified)** | `synkro-workflow` depends synchronously on three participants; failing fast prevents cascading timeouts |
| Saga | **Adopted, with persisted state (ADR-007)** | The sale spans three domains; its state is saved after every step, so it can resume or compensate after a restart |
| Outbox Pattern | **Deferred (ADR-007)** | No event is published in the MVP; it is applied to the publisher the day an event has a real consumer |
| CQRS | **Rejected for MVP** | Read and write models are nearly identical; reports use aggregation queries, not a separate read store |
| Idempotency Key | **Adopted (ADR-005, ADR-007)** | A retried creation or saga step must never create a second sale or reserve stock twice |

---

## 1. Circuit Breaker — Adopted (Simplified)

### Why It Applies

ADR-001 Consequences (Negative) identifies temporal coupling as a risk: if `synkro-products-api` is unavailable, `synkro-workflow` cannot complete the `register-sale` saga (ADR-007). Without a Circuit Breaker, `synkro-workflow` would hold connections open until each call times out, exhausting its connection pool and becoming unresponsive to every saga in progress — not just the ones that depend on the failing service.

### What We Adopt

A lightweight Circuit Breaker on every outgoing HTTP call from `synkro-workflow` to `synkro-customers-api`, `synkro-products-api` and `synkro-sales-api`. The mechanism follows the standard three-state model:

```
CLOSED (normal)
  │ call succeeds → stays CLOSED, reset failure count
  │ call fails    → increment failure count
  │ failure count >= threshold → switch to OPEN
  │
OPEN (circuit tripped)
  │ all calls fail immediately
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
| Timeout per call | 5 seconds | See §5 and `cross-cutting.md` §8 |
| Monitored calls | `validate-customer`, `reserve-stock`, `release-stock`, `register-sale` | The saga steps and the compensation (ADR-007) |

### What We Do NOT Adopt

A full resiliency library with bulkheads, rate limiters, and retry policies managed by an external configuration server. The MVP has a single instance of each service; the goal is fail-fast, not self-healing at scale.

### Implementation Guidance

`synkro-workflow` is Java 21 with Spring Boot 3.5 (ADR-008): use Resilience4j's `CircuitBreaker` module, with one instance per participant.

### Fallback Behavior

When the circuit is open, the step fails immediately, as any other failed step. The saga compensates what it already did and ends `COMPENSATED`, with the failed step recorded (ADR-007). The client reads that result in the saga resource:

```json
{
  "id": "0b4c2d1e-7f3a-4b58-9c6d-2e1f0a9b8c7d",
  "status": "COMPENSATED",
  "completedSteps": ["validate-customer"],
  "failedStep": "reserve-stock"
}
```

There is no cached fallback, because creating a sale with stale customer or stock data would violate business invariants. The only safe fallback is to reject the operation and let the user retry.

---

## 2. Saga — Adopted, with Persisted State (ADR-003, ADR-007)

### First adoption (ADR-003)

ADR-003 moved sale registration from inline orchestration inside the sales service to a saga in `synkro-workflow`, and reordered the steps so that stock is reserved before the sale is written. It kept the saga state only in memory.

### Current Decision (ADR-007)

- **State persisted after every step** in `workflow-db`, the workflow's own instance. Statuses: `RUNNING`, `COMPLETED`, `COMPENSATED`, `FAILED`. On startup, every `RUNNING` saga is resumed forward, or backward if it was compensating.
- **The sale is a saga resource:** `POST /api/v1/sagas/register-sale` with `Idempotency-Key`, and `GET /api/v1/sagas/{id}`.
- **Steps:** `validate-customer` (no side effect) → `reserve-stock` (compensation `release-stock`) → `register-sale` (last, no compensation).
- **Every step is idempotent:** it sends `Idempotency-Key: <sagaId>:<step>`, so a retried step never repeats its effect. If a compensation still fails after its bounded retries, the saga ends `FAILED` for a person to decide.
- **Participants are called with the workflow's service token** (ADR-006).

### What stays in `synkro-sales-api`

Single-domain operations that do not touch other services: sale queries and reports (FR-008, FR-009). They are not saga-worthy.

---

## 3. Outbox Pattern — Deferred (ADR-007)

### Previous Decision (ADR-003)

ADR-003 adopted RabbitMQ and an outbox in the sales database, so that `SaleCompleted` and `SaleFailed` would reach the worker reliably.

### Current Decision

Deferred. `SaleFailed` was removed (the saga record holds the failure), and the only consumer of `SaleCompleted` was a hypothetical email that no requirement asks for. Keeping a broker, an outbox and a relay without a real consumer is complexity without an owner.

### When It Would Be Adopted

The day an event has a consumer that a requirement needs. Then the publisher writes the event to an `outbox` table in the same transaction as the change, and a separate process publishes it — publishing right after the commit loses the event if the process stops between the two actions. The reference design of the event is kept in `02-domain/domain-events.md`.

---

## 4. CQRS — Rejected for MVP

### Why It Was Evaluated

CQRS separates the write model (commands) from the read model (queries), allowing each to be optimized independently. In the SynkroTech context, the potential use case is reports: the write model is `sale` + `sale_detail`, and the read model could be a pre-aggregated summary.

### Why It Is Rejected

The read and write models in this system are nearly identical. The entities the API exposes for reading (customers, products, sales) are the same entities the API writes to — there is no transformation, no denormalization, and no different schema. The reporting queries (daily sales, monthly sales, top products) are simple `SUM` + `GROUP BY` aggregations over `sale` and `sale_detail`, executable in milliseconds at the MVP's data volume.

Introducing CQRS would mean maintaining two models (a write model and a read projection) with a synchronization mechanism between them, for a system where a single `SELECT` already answers the query.

### The Reporting Strategy We Use Instead

Reports are computed on demand using aggregation queries directly over `sale` and `sale_detail`. The `sales_summary` table is not created (ADR-005): `SalesSummary` is a derived projection, not a domain entity (`02-domain/entities-and-rules.md`).

### When It Would Be Adopted

If the transaction volume grows to the point where aggregation queries degrade the write path's performance (contention on `sale`/`sale_detail` during reporting windows), CQRS with a materialized read model becomes justified. This is not expected within the academic scope of the project.

---

## 5. Timeout and Retry Policy

### Timeouts

Every outgoing HTTP call from `synkro-workflow` has a hard timeout of 5 seconds, declared with the other limits in `cross-cutting.md` §8. A call that exceeds it counts as a failure for the Circuit Breaker.

### Retry Policy

| Call | Retryable? | Policy | Rationale |
|------|-----------|--------|-----------|
| `validate-customer` (read) | Yes | Up to 3 attempts, exponential backoff with jitter | Idempotent by nature |
| `reserve-stock`, `release-stock`, `register-sale` | Yes | Up to 3 attempts, exponential backoff with jitter | Each call carries `Idempotency-Key: <sagaId>:<step>`, so a retry returns the first result instead of repeating the effect |

Only network errors, `429` and `5xx` are retried. A `4xx` is a business answer (for example, insufficient stock): it is never retried, and it starts compensation.

### Implementation Notes

Every call uses the workflow's service token (ADR-006) and forwards the request's `X-Correlation-Id`. The timeout and retry configuration lives in the workflow's composition root.

---

## 6. Stock Concurrency and Failure Compensation

### The Problem

Two concurrent sales could both read `stock = 5`, both try to sell 3 units, and both succeed — leaving stock at -1, violating the invariant "stock can never be negative."

### Chosen Mechanism: Conditional UPDATE with Row-Level Result Check

When a stock reservation is created, `synkro-products-api` decreases the stock of each line with one atomic SQL statement, all inside the same transaction:

```sql
UPDATE products.product
SET stock = stock - :quantity
WHERE product_id = :productId
  AND active = true
  AND stock >= :quantity;
```

The application checks `rows_affected`:

- **`rows_affected = 1`:** deduction succeeded for that line.
- **`rows_affected = 0`:** either the product does not exist, is inactive, or stock is insufficient; the whole reservation is rolled back and answered with `422 BUSINESS_RULE_VIOLATION`.

This mechanism works because:

1. PostgreSQL's `UPDATE` acquires a row-level lock implicitly — two concurrent UPDATEs on the same row are serialized.
2. The `CHECK (stock >= 0)` constraint on the `product` table acts as a safety net — even if the application logic has a bug, the database will reject a negative stock.
3. No explicit `SELECT FOR UPDATE` or optimistic locking (`version` column) is needed, because the read-then-write race condition is eliminated: there is no separate read step.

### Alternatives Evaluated

| Alternative | Verdict | Reason |
|-------------|---------|--------|
| **Conditional UPDATE** (chosen) | Adopted | Simplest; no extra columns; leverages PostgreSQL's implicit row lock; the `CHECK` constraint acts as a double safeguard |
| Optimistic locking (`version` column) | Rejected | Requires adding a `version` column to `product`, modifying the domain entity, and handling retry-on-conflict — more complexity for the same result at MVP traffic |
| `SELECT FOR UPDATE` (pessimistic locking) | Rejected | Holds the lock for the entire transaction duration; the conditional UPDATE already serializes concurrent changes on the same row |
| Application-level distributed lock (Redis) | Rejected | Introduces an infrastructure dependency (Redis) the MVP does not have |

### Failure Path and Compensation

| Failure point | What happens | Compensation |
|---|---|---|
| `validate-customer` rejects (inactive or missing customer) | Nothing was changed | None; the saga ends `COMPENSATED` with that failed step |
| `reserve-stock` rejects (a line lacks stock) | The reservation transaction rolls back as a whole | None; nothing was reserved |
| `register-sale` fails | The stock is reserved but no sale exists | `release-stock` gives back every line once; releasing an already released reservation changes nothing |
| `release-stock` still fails after its retries | Stock stays reserved without a sale | The saga ends `FAILED` and a person decides (ADR-007) |

### Idempotent Creation — Adopted

Every operation that creates a resource requires the `Idempotency-Key` header (8 to 128 characters). The key and the resource are written in the same transaction; a repeated key returns the original resource (`200`) instead of creating another. The saga applies the same rule to each step with `<sagaId>:<step>` (ADR-005, ADR-007). This replaces the earlier reliance on disabling the "Register sale" button: a network retry is no longer able to register a sale twice.

---

## 7. Patterns Not Evaluated

The following patterns appear in the teaching reference but were not evaluated because they have no applicability to the current system:

| Pattern | Why Not Evaluated |
|---------|-------------------|
| Event Sourcing | No audit or state-replay requirement beyond soft deletion; outside MVP scope |
| Backend for Frontend (BFF) | One web shell with four portals, all with similar data needs, behind one gateway |
| Strangler Fig | No legacy system to migrate from |
| Sidecar | No service mesh; each service handles its own concerns |

---

## Correlations

* Architecture decisions → `05-architecture/decisions/records/ADR-001-architecture.md`, `ADR-005-data-isolation-per-domain.md`, `ADR-006-token-validation-per-service.md`, `ADR-007-persistent-saga-and-scheduled-work.md`
* Architectural overview and pattern table → `05-architecture/overview.md` §6
* Error format, limits and retries → `05-architecture/cross-cutting.md` §1, §8
* Stock tables and constraints → `06-data/models.md` (domain `products`)
* Domain invariants and the saga at write time → `02-domain/entities-and-rules.md`
* Events deferred with the outbox → `02-domain/domain-events.md`
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
