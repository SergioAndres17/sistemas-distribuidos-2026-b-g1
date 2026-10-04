# Cross-Cutting Concerns

> Standards that apply to **every service that receives requests** — the four
> `-api` services and `synkro-workflow` — regardless of language (Java or Go),
> plus the gateway and the worker where a section says so. Every item here must
> be implementable in both Spring Boot and Go without a shared library.

## Where Each Section Applies

| Section | Four `-api` services | `synkro-workflow` | `synkro-api-gateway` | `synkro-worker` |
|---|---|---|---|---|
| §1 Error format | ✅ | ✅ | ✅ (its own errors) | — |
| §2 Logging | ✅ | ✅ | ✅ | ✅ |
| §3 Correlation ID | ✅ | ✅ | ✅ (generates it if absent) | ✅ (one per run) |
| §4 Health check | ✅ | ✅ | ✅ | — (no HTTP) |
| §5 Public key | ✅ | ✅ | — | — |
| §6 CORS | — | — | ✅ (only here) | — |
| §7 Request and response conventions | ✅ | ✅ | — | — |
| §8 Limits and retries | ✅ | ✅ | ✅ (rate limit, timeouts) | ✅ (outgoing calls) |

---

## 1. Standard Error Response Format

All services return errors using the same JSON structure — also for unknown routes, methods not allowed and malformed JSON. This allows every frontend to implement a single error handler.

```json
{
  "error": "VALIDATION_ERROR",
  "message": "The field email is required",
  "details": [
    {
      "field": "email",
      "message": "The field email is required"
    }
  ],
  "traceId": "4f1c2b7e-8a3d-4e21-9b6f-0c5d7a2e1f90"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `error` | string | Yes | Error code in `SCREAMING_SNAKE_CASE` |
| `message` | string | Yes | Human-readable message in English |
| `details` | array | No | Per-field detail (`field`, `message`); present for `VALIDATION_ERROR` and when a business rule names the field |
| `traceId` | string | Yes | The request's `X-Correlation-Id` (see §3) |

### Error Codes

The only catalog of error codes, with their HTTP status and when to use each one, is `07-api/guidelines.md`. The set is closed: `VALIDATION_ERROR`, `UNAUTHORIZED`, `FORBIDDEN`, `NOT_FOUND`, `INVALID_STATUS_TRANSITION`, `BUSINESS_RULE_VIOLATION` and `INTERNAL_ERROR`; only the gateway adds `TOO_MANY_REQUESTS` and `SERVICE_UNAVAILABLE`. A domain rule (duplicate email, insufficient stock, inactive customer) is a `BUSINESS_RULE_VIOLATION` whose `message` and `details` say which rule failed; no service invents a code.

An error never exposes a driver message, a stack trace or an internal host name. The full detail goes to the log, with the same `traceId`.

### Implementation Notes

**Java (Spring Boot):** a `@RestControllerAdvice` with `@ExceptionHandler` methods mapping domain exceptions to the standard structure.

**Go:** a middleware or shared `errorResponse(w, code, err)` helper in the HTTP adapter layer.

---

## 2. Logging Standard

All services produce structured JSON logs to stdout. No service writes to files; log aggregation (if needed) is handled at the infrastructure level.

### Log Format

```json
{
  "timestamp": "2026-09-15T10:30:00.123Z",
  "level": "INFO",
  "service": "synkro-sales-api",
  "correlationId": "4f1c2b7e-8a3d-4e21-9b6f-0c5d7a2e1f90",
  "message": "Sale created successfully",
  "context": {
    "saleId": "550e8400-e29b-41d4-a716-446655440000",
    "customerId": "7c9e6679-7425-40de-944b-e07fc1f90ae7"
  }
}
```

| Field | Required | Description |
|-------|----------|-------------|
| `timestamp` | Yes | ISO 8601 with milliseconds, UTC |
| `level` | Yes | One of: `DEBUG`, `INFO`, `WARN`, `ERROR` |
| `service` | Yes | The repository name (`synkro-sales-api`, `synkro-workflow`, `synkro-worker`, …) |
| `correlationId` | Yes | The request's `X-Correlation-Id`, or the worker run's own ID (see §3) |
| `message` | Yes | Human-readable description of the event |
| `context` | No | Structured key-value pairs relevant to the event; never include sensitive data (passwords, tokens, PII) |

### Log Levels

| Level | Use |
|-------|-----|
| `DEBUG` | Internal state useful during development; disabled in production |
| `INFO` | Successful operations, service startup/shutdown, external calls made |
| `WARN` | Recoverable issues: retried request, slow query, approaching a limit |
| `ERROR` | Unrecoverable issues: unhandled exception, downstream service failure, data inconsistency |

### Implementation Notes

**Java (Spring Boot):** Logback with `logstash-logback-encoder` for JSON output. The `correlationId` is injected into the MDC by a servlet filter.

**Go:** `slog` (standard library, Go 1.21+) configured with `slog.NewJSONHandler(os.Stdout, nil)`. The `correlationId` is carried in the request context.

---

## 3. Correlation ID Propagation

Every incoming request receives a correlation ID that flows through all downstream calls, so a request can be followed across the gateway, the workflow and every participant (traces are also exported, see `deployment.md` §8).

### Flow

```
Frontend                    Service A                    Service B
   │                           │                            │
   │── X-Correlation-Id ─────>│                            │
   │   (or absent)             │                            │
   │                           │ if absent, generate uuid   │
   │                           │ store in request context   │
   │                           │                            │
   │                           │── X-Correlation-Id ─────>│
   │                           │                            │ log with correlationId
   │                           │<── response ───────────────│
   │                           │                            │
   │<── X-Correlation-Id ─────│                            │
   │    (echoed in response)   │                            │
