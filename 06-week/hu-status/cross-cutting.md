# Cross-Cutting Concerns

> Standards that apply to **all 4 microservices** regardless of language (Java or Go).
> Every item here must be implementable in both Spring Boot and Go
> without requiring a shared library or a centralized infrastructure component.

---

## 1. Standard Error Response Format

All services return errors using the same JSON structure. This allows every frontend to implement a single error handler.

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
  "traceId": "abc123-def456"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `error` | string | Yes | Error code in `SCREAMING_SNAKE_CASE` |
| `message` | string | Yes | Human-readable message in English |
| `details` | array | No | Per-field validation errors; present only for `VALIDATION_ERROR` |
| `traceId` | string | No | Correlation ID propagated from the request header (see §3) |

### Standard Error Codes

| Code | HTTP Status | When to Use |
|------|-------------|-------------|
| `VALIDATION_ERROR` | 400 | Request body or query parameter fails validation |
| `UNAUTHORIZED` | 401 | Missing, expired, or malformed JWT |
| `FORBIDDEN` | 403 | Valid JWT but insufficient role/permissions |
| `NOT_FOUND` | 404 | Resource does not exist or is deactivated (`active = false`) |
| `CONFLICT` | 409 | Business rule violation (e.g., duplicate email, insufficient stock) |
| `INTERNAL_ERROR` | 500 | Unhandled exception; details must never leak stack traces |
| `SERVICE_UNAVAILABLE` | 503 | Downstream service unreachable (used by sales-service when customers-service or products-service is down) |

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
  "service": "sales-service",
  "traceId": "abc123-def456",
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
| `service` | Yes | Service name matching the catalog (`auth-service`, `customers-service`, `products-service`, `sales-service`) |
| `traceId` | Yes (when available) | Correlation ID from the request header |
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

**Java (Spring Boot):** Logback with `logstash-logback-encoder` for JSON output. The `traceId` is injected into the MDC by a servlet filter.

**Go:** `slog` (standard library, Go 1.21+) configured with `slog.NewJSONHandler(os.Stdout, nil)`. The `traceId` is carried in the request context.

---

## 3. Correlation ID Propagation

Every incoming request receives a correlation ID that flows through all downstream calls, enabling end-to-end tracing without a distributed tracing system.

### Flow

```
Frontend                    Service A                    Service B
   │                           │                            │
   │── X-Trace-Id: <uuid> ───>│                            │
   │   (or absent)             │                            │
   │                           │ if absent, generate uuid   │
   │                           │ store in request context   │
   │                           │                            │
   │                           │── X-Trace-Id: <uuid> ────>│
   │                           │                            │ log with traceId
   │                           │<── response ───────────────│
   │                           │                            │
   │<── X-Trace-Id: <uuid> ───│                            │
   │    (echoed in response)   │                            │
```

### Rules

1. **Header name:** `X-Trace-Id`
2. **If the incoming request has the header:** use its value as-is.
3. **If absent:** generate a UUID v4 and use it for the rest of the request lifecycle.
4. **Every outgoing HTTP call** to another service must forward the same `X-Trace-Id`.
5. **Every log entry** during that request must include `traceId` (see §2).
6. **The response** echoes the `X-Trace-Id` header back to the caller.

### Implementation Notes

**Java (Spring Boot):** a `OncePerRequestFilter` that reads or generates the ID, stores it in the MDC, and adds it to the response.

**Go:** a middleware that reads or generates the ID, injects it into `context.Context`, and propagates it through the HTTP client.

---

## 4. Health Check Endpoints

Every service exposes a health check endpoint that requires no authentication.

### Endpoint

```
GET /health
```

**Security:** this endpoint is excluded from JWT validation (`security: []` in the OpenAPI contract).

### Response — Healthy (200)

```json
{
  "status": "ok",
  "service": "sales-service",
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
  "service": "sales-service",
  "timestamp": "2026-09-15T10:30:00Z",
  "dependencies": {
    "database": "down"
  }
}
```

### Dependency Checks

| Service | Dependencies to Check |
|---------|----------------------|
| auth-service | PostgreSQL (`schema: auth`) |
| customers-service | PostgreSQL (`schema: customers`) |
| products-service | PostgreSQL (`schema: products`) |
| sales-service | PostgreSQL (`schema: sales`), customers-service (optional), products-service (optional) |

For sales-service, the downstream service checks are **informational only** — the health endpoint returns `ok` even if customers-service or products-service are unreachable, because the service itself is healthy. It reports their status under `dependencies` so that an operator can see the full picture.

