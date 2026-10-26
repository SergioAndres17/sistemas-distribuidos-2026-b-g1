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
| Circuit Breaker | **Adopted in design; implementation gap TD-015** | Per-participant CLOSED/OPEN/HALF-OPEN behavior is not established by timeout/retry code |
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

`synkro-workflow` is Java/Spring Boot (ADR-008). The adopted design calls for a per-participant Circuit Breaker. The inspected HttpSaleParticipants and composition expose bounded calls/retries without that state machine; [TD-015](../15-project-control/technical-backlog.md#td-015--designed-circuit-breaker-is-not-implemented) requires an implementation or reviewed design resolution before claiming Circuit Breaker coverage. The section's configuration is a design target, not measured delivered behavior.

### Fallback Behavior

An open circuit means no current participant result is available; it is not proof that a previous mutating attempt failed before commit. The dependent safety clarification retains uncertain reservations until evidence exists. The following projection illustrates a confirmed rejection with completed required compensation, not a universal circuit-open fallback:

```json
{
  "sagaId": "0b4c2d1e-7f3a-4b58-9c6d-2e1f0a9b8c7d",
  "sagaType": "register-sale",
  "status": "COMPENSATED",
  "completedSteps": ["validate-customer"],
  "failedStep": "reserve-stock",
  "createdAt": "2026-10-24T20:00:00Z",
  "updatedAt": "2026-10-24T20:00:01Z"
}
```

There is no cached fallback, because creating a sale with stale customer or stock data would violate business invariants. The only safe fallback is to reject the operation and let the user retry.

---

## 2. Saga — Adopted, with Persisted State (ADR-003, ADR-007)

### First adoption (ADR-003)

ADR-003 moved sale registration from inline orchestration inside the sales service to a saga in `synkro-workflow`, and reordered the steps so that stock is reserved before the sale is written. It kept the saga state only in memory.

### Current Decision (ADR-007)

- **State persisted after every step** in `workflow-db`, the workflow's own instance. Statuses: `RUNNING`, `COMPLETED`, `COMPENSATED`, `FAILED`. On startup, eligible RUNNING rows resume forward or backward; rows privately marked OUTCOME_UNRESOLVED are excluded from automatic recovery and require evidence-based handling.
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

CQRS separates the write model (commands) from the read model (queries), allowing each to be optimized independently. In the SynkroTech context, the potential use case is reports: the write model is a Sale document with embedded details, and the alternative read model would be a pre-aggregated summary.

### Why It Is Rejected

The read and write models in this system are nearly identical. The entities the API exposes for reading (customers, products, sales) are the same entities the API writes to — there is no transformation, no denormalization, and no different schema. The reporting queries group the MongoDB sale collection and its embedded details on demand (ADR-010); no measured latency is inferred from the query shape.

Introducing CQRS would mean maintaining two models (a write model and a read projection) with a synchronization mechanism between them, for a system where an on-demand aggregation already answers the query.

### The Reporting Strategy We Use Instead

Reports are MongoDB aggregation pipelines over the sale collection and embedded details: active-sale/date/owner filters precede grouping; top products also filters active details, and stable ordering precedes pagination. The `sales_summary` table is not created (ADR-005): `SalesSummary` is a derived projection, not a domain entity (`02-domain/entities-and-rules.md`).

### When It Would Be Adopted

If the transaction volume grows to the point where aggregation queries degrade the write path's performance (contention or aggregation cost on the sale collection during reporting windows), CQRS with a materialized read model becomes justified. This is not expected within the academic scope of the project.

---

## 5. Timeout and Retry Policy

### Timeouts

Outgoing calls remain bounded. The current Workflow composition uses a 3 s call timeout, 2 s connect timeout and three durable attempts per step; the cross-cutting table records the design bound and actual override. A timeout is not business rejection evidence and bounded calls do not prove an implemented Circuit Breaker.

### Retry Policy

| Call | Retryable? | Policy | Rationale |
|------|-----------|--------|-----------|
| `validate-customer` (read) | Yes | Up to 3 attempts, exponential backoff with jitter | Idempotent by nature |
| `reserve-stock`, `release-stock`, `register-sale` | Yes | Up to 3 attempts, exponential backoff with jitter | Each call carries `Idempotency-Key: <sagaId>:<step>`, so a retry returns the first result instead of repeating the effect |

Approved retry categories remain network, 429 and 5xx. A 4xx response alone never authorizes compensation: permitted rejection status, content type, single-object JSON, error/status correspondence and original trace must be validated. A previously ambiguous write can still have committed, even if a later attempt receives a valid rejection.

> Saga decision status and adoption boundary → [REST Guidelines](../07-api/guidelines.md#saga-decision-status).

The delivered HTTP 408 retry and delay range are tracked in [TD-016](../15-project-control/technical-backlog.md#td-016--retry-classification-and-jitter-differ-from-policy); no broader retry policy is accepted here. Private durable attempts survive restart and prevent unbounded retry. Malformed/protocol/local/authorization failures remain distinct from confirmed rejection. Source and boundary tests: [workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/out/http/HttpSaleParticipants.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/out/http/HttpSaleParticipants.java) and [workflow-adapters/src/test/java/co/edu/corhuila/synkro/workflow/adapter/out/http/HttpSaleParticipantsTest.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-adapters/src/test/java/co/edu/corhuila/synkro/workflow/adapter/out/http/HttpSaleParticipantsTest.java).

### Implementation Notes

Every call uses the workflow's service token (ADR-006) and forwards the request's `X-Correlation-Id`. The timeout and retry configuration lives in the workflow's composition root.

---

## 6. Stock Concurrency and Failure Compensation

### The Problem

Two concurrent sales could both read `stock = 5`, both try to sell 3 units, and both succeed — leaving stock at -1, violating the invariant "stock can never be negative."

### Chosen Mechanism: Ordered Row Locks and Atomic Updates

The delivered Products adapter locks the affected product rows in ascending product_id order with SELECT FOR UPDATE inside one transaction. It rechecks active state, availability and price under that lock, then writes every reservation line and stock change together. This is the actual persistence mechanism, not an assertion that a prior read outside the transaction is safe:

```sql
-- Illustrative projection of the adapter's ordered locking step:
SELECT product_id, price_cents, stock, active
FROM products_schema.product
WHERE product_id = ANY (:product_ids)
ORDER BY product_id
FOR UPDATE;
```

The adapter rolls back the whole reservation on any failed line. The stock >= 0 constraint remains a safety net; deterministic row order limits deadlocks and the locked row supplies each frozen price. No Redis lock, new version column or cross-domain transaction is introduced. Evidence: [internal/adapter/out/persistence/postgres_stock.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/adapter/out/persistence/postgres_stock.go) and [internal/adapter/out/persistence/postgres_stock_integration_test.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/adapter/out/persistence/postgres_stock_integration_test.go).

### Alternatives Evaluated

| Alternative | Verdict | Reason |
|-------------|---------|--------|
| Conditional UPDATE | Alternative | Can protect a deduction but is not the delivered multi-line reservation mechanism |
| Optimistic locking (`version` column) | Rejected | Requires adding a `version` column to `product`, modifying the domain entity, and handling retry-on-conflict — more complexity for the same result at MVP traffic |
| `SELECT FOR UPDATE` (pessimistic locking) | Delivered mechanism | Locks all affected products in deterministic order so stock and frozen price are checked consistently in the one-domain transaction |
| Application-level distributed lock (Redis) | Rejected | Introduces an infrastructure dependency (Redis) the MVP does not have |

### Failure Path and Compensation

| Failure point | What happens | Compensation |
|---|---|---|
| `validate-customer` rejects (inactive or missing customer) | Nothing was changed | None; the saga ends `COMPENSATED` with that failed step |
| `reserve-stock` rejects (a line lacks stock) | The reservation transaction rolls back as a whole | None; nothing was reserved |
| `register-sale` has validated rejection without earlier ambiguous effect | Stock is reserved and rejection evidence establishes the safe branch | Idempotent `release-stock`; never infer this branch from status alone |
| `register-sale` outcome is ambiguous or its rejection is malformed | A sale may have committed; the reservation remains held | No unsafe release; private suspension remains public RUNNING pending evidence |
| `release-stock` still fails after its durable attempts | Release outcome may be uncertain | The saga ends `FAILED` and a person decides; no automatic row reset |

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
