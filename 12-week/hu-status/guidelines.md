# REST Guidelines

> SynkroTech's REST standard. The entire team must follow this before
> designing a new endpoint.

---

## Versioning

**Decision: every route is versioned under `/api/v1/`** (`/api/v1/customers`).

- The gateway routes by version prefix, one route file per domain (ADR-008), so a future `/api/v2/…` can run beside `/api/v1/…` while clients move over.
- The contract of every service states which version it serves, so a client can tell from the URL which contract it is calling.

This replaces the earlier decision of unversioned routes: that decision assumed a single frontend moving in lockstep with the backend, but the portals are now deployed independently of each other (ADR-008) and cannot all switch at once.

**Health checks are the exception:** `GET /health` is not versioned and requires no token (`05-architecture/cross-cutting.md` §4). Each health Path Item overrides OpenAPI servers with the service origin. A /health path appended to a server ending in /api/v1 would describe /api/v1/health, so the override is required.

---

## Endpoint naming

- Plural for collections: `GET /api/v1/customers`, not `/api/v1/customer`
- Nouns, not verbs: `GET /api/v1/sales/{id}` (not `GET /api/v1/getSale`)
- kebab-case for multi-word URL segments: `/api/v1/stock-reservations`
- Nested sub-resources only when the relationship is real ownership: `POST /api/v1/products/{id}/stock-adjustments` (an adjustment belongs to the product), never for references between distinct aggregates — a sale references its customer and products by ID in the body
- **State transitions** are a `POST` to a sub-resource named after the transition: `POST /api/v1/stock-reservations/{id}/release`, `POST /api/v1/stock-alerts/{id}/resolve`. A transition that is already done answers `200` with the resource unchanged; a transition the current status does not allow answers `422 INVALID_STATUS_TRANSITION`
- A fixed path segment takes precedence over `{id}`: `/api/v1/products/categories` is not read as a product with id `categories`

---

## Payload conventions

| Aspect | Rule |
|---|---|
| Field names | JSON in `camelCase` |
| Identifiers | UUID v4 |
| Money | Integer in minor units (1/100 COP), named `…Cents`; never floating point (ADR-005) |
| Timestamps | RFC 3339 in UTC (`2026-09-15T10:30:00Z`) |
| Date-only parameters | RFC 3339 full date (`2026-09-15`), interpreted as a UTC day |
| Domain entities | Never serialized directly: the HTTP adapter translates them into a response object, so renaming a domain field never breaks a client |

---

## Lists

**Every list is paginated, offset-based:** `page` from 1 and `limit` from 1 to 100 (default 20). This includes the sales reports: a list without a limit is a defect, not a simplification.

```
GET /api/v1/sales?page=1&limit=20
```

```json
{
  "data": [ ... ],
  "meta": { "page": 1, "limit": 20, "total": 142, "totalPages": 8 }
}
```

- Results are ordered newest first, with a stable order.
- A `limit` out of range, or a filter the endpoint does not declare, answers `400 VALIDATION_ERROR`.
- Cursor-based pagination is not used: no list in the MVP has the volume or the concurrent inserts that would justify it.

---

## Idempotent creation

Every operation that creates a resource requires the `Idempotency-Key` header (8 to 128 characters). The key and the resource are written in the same transaction (ADR-005).

| Request | Response |
|---|---|
| First request with a key | `201`, with `Location` and the created resource |
| Permitted same-key replay | `200`, with the original resource; authentication and authorization still apply |
| No key | `400 VALIDATION_ERROR` |

The saga uses `<sagaId>:<step>` as the key of each step (ADR-007). The original operation identity survives timeout/restart; clients must not solve uncertainty by inventing another key.

Workflow's observed normalized replay rules are a review candidate under HU-ARQ-28: same authenticated caller/equivalent validated input gives 200; conflicting input gives generic 400; another caller's key gives generic 403 without exposing stored values or running steps. Existing GET authorization is unchanged. Adjustment replay uses the immutable V017 receipt; legacy NULL receipts have the documented fallback.

---

### Saga decision status