```

### Rules

1. **Header name:** `X-Correlation-Id`.
2. **If the incoming request has the header:** reuse its value. The gateway generates one when the client sends none.
3. **If absent:** generate a UUID v4 and use it for the rest of the request lifecycle.
4. **Every outgoing HTTP call** to another service forwards the same `X-Correlation-Id`, including every saga step.
5. **Every log entry** during that request includes `correlationId` (see §2), and every error uses it as `traceId` (see §1).
6. **The response** echoes the `X-Correlation-Id` header back to the caller.
7. **The worker** has no incoming request: each job run generates its own ID and sends it on every call it makes.

### Implementation Notes

**Java (Spring Boot):** a `OncePerRequestFilter` that reads or generates the ID, stores it in the MDC, and adds it to the response.

**Go:** a middleware that reads or generates the ID, injects it into `context.Context`, and propagates it through the HTTP client.

---

## 4. Health Check Endpoints

Every service that receives requests exposes a health check endpoint that requires no authentication. It checks **only the service's own dependencies**: a participant being down is handled per call, not reported as the service's own failure.

### Endpoint

```
GET /health
```

**Security:** this endpoint is excluded from JWT validation (`security: []` in the OpenAPI contract).

### Response — Healthy (200)

```json
{
  "status": "ok",
  "service": "synkro-sales-api",
  "timestamp": "2026-09-15T10:30:00Z",
  "dependencies": {
    "database": "ok"
  }
}
```

### Response — Unhealthy (503)

```json
{
  "status": "degraded",
  "service": "synkro-sales-api",
  "timestamp": "2026-09-15T10:30:00Z",
  "dependencies": {
    "database": "down"
  }
}
```

### Dependency Checks

| Service | Dependencies to Check |
|---------|----------------------|
| `synkro-auth-api` | `synkro-db` — connectivity to `auth_schema` |
| `synkro-customers-api` | `synkro-db` — connectivity to `customers_schema` |
| `synkro-products-api` | `synkro-db` — connectivity to `products_schema` |
| `synkro-sales-api` | `synkro-db` — connectivity to `sales_schema` |
| `synkro-workflow` | `synkro-db` — connectivity to `workflow_schema` only; the saga participants are handled per call (retries, then the step fails and compensation starts), so their availability is not part of the workflow's health (ADR-007) |
| `synkro-api-gateway` | Answers by itself; it does not check upstreams, so one service down does not mark the gateway down |
| `synkro-worker` | No HTTP endpoint; each run logs its result, and a failed run is visible by its `correlationId` |

---

## 5. RS256 Public Key Distribution

Every service that receives requests validates the JWT by itself (ADR-006): the four `-api` services and `synkro-workflow`. This section defines how the public key reaches them.

### Chosen Mechanism: Environment Variable

The RS256 public key is distributed as a **PEM-encoded environment variable** (`JWT_PUBLIC_KEY`, or `JWT_PUBLIC_KEY_FILE` with a path) to every validating service. The gateway does not need it (it only checks that a credential is present), nor does the worker (it receives no requests). The private key exists only in `synkro-auth-api`.

```
JWT_PUBLIC_KEY=-----BEGIN PUBLIC KEY-----\nMIIBI...\n-----END PUBLIC KEY-----
```

### Why This Mechanism

| Alternative | Verdict | Reason |
|-------------|---------|--------|
| **Environment variable** | Adopted | Simplest option that satisfies the MVP; no additional endpoint, no network call at startup, no dependency on `synkro-auth-api` availability |
| JWKS endpoint (`GET /api/v1/auth/.well-known/jwks.json`) | Future candidate | Standard mechanism (RFC 7517) that supports key rotation; requires `synkro-auth-api` to be available at startup or a caching strategy; worth adopting post-MVP |
| Mounted file volume | Rejected | Adds file-system coupling; environment variables are already the standard for secrets in Docker |

### Key Rotation

For the MVP, key rotation requires redeploying every validating service with the new public key, and issuing new service tokens. This is acceptable because:

* The MVP has no real end users; deployments are frequent and controlled.
* The access token's TTL is short (defined in `04-requirements/non-functional.md`).
* Post-MVP, a JWKS endpoint would allow rotation without redeployment.

---

## 6. CORS Configuration

`synkro-api-gateway` handles CORS centrally (ADR-008) — domain services are never called directly by a browser, so they do not configure CORS. The rules below apply at the gateway.

### Rules

| Setting | Value | Reason |
|---------|-------|--------|
| `Access-Control-Allow-Origin` | The origin of `synkro-front` only (`http://localhost:5173` in `develop`) | The portals are served by `synkro-front` from that same origin, so one origin covers the whole interface |
| `Access-Control-Allow-Methods` | `GET, POST, PUT, PATCH, DELETE, OPTIONS` | Full CRUD and state transitions |
| `Access-Control-Allow-Headers` | `Authorization, Content-Type, Idempotency-Key, X-Correlation-Id` | Token, body, idempotent creation and correlation |
| `Access-Control-Expose-Headers` | `X-Correlation-Id, Location` | So the frontend can read the correlation ID and the URL of a created resource |
| `Access-Control-Allow-Credentials` | `false` | JWT is sent via `Authorization` header, not cookies |

