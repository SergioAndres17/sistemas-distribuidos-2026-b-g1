# Domain Events

> **What to fill in here:** a domain event is a fact that already occurred
> in the business. It is the backbone of asynchronous communication
> between bounded contexts. The name is ALWAYS in past tense and in the
> ubiquitous language of the domain.
>
> **Status in the MVP: no events are published.** ADR-003 introduced
> `SaleCompleted` and `SaleFailed`; ADR-007 removed `SaleFailed` (the saga
> record holds the failure) and deferred `SaleCompleted`, the broker and
> the outbox until an event has a consumer that a requirement needs. This
> file keeps the concept, the sync/async decision for every interaction,
> and the reference design of `SaleCompleted` for that day.
>
> **This document feeds `09-microservices/service-catalog.md`**, the
> same way `domain-map.md` feeds `service-catalog.md` itself — these are
> not duplicate content, they are two levels of abstraction of the same
> fact (here, the event as a business concept; there, which service
> publishes/consumes it and over which channel).

---

## What is a domain event?

A **Domain Event** communicates that something important occurred in the
business. It is an immutable message that describes the fact in past
tense.

```
✓ SaleCompleted
✓ CustomerDeactivated

✗ RegisterSale (this is a command, not an event)
✗ SaleUpdated (too generic — what changed?)
✗ SaleEvent (does not indicate what occurred)
```

### Difference between Command and Event

| Concept | Intent | Tense | Can fail? |
|---|---|---|---|
| **Command** | Instruction to do something | Present/infinitive | Yes |
| **Event** | Notification of something that already occurred | Past | No (it already happened) |

```
Salesperson → [POST /api/v1/sagas/register-sale] → workflow → [SaleCompleted] → consumer
                     (Command, via Gateway)                  (Event, future candidate)
```

---

## Sync/async decision, per interaction

The professor explicitly requires deciding this for every interaction in
the system, not just the new ones. Here are the 10 real interactions in
SynkroTech, with their decision and justification:

| # | Interaction | Decision | Channel | Justification |
|---|---|---|---|---|
| 1 | Frontend → API Gateway | Synchronous | REST | The user expects an immediate response to their action (login, viewing the catalog, etc.) |
| 2 | Gateway → `synkro-auth-api` | Synchronous | REST | Simple routing; the client waits for the login result |
| 3 | Gateway → `synkro-customers-api` | Synchronous | REST | Direct CRUD operation; no reason to decouple a simple read/write |
| 4 | Gateway → `synkro-products-api` (catalog, stock adjustments, stock alerts) | Synchronous | REST | Same as above |
| 5 | Gateway → `synkro-sales-api` (reads, reports) | Synchronous | REST | The user needs to see the result on screen immediately |
| 6 | Gateway → `synkro-workflow` (`POST /api/v1/sagas/register-sale`, `GET /api/v1/sagas/{id}`) | Synchronous | REST | The salesperson needs to know whether the sale was registered or failed before continuing; if the saga is still running, the portal reads it again (ADR-007) |
| 7 | `synkro-workflow` → `synkro-customers-api` (saga step 1) | Synchronous | REST, service token | Part of an operation that must complete or be compensated as a unit |
| 8 | `synkro-workflow` → `synkro-products-api` (saga step 2 + compensation) | Synchronous | REST, service token | Same reason; every step is idempotent and its state is persisted |
| 9 | `synkro-workflow` → `synkro-sales-api` (saga step 3) | Synchronous | REST, service token | Same reason |
| 10 | `synkro-worker` → `synkro-products-api` (low-stock job) | Synchronous | REST, service token | Triggered by a schedule, not by a person; it reads products and opens or resolves alerts through the API (ADR-007) |

**Why interactions 1-9 are synchronous, and not an arbitrary choice:**
all of them occur within an operation the user is actively waiting for on
screen. Making any of them asynchronous would mean either polling from
the frontend (worse experience, more complexity) or telling the user
"your sale is pending" when the operation is actually fast enough
(milliseconds) not to need that.

**Why interaction 10 is synchronous even though no one waits for it:**
the worker is started by time, not by an event, so it simply calls the
API it needs. The MVP has no asynchronous interaction: the broker was
deferred because no requirement needs a consumer yet (ADR-007).

---

## Event catalog

**The MVP publishes no events** (ADR-007). `SaleCompleted` is kept below as
a future candidate: its design is ready for the day an event has a consumer
that a requirement needs, and it is not published until then — no outbox,
relay or broker exists for it in the MVP. The candidates noted before
ADR-003 existed (`SaleRequested`, `StockReserved`, `SaleCompensated`) did
not survive either — the saga's steps are synchronous REST calls, not
published events.

### Future candidate: `SaleCompleted` (not published in the MVP)