> **Decision dependency — HU-ARQ-28/#254:** replay and saga-safety changes are review candidates until the modifying decision is accepted. ADR-007 remains unchanged; the next new record is ADR-015. An implementation, closed code issue or validated OpenAPI file does not accept that decision.

The candidate may be circulated for technical review. It is not the effective replacement for ADR-007 Decision 2. HU-DOCS-95/#253 requires the relevant HU-ARQ-28 decision to be reviewed and accepted before dependent normative clauses are merged as approved guidance. Acceptance of that decision, not closure of every unrelated ARQ-28 task, is the dependency.

### Query and error reconciliation

Sales detail rejects any query string and a noncanonical UUID with 400. Sales reports accept only one nonempty value for each declared page/limit/from/to filter; ownership never comes from a query parameter. A from date later than to, or an offset (page - 1) * limit exceeding the signed 64-bit range, also returns 400; from = to is a valid one-day window. Customers validates UUID path identifiers; its detail controller does not inherit Sales' no-query rule. Workflow validates the saga UUID without adding an owner restriction to GET. Do not invent a universal detail-query policy from one service's behavior.

The contracts explicitly declare 500 INTERNAL_ERROR for ordinary persistence/unexpected failures with the shared safe envelope. A mutating 500 can be an uncertain outcome, not evidence that a retry should start another operation or that stock is releasable. Health 503 has a separate HealthResponse shape, not the gateway's SERVICE_UNAVAILABLE envelope.

## Correlation

Every request carries `X-Correlation-Id`: the gateway generates one if the client sends none, every service reuses it on its own calls, returns it in the response, writes it in every log line and uses it as the `traceId` of any error (`05-architecture/cross-cutting.md` §3).

---

## Standard responses

| Code | When to use it |
|---|---|
| 200 | Success with a body; also a repeated creation with the same `Idempotency-Key`, and a transition that was already done |
| 201 | Successful creation, with `Location` |
| 204 | Success without a body (not currently used — there is no physical `DELETE` anywhere in the system, per the soft-delete pillar; see note below) |
| 400 | Client error (payload validation, unknown filter, missing `Idempotency-Key`) |
| 401 | Authentication rejected: protected credential missing/invalid, wrong login credentials, or invalid/expired refresh credential |
| 403 | Not authorized (valid token, but its roles or permissions do not allow the operation) |
| 404 | Resource not found |
| 409 | Not used: a business conflict is a `422 BUSINESS_RULE_VIOLATION` (see the catalog below) |
| 422 | The payload is valid but a business rule or a state transition rejects it (`BUSINESS_RULE_VIOLATION`, `INVALID_STATUS_TRANSITION`) |
| 429 | Gateway only: rate limit exceeded, with `Retry-After` |
| 500 | Server error |
| 503 | Gateway upstream-unreachable error; separately, a public health check reports its own store failure using HealthResponse. Ordinary persistence failures return generic 500 |

**Note on 204 and `DELETE`:** no endpoint in the system uses a physical
`DELETE` HTTP verb result — `entities-and-rules.md` and `models.md`
already establish that the whole system uses soft deletion via the
`active` field. `DELETE /api/v1/customers/{id}` internally
performs `active = false` (not a real `DELETE FROM`) — the HTTP verb
stays because semantically it's still "deactivate this resource", but
the actual database operation never removes the row. It returns `200 OK`
with the updated resource (`active: false`), not `204`, so the client
sees the resulting state without having to fetch it again. Deactivating
a resource that is already inactive also answers `200`.

---

## Contract files