### Implementation Notes

No domain service uses `@CrossOrigin` or a CORS middleware: CORS lives only in the gateway's NGINX configuration (ADR-008).

### Exception: internal service-to-service calls

`synkro-workflow` calls the customers, products and sales APIs during the saga, and `synkro-worker` calls the products API (ADR-007). These are backend-to-backend calls with a service token, not browser requests, so CORS does not apply to them.

---

## 7. Request and Response Conventions

### Pagination

Every list is paginated: `page` from 1 and `limit` from 1 to 100 (default 20). A `limit` out of range or an unknown filter is a `400 VALIDATION_ERROR`. Results are ordered newest first. A list without a limit is a defect, not a simplification.

```
GET /api/v1/customers?page=1&limit=20
```

Response includes pagination metadata:

```json
{
  "data": [ ... ],
  "meta": {
    "page": 1,
    "limit": 20,
    "total": 142,
    "totalPages": 8
  }
}
```

### Idempotent Creation

Every operation that creates a resource requires the `Idempotency-Key` header (8 to 128 characters). The first request answers `201` with `Location`; repeating it with the same key answers `200` with the same resource and creates nothing (ADR-005).

### Money

Amounts are integers in minor units (1/100 COP), named `…Cents`; never floating point (ADR-005).

### Date and Time Format

All timestamps use RFC 3339 in UTC: `2026-09-15T10:30:00Z`. No local time zones in API responses.

### ID Format