| Field | Value |
|---|---|
| **Name** | `SaleCompleted` |
| **Bounded Context** | Sales |
| **Aggregate** | `Sale` |
| **Trigger** | `synkro-sales-api` registers the sale successfully (saga step 3, ADR-007) |
| **Consumers** | None yet — a consumer must come from a requirement |
| **Channel (topic)** | `sale.completed` (`sales.events` exchange, topic type) |
| **Schema version** | `v1` |
| **Delivery guarantee** | At-least-once, once a broker is adopted (manual ack) |

**Payload (JSON schema):**

```json
{
  "eventId": "uuid",
  "eventType": "SaleCompleted",
  "aggregateId": "uuid (saleId)",
  "aggregateType": "Sale",
  "occurredAt": "ISO 8601 timestamp",
  "version": 1,
  "payload": {
    "saleId": "uuid",
    "customerId": "uuid, external reference to customers.customer_id",
    "createdBy": "uuid, external reference to auth.system_user.user_id (ADR-002)",
    "items": [
      { "productId": "uuid", "quantity": "integer > 0", "unitPriceCents": "integer > 0", "subtotalCents": "integer" }
    ],
    "totalCents": "integer >= 0"
  },
  "metadata": {
    "correlationId": "uuid — same value as X-Correlation-Id (cross-cutting.md)",
    "causationId": null,
    "userId": "uuid, same as payload.createdBy"
  }
}
```

**Real payload example:**

```json
{
  "eventId": "a1b2c3d4-0000-0000-0000-000000000001",
  "eventType": "SaleCompleted",
  "aggregateId": "9f8e7d6c-0000-0000-0000-000000000099",
  "aggregateType": "Sale",
  "occurredAt": "2026-09-20T14:32:00Z",
  "version": 1,
  "payload": {
    "saleId": "9f8e7d6c-0000-0000-0000-000000000099",
    "customerId": "c1c1c1c1-0000-0000-0000-000000000010",
    "createdBy": "u1u1u1u1-0000-0000-0000-000000000005",
    "items": [
      { "productId": "p1p1p1p1-0000-0000-0000-000000000020", "quantity": 2, "unitPriceCents": 4500000, "subtotalCents": 9000000 }
    ],
    "totalCents": 9000000
  },
  "metadata": {
    "correlationId": "trace-2026-09-20-000123",
    "causationId": null,
    "userId": "u1u1u1u1-0000-0000-0000-000000000005"
  }
}
```

**Note on `causationId`:** it stays `null` in the MVP because there is
only one hop (the Saga produces the event directly) — there is no
event-triggers-event chain yet. It would be populated if, in the future,
an event reacted to another event rather than directly to a command.

**What would consumers do with this event?** No consumer exists yet. The
idempotency design below shows what any consumer must do once one is
adopted.

### Removed: `SaleFailed`

ADR-007 removed it. A failed sale is the saga in `COMPENSATED` or `FAILED`,
with its failed step, readable through `GET /api/v1/sagas/{id}` and in the
workflow's structured logs. No other context needs to react to it.

---

## Standard fields for all events

Every event must include these fields in the envelope:

| Field | Type | Description |
|---|---|---|
| `eventId` | UUID | Unique event ID (for idempotency) |
| `eventType` | string | Event name in PascalCase |
| `aggregateId` | UUID | ID of the aggregate that generated the event |
| `aggregateType` | string | Aggregate type (`Sale`) |
| `occurredAt` | ISO 8601 | When the business fact occurred |
| `version` | integer | Schema version (for evolution) |
| `payload` | object | Event data (specific per type) |
| `metadata.correlationId` | UUID | For tracing a transaction across services — reuses the same `X-Correlation-Id` already defined in `cross-cutting.md` |
| `metadata.causationId` | UUID \| null | ID of the event or command that caused this event |
| `metadata.userId` | UUID | User who initiated the chain (same as `createdBy`) |

---

## Event flow: Sale registration

```
Salesperson
  │
  │  POST /api/v1/sagas/register-sale (command, via Gateway)
  ▼
[synkro-workflow: register-sale saga, state persisted after every step]
  │
  │  validate-customer → reserve-stock → register-sale
  │
  ├── success ──▶ [Aggregate: Sale] registered ──▶ saga COMPLETED
  │                                    (future: SaleCompleted event)
  │
  └── failure ──▶ release-stock (compensation) ──▶ saga COMPENSATED
                  compensation fails ────────────▶ saga FAILED, for a person
```

---

## Schema evolution strategy

Events are contracts. Changing them incompatibly breaks consumers. No
event has needed a v2 yet — this section stays as reference for when it
happens.