- One OpenAPI 3.0 file per service in `07-api/contracts/openapi/`. An operation URL combines the declared `servers[].url` base with its path; the base may already contain `/api/v1`, so do not add the prefix twice.
- Auth currently declares `http://synkro-auth-api:8080/api/v1` with `/auth/login`, yielding the internal operation URL `http://synkro-auth-api:8080/api/v1/auth/login`. That contract address describes platform-internal service access, not a browser destination.
- Browser requests use `/api/v1/auth/login` through the host's shared client and configured gateway (locally `http://localhost:8000`). Services are not called directly from outside the network; portals do not embed the internal server address.
- Browser login/refresh/401 handling is defined once in [Authentication](authentication.md#authentication-outcomes-by-operation), not inferred solely from the common HTTP status catalog.
- Errors, pagination, the `Idempotency-Key` and `X-Correlation-Id` headers and the security scheme are referenced from `_shared.yaml`, never redefined.

---

## Error format

```json
{
  "error": "VALIDATION_ERROR",
  "message": "The email field is required",
  "details": [
    { "field": "email", "message": "required" }
  ],
  "traceId": "4f1c2b7e-8a3d-4e21-9b6f-0c5d7a2e1f90"
}
```

| Field | Type | Description |
|---|---|---|
| `error` | string | Stable `SCREAMING_SNAKE_CASE` code from the closed catalog below — the frontend can branch logic on this field |
| `message` | string | Human-readable message, in English (per `documentation-rules.md`, "Language" — error messages to the frontend) |
| `details` | array, optional | Per-field detail (`field`, `message`); present on validation errors and when a business rule names the field |
| `traceId` | string | The request's `X-Correlation-Id` (`05-architecture/cross-cutting.md` §3) |

Every error uses this envelope, including unknown routes, methods not allowed and malformed JSON. It never contains a driver message, a stack trace or an internal host name.

**Catalog of error codes — closed.** This is the only catalog in the repository (`05-architecture/cross-cutting.md` §1 refers here). No service adds a code: a domain rule is expressed as `BUSINESS_RULE_VIOLATION`, with the rule in `message` and the field in `details`.

| Code | HTTP | When it appears |
|---|---|---|
| `VALIDATION_ERROR` | 400 | Missing or malformed fields, a `limit` out of range, an unknown filter, malformed JSON |
| `UNAUTHORIZED` | 401 | Token missing, expired, malformed or with an invalid signature; wrong login credentials; invalid refresh token |
| `FORBIDDEN` | 403 | Valid token, but its roles or permissions do not allow the operation |
| `NOT_FOUND` | 404 | The resource does not exist or is not active; unknown route |
| `INVALID_STATUS_TRANSITION` | 422 | The resource's current status does not allow the requested transition |
| `BUSINESS_RULE_VIOLATION` | 422 | A domain rule rejects the request |
| `INTERNAL_ERROR` | 500 | Unexpected failure; the detail goes only to the log |
| `TOO_MANY_REQUESTS` | 429 | **Gateway only** — rate limit exceeded; the response includes `Retry-After` |
| `SERVICE_UNAVAILABLE` | 503 | **Gateway only** — the target service cannot be reached |

**Previous codes and how they are expressed now:**

| Previous code | Now |
|---|---|
| `UNAUTHENTICATED`, `INVALID_CREDENTIALS`, `INVALID_REFRESH_TOKEN` | `401 UNAUTHORIZED` (the message never reveals whether an email exists) |
| `EMAIL_ALREADY_EXISTS`, `IDENTITY_DOCUMENT_ALREADY_EXISTS`, `CATEGORY_ALREADY_EXISTS` | `422 BUSINESS_RULE_VIOLATION`, with the field in `details` |
| `INSUFFICIENT_STOCK`, `INACTIVE_CUSTOMER`, `CUSTOMER_INACTIVE` | `422 BUSINESS_RULE_VIOLATION`, with the rule in `message` |
| `INVARIANT_VIOLATION` | `422 BUSINESS_RULE_VIOLATION` |
| `SALE_REGISTRATION_ERROR` | Not an HTTP error: the saga ends `COMPENSATED` and reports its failed step (ADR-007) |

---

## Correlations

- Authentication strategy → `07-api/authentication.md`
- OpenAPI contracts per service and shared components → `07-api/contracts/openapi/`
- Endpoints fixed by the decision records → `05-architecture/decisions/records/ADR-001-architecture.md` §8 and `05-architecture/decisions/records/ADR-004-api-contract-extensions.md`
- Money in minor units and idempotency keys → `05-architecture/decisions/records/ADR-005-data-isolation-per-domain.md`
- Gateway routing by version prefix → `05-architecture/decisions/records/ADR-008-cross-cutting-stack.md`
- Correlation, limits and retries → `05-architecture/cross-cutting.md`
- Business invariants that generate the 422 errors → `02-domain/entities-and-rules.md`