All entity IDs use UUID v4, as defined in `02-domain/entities-and-rules.md`. JSON fields use `camelCase`, and every route is versioned under `/api/v1/` (`07-api/guidelines.md`).

---

## 8. Explicit Limits and Retries

Every service declares these limits in its composition root, with their value. A default that is not declared is not a decision: in several frameworks it means "no limit".

| Limit | Initial value | Go | Java (Spring Boot) |
|---|---|---|---|
| Header read | 5 s | `http.Server.ReadHeaderTimeout` | `server.tomcat.connection-timeout` |
| Write / idle | 15 s / 60 s | `WriteTimeout`, `IdleTimeout` | `server.tomcat.keep-alive-timeout` |
| Connection pool size | 10 | `db.SetMaxOpenConns` | `spring.datasource.hikari.maximum-pool-size` |
| Wait for a connection | 5 s | context with deadline | `spring.datasource.hikari.connection-timeout` |
| Statement time | 5 s | context with deadline | `statement_timeout` |
| Graceful shutdown | 20 s | `srv.Shutdown(ctx)` | `server.shutdown: graceful` |
| Outgoing HTTP call | 5 s, at most 3 attempts | `http.Client.Timeout` | client timeout |

**Retries:** only network errors, `429` and `5xx` are retried, with exponential backoff and jitter, and a bounded number of attempts. A `4xx` is never retried: it is an answer, not a failure. A retried creation always carries the same `Idempotency-Key`.

---

## 9. Summary Table

| Concern | Standard | Where It Is Configured |
|---------|----------|------------------------|
| Error format | `{error, message, details?, traceId}`; closed catalog in `07-api/guidelines.md` | Each service's exception handler |
| Logging | Structured JSON to stdout, with `correlationId` | Logback (Java) / slog (Go) |
| Correlation ID | `X-Correlation-Id`, reused or generated, echoed | Middleware/filter in each service and the gateway |
| Health check | `GET /health` — public, checks connectivity to its own schema in the shared instance | Each service's router |
| JWT validation | RS256 with `JWT_PUBLIC_KEY` in every validating service | Each service's auth middleware (ADR-006) |
| CORS | Only at the gateway, only for `synkro-front`'s origin | `synkro-api-gateway` (ADR-008) |
| Pagination | `page`, `limit` 1–100, `{data, meta}`, newest first | Each list endpoint |
| Idempotent creation | `Idempotency-Key`, `201` then `200` with the same resource | Each creation use case (ADR-005) |
| Money | Integers in minor units, `…Cents` | All contracts and tables (ADR-005) |
| Limits and retries | Explicit values; retry only network, `429`, `5xx` | Each service's composition root |
| Timestamps | RFC 3339, UTC | All API responses and log entries |
| IDs | UUID v4 | All entity primary keys |
| Soft deletion | `active` boolean field | Every table whose rows can be deactivated |

---

## References

* Decisions behind this document:
  * Architecture → `05-architecture/decisions/records/ADR-001-architecture.md`
  * Schema-per-domain isolation → `05-architecture/decisions/records/ADR-009-shared-instance-schema-per-domain.md`; minor units, idempotency keys → `05-architecture/decisions/records/ADR-005-data-isolation-per-domain.md` Decisions 2–5
  * Token validation in every service, service tokens → `05-architecture/decisions/records/ADR-006-token-validation-per-service.md`
  * Saga with persisted state, scheduled worker → `05-architecture/decisions/records/ADR-007-persistent-saga-and-scheduled-work.md`
  * Gateway, workflow, worker and frontend stack → `05-architecture/decisions/records/ADR-008-cross-cutting-stack.md`
* Keep consistent with — a change in any of these documents requires reviewing this one:
  * Error catalog and API conventions → `07-api/guidelines.md`
  * Token validation and service tokens → `07-api/authentication.md`
  * Ports, environment, network isolation and observability → `05-architecture/deployment.md`
* Architectural overview → `05-architecture/overview.md`
* Security policy and RBAC → `00-governance/security-policy.md`
* Entities and business rules → `02-domain/entities-and-rules.md`
* Non-functional requirements → `04-requirements/non-functional.md`