---

## 5. RS256 Public Key Distribution

ADR-001 §6 states that each service validates the JWT locally using Auth's public key, without calling auth-service per request. This section defines how the key reaches each service.

### Chosen Mechanism: Environment Variable

The RS256 public key is distributed as a **PEM-encoded environment variable** (`JWT_PUBLIC_KEY`) configured in each service's deployment.

```
JWT_PUBLIC_KEY=-----BEGIN PUBLIC KEY-----\nMIIBI...\n-----END PUBLIC KEY-----
```

### Why This Mechanism

| Alternative | Verdict | Reason |
|-------------|---------|--------|
| **Environment variable** | Adopted | Simplest option that satisfies the MVP; no additional endpoint, no network call at startup, no dependency on auth-service availability |
| JWKS endpoint (`GET /api/auth/.well-known/jwks.json`) | Future candidate | Standard mechanism (RFC 7517) that supports key rotation; requires auth-service to be available at startup or a caching strategy; worth adopting post-MVP |
| Mounted file volume | Rejected | Adds file-system coupling; environment variables are already the standard for secrets in Docker |

### Key Rotation

For the MVP, key rotation requires redeploying all 4 services with the new public key. This is acceptable because:

* The MVP has no real end users; deployments are frequent and controlled.
* The access token's TTL is short (defined in `04-requirements/non-functional.md`).
* Post-MVP, a JWKS endpoint would allow rotation without redeployment.

---

## 6. CORS Configuration

With no API Gateway, each backend service configures CORS independently.

### Rules

| Setting | Value | Reason |
|---------|-------|--------|
| `Access-Control-Allow-Origin` | The URL of the corresponding frontend (e.g., `http://localhost:5173` in development) | Each SPA calls its own backend; cross-SPA calls are not expected |
| `Access-Control-Allow-Methods` | `GET, POST, PUT, PATCH, DELETE, OPTIONS` | Full CRUD + stock update |
| `Access-Control-Allow-Headers` | `Content-Type, Authorization, X-Trace-Id` | JWT token + correlation ID |
| `Access-Control-Expose-Headers` | `X-Trace-Id` | So the frontend can read the echoed correlation ID |
| `Access-Control-Allow-Credentials` | `false` | JWT is sent via `Authorization` header, not cookies |

### Implementation Notes

**Java (Spring Boot):** `@CrossOrigin` annotation on controllers or a global `WebMvcConfigurer.addCorsMappings()` configuration.

**Go:** a CORS middleware (e.g., `rs/cors`) applied to the router.

### Exception: sales-service

sales-service calls customers-service and products-service server-to-server. These are backend-to-backend calls, not browser requests, so CORS does not apply to them. The HTTP client in sales-service does not send `Origin` headers.

---

## 7. Request and Response Conventions

### Pagination

List endpoints support pagination through query parameters:

```
GET /api/customers?page=1&limit=20
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

### Date and Time Format

All timestamps use ISO 8601 in UTC: `2026-09-15T10:30:00Z`. No local time zones in API responses.

### ID Format

All entity IDs use UUID v4, as defined in `02-domain/entities-and-rules.md`.

---

## 8. Summary Table

| Concern | Standard | Where It Is Configured |
|---------|----------|------------------------|
| Error format | `ErrorResponse` JSON with `error`, `message`, `details`, `traceId` | Each service's exception handler |
| Logging | Structured JSON to stdout | Logback (Java) / slog (Go) |
| Correlation ID | `X-Trace-Id` header, generated if absent | Middleware/filter in each service |
| Health check | `GET /health` — public, no auth | Each service's router |
| JWT validation | RS256 public key via `JWT_PUBLIC_KEY` env var | Each service's auth middleware |
| CORS | Per-service, allowing its own frontend origin | Spring `@CrossOrigin` / Go CORS middleware |
| Pagination | `?page=N&limit=N` with `meta` in response | Each list endpoint |
| Timestamps | ISO 8601, UTC | All API responses and log entries |
| IDs | UUID v4 | All entity primary keys |
| Soft deletion | `active` boolean field | All entities, all services |

---

## References

* Architecture decision → `05-architecture/decisions/records/ADR-001-architecture.md`
* Architectural overview → `05-architecture/overview.md`
* Security policy and RBAC → `00-governance/security-policy.md`
* Entities and business rules → `02-domain/entities-and-rules.md`
* Non-functional requirements → `04-requirements/non-functional.md`
* Deployment topology → `05-architecture/deployment.md`