**Compatible change (breaks nothing):**
```
✓ Add a new optional field to the payload
✓ Add a new event type
✓ Change a required field → optional
```

**Incompatible change (breaks consumers):**
```
✗ Remove a field from the payload
✗ Change a field's type (string → number)
✗ Change an optional field → required
✗ Change the event's name
```

**How to evolve without breaking consumers:** publish the new type
(`SaleCompletedV2`) alongside the old one during a migration period,
migrate its consumers to v2, announce v1's deprecation one sprint in
advance, and only then stop publishing v1.

---

## Event summary table

| Event | Origin context | Topic | Consumers | Version | Status |
|---|---|---|---|---|---|
| `SaleCompleted` | Sales | `sale.completed` | None yet | v1 | Future candidate (ADR-007) |
| `SaleFailed` | Sales | — | — | — | Removed (ADR-007) |

---

## Policies — Reactions to events

A **Policy** describes what happens automatically when an event arrives:
"whenever X occurs, do Y".

| Trigger event | Policy | Service |
|---|---|---|
| `SaleCompleted` | Whenever a sale completes, notify the customer (hypothetical example — see note below, not real scope yet) | A future consumer |

---

## Resilience patterns for events

### At-least-once delivery + Idempotency

When an event is adopted, the broker will deliver it **at least once**,
and it may deliver it more than once (in case of retries). Consumers must
be **idempotent**. This section is the reference design for that day.

**Why the generic "check in a database whether the eventId was already
processed" pattern doesn't apply here:** that's the standard pattern any
generic event template would assume, but it requires the consumer to
have its own database — and `synkro-worker` does not have one (ADR-007
keeps it that way). The real mechanism used here is
different, and is explained in full below.

### Idempotent consumer design for `SaleCompleted`

**Important note:** neither "sending a confirmation email" nor any
specific provider (SendGrid, Mailgun, etc.) are part of SynkroTech's real
scope — no FR requires it. It is used only as a hypothetical example to
show the idempotency mechanism concretely, because "the consumer must be
idempotent" without a real example is an empty statement. If a real email
feature is decided in the future, the provider is chosen at that point —
this does not commit to it.

**The problem:** if `synkro-worker` crashes right after processing a
message but before confirming (`ack`) its receipt, the broker redelivers
it. If the job `worker` performed were "send a confirmation email to the
customer," an unprotected redelivery would mean sending the same email
twice.

**Suppose** `worker` had to send a confirmation email to the customer
when `SaleCompleted` arrives. The mechanism would be: use the `saleId`
from the event payload as an **idempotency key** when calling any
transactional email provider that supports key-based deduplication
(SendGrid and Mailgun are two examples that support this, via an
`Idempotency-Key` header or equivalent):

```
POST https://api.[email-provider]/v3/mail/send
Idempotency-Key: sale-completed-{saleId}
Body: { to: customerEmail, template: "sale-confirmation", ... }
```

**What would happen on a duplicate delivery:** the provider would see the
same `Idempotency-Key` (`sale-completed-{saleId}`) and would not resend
the email — it would return the same response it had already given the
first time. Deduplication happens on the provider's side, not in
`worker`.

**Why this mechanism would be genuine idempotency:**
- The key is deterministic: the same `saleId` would always generate the
  same `Idempotency-Key`, no matter how many times the message arrives.
- It would not depend on `worker` remembering anything — the "already
  sent" state would live in the external provider, which does have its
  own database.
- When something needs real persistence, the worker leans on a service
  that already has it (here, the email provider, in the hypothetical
  example).

**Alternative if the job has no external provider with native
idempotency:** for a future job without that option, the consumer
would call the domain service that owns the data with an
`Idempotency-Key` derived from the `eventId`, and that service (which
does have a database) would decide whether it was already processed. This is not implemented now because no real job needs it yet.

### Dead Letter Queue (DLQ)

When an event fails after N retries, it goes to the DLQ.

| Configuration | Reference value |
|---|---|
| Retries before DLQ | 3 |
| Backoff | Exponential (1s → 2s → 4s) |
| DLQ retention | 7 days |
| Alert | When the DLQ has > 0 messages |

> A DLQ runbook is written when the broker is adopted.

---

## Correlations

- Decision that created these events → `05-architecture/decisions/records/ADR-003-gateway-saga-async.md`, §4 and §5
- Decision that removed `SaleFailed` and deferred `SaleCompleted`, the broker and the outbox → ADR-007
- Infrastructure mapping (which service calls which, over which channel) → `09-microservices/service-catalog.md`, "Service communication matrix"
- `Sale` entity invariants that `SaleCompleted`'s payload reflects → `02-domain/entities-and-rules.md`
- `X-Correlation-Id` mechanism reused in `metadata.correlationId` → `05-architecture/cross-cutting.md`
