# Microservices Catalog

> The complete inventory of system services. One row per service in the
> registry, one card per service in the detail section. This document
> is the executive view — for deployment topology and compose files see
> `05-architecture/deployment.md`; for the data model of each schema
> see `06-data/models.md`.

---

## Services map

```
   synkro-front (host) packages 4 domain portals as Module Federation remotes.
   All external traffic enters through the API Gateway.

   PUBLIC PATH — every request from the browser

   synkro-front :5173 ──▶ synkro-api-gateway :8000 (NGINX)
                            │
                            ├── /api/v1/auth/*                              ──▶ synkro-auth-api
                            ├── /api/v1/customers/*                         ──▶ synkro-customers-api
                            ├── /api/v1/products/*, /api/v1/stock-alerts/*  ──▶ synkro-products-api
                            ├── /api/v1/sales/*                             ──▶ synkro-sales-api
                            └── /api/v1/sagas/*                             ──▶ synkro-workflow

   INTERNAL CALLS — service token, never through the gateway

   synkro-workflow ──▶ step 1   synkro-customers-api    validate-customer
                   ──▶ step 2   synkro-products-api     reserve-stock / release-stock
                   ──▶ step 3   synkro-sales-api        register-sale
   synkro-worker   ──▶          synkro-products-api     low-stock job

   DATA — one PostgreSQL instance per environment (synkro-db, defined by synkro-infra, ADR-009)

   synkro-auth-api       ──▶ auth_schema
   synkro-customers-api  ──▶ customers_schema
   synkro-products-api   ──▶ products_schema
   synkro-sales-api      ──▶ sales_schema
   synkro-workflow       ──▶ workflow_schema
   synkro-worker             no database (calls synkro-products-api)
```

---

## Service registry

| # | Service | Responsibility | Technology | Port | Database | Repository | Status |
|---|---------|---------------|------------|------|----------|------------|--------|
| 01 | `synkro-auth-api` | User registration/login, JWT (RS256) issuance, service tokens, roles | Java (Spring Boot) | 8080 | PostgreSQL, shared instance — schema `auth_schema`, own service user `AUTH_APP_USER`, member of role `auth_writer` | `synkro-auth-api` | 🔴 Planned |
| 02 | `synkro-customers-api` | Customer CRUD with soft deletion, lookup by identity document | Java (Spring Boot) | 8080 | PostgreSQL, shared instance — schema `customers_schema`, own service user `CUSTOMERS_APP_USER`, member of role `customers_writer` | `synkro-customers-api` | 🔴 Planned |
| 03 | `synkro-products-api` | Catalog, categories, stock adjustments, stock reservations, stock alerts | Go | 8080 | PostgreSQL, shared instance — schema `products_schema`, own service user `PRODUCTS_APP_USER`, member of role `products_writer` | `synkro-products-api` | 🔴 Planned |
| 04 | `synkro-sales-api` | Sale registration (saga step 3), sale queries, daily/monthly/top-products reports | Go | 8080 | PostgreSQL, shared instance — schema `sales_schema`, own service user `SALES_APP_USER`, member of role `sales_writer` | `synkro-sales-api` | 🔴 Planned |
| 05 | `synkro-api-gateway` | Single entry point: credential presence check, routing, rate limiting, CORS, correlation | NGINX (ADR-008) | 8000 | — | `synkro-api-gateway` | 🔴 Planned |
| 06 | `synkro-workflow` | Orchestrates the register-sale saga with persisted state after every step | Java (Spring Boot) (ADR-008) | 8080 | PostgreSQL, shared instance — schema `workflow_schema`, own service user `WORKFLOW_APP_USER`, member of role `workflow_writer` | `synkro-workflow` | 🔴 Planned |
| 07 | `synkro-worker` | Scheduled low-stock alert job | Go (ADR-008) | — | — (calls `synkro-products-api` via REST) | `synkro-worker` | 🔴 Planned |

**Statuses:** 🟢 Active | 🟡 In development | 🔴 Planned | ⏸ Deprecated

Every HTTP service listens on port `8080` inside the `platform` network; only `synkro-api-gateway` (:8000) and `synkro-front` (:5173) publish ports to the host (`deployment.md` §2). `synkro-worker` listens on no port.

---

## Frontend and shared repositories

| Repository | Pairs with | Type | Branches |
|------------|------------|------|----------|
| `synkro-auth-portal` | `synkro-auth-api` | React remote (Module Federation) | `main/qa/develop` |
| `synkro-customers-portal` | `synkro-customers-api` | React remote | `main/qa/develop` |
| `synkro-products-portal` | `synkro-products-api` | React remote | `main/qa/develop` |
| `synkro-sales-portal` | `synkro-sales-api` | React remote | `main/qa/develop` |
| `synkro-front` | all 4 portals | Frontend host: session, HTTP client, navigation | `main/qa/develop` |
| `synkro-auth-db` | `synkro-auth-api` | Flyway migrations for `auth_schema` | `main/qa/develop` |
| `synkro-customers-db` | `synkro-customers-api` | Flyway migrations for `customers_schema` | `main/qa/develop` |
| `synkro-products-db` | `synkro-products-api` | Flyway migrations for `products_schema` | `main/qa/develop` |
| `synkro-sales-db` | `synkro-sales-api` | Flyway migrations for `sales_schema` | `main/qa/develop` |
| `synkro-infra` | all services | PostgreSQL instance, init script, compose composition, observability, dev keys | `main/qa/develop` |
| `synkro-docs` | — | This documentation | `main` only |

---

## Detail per service

> The per-service folders under `09-microservices/services/` (README, data model, events, runbook — structure in `09-microservices/README.md`) are not created yet. Until they exist, the cards below are the authoritative per-service detail. `services/_example-api-gateway/` and `services/_example-auth-service/` are examples kept from the documentation template (HU-DOCS-61); neither describes a SynkroTech service. `_example-auth-service/` includes a Redis schema and a `[tu-stack].md` placeholder — no Redis was adopted in this project (`05-architecture/overview.md` §8, "What This Architecture Intentionally Does NOT Have").
### 01 — `synkro-auth-api`

| Field | Value |
|-------|-------|
| **Responsibility** | Manages the identity of SynkroTech SAS's internal users: credentials, roles, JWT (RS256) issuance, refresh tokens, and service-token issuance |
| **Type** | Supporting service (cross-cutting identity, not a core business process) |
| **Technology** | Java (Spring Boot) — 3-module Maven (domain, application, infrastructure) per ADR-008 |
| **Port** | 8080 |
| **DB** | PostgreSQL, shared instance — schema `auth_schema`, own service user `AUTH_APP_USER`, member of role `auth_writer` (ADR-009) |
| **Bounded Context** | `02-domain/domain-map.md` → Authentication and Users |
| **Externally exposed** | Via the gateway only. The only holder of the RS256 private key |
| **Contract** | `07-api/contracts/openapi/synkro-auth-api.yaml` |

**Key behavior:** issues JWTs and service tokens. Every other service validates the JWT locally with the public key (ADR-006) — no runtime call to this service per request. Service tokens are issued to an ADMIN and carry scoped permissions.

---

### 02 — `synkro-customers-api`

| Field | Value |
|-------|-------|
| **Responsibility** | Register, update, query, and deactivate customers. Lookup by identity document for the point-of-sale flow |
| **Type** | Core service |
| **Technology** | Java (Spring Boot) — 3-module Maven per ADR-008 |
| **Port** | 8080 |
| **DB** | PostgreSQL, shared instance — schema `customers_schema`, own service user `CUSTOMERS_APP_USER`, member of role `customers_writer` (ADR-009) |
| **Bounded Context** | `02-domain/domain-map.md` → Customers |
| **Externally exposed** | Via the gateway. Called internally by `synkro-workflow` (saga step 1: validate-customer) |
| **Contract** | `07-api/contracts/openapi/synkro-customers-api.yaml` |

---

### 03 — `synkro-products-api`

| Field | Value |
|-------|-------|
| **Responsibility** | Product catalog, categories, manual stock adjustments, stock reservations (saga step 2) and low-stock alerts (worker) |
| **Type** | Core service |
| **Technology** | Go — `cmd/`, `internal/`, `pkg/` layout per ADR-008 |
| **Port** | 8080 |
| **DB** | PostgreSQL, shared instance — schema `products_schema`, own service user `PRODUCTS_APP_USER`, member of role `products_writer` (ADR-009) |
| **Bounded Context** | `02-domain/domain-map.md` → Products and Inventory |
| **Externally exposed** | Via the gateway (`/api/v1/products/*`, `/api/v1/stock-alerts/*`). Called internally by `synkro-workflow` (saga step 2: reserve-stock, compensation: release-stock) and by `synkro-worker` (low-stock job) |
| **Contract** | `07-api/contracts/openapi/synkro-products-api.yaml` |

**Key behavior:** stock changes only through a reservation (saga) or a manual adjustment (INVENTORY/ADMIN). A reservation decreases stock for every line in one transaction; releasing restores it idempotently. At most one OPEN alert per product (partial unique index).

---

### 04 — `synkro-sales-api`

| Field | Value |
|-------|-------|
| **Responsibility** | Persists sales registered by the saga (step 3), serves sale queries and three report endpoints (daily, monthly, top-products) |
| **Type** | Core service |
| **Technology** | Go — `cmd/`, `internal/`, `pkg/` layout per ADR-008 |
| **Port** | 8080 |
| **DB** | PostgreSQL, shared instance — schema `sales_schema`, own service user `SALES_APP_USER`, member of role `sales_writer` (ADR-009) |
| **Bounded Context** | `02-domain/domain-map.md` → Sales |
| **Externally exposed** | Via the gateway for reads and reports. Called internally by `synkro-workflow` (saga step 3: register-sale with `sales:register` permission). Makes no outgoing calls to other domain services |
| **Contract** | `07-api/contracts/openapi/synkro-sales-api.yaml` |

**Key behavior:** `POST /api/v1/sales` requires the workflow's service token with `sales:register` — not directly accessible from the frontend. `created_by` comes from the saga's validated person token (ADR-002, ADR-006). SALESPERSON sees only own sales on GET endpoints. Reports aggregate live data — there is no `sales_summary` table (ADR-005 Decision 5).

---

### 05 — `synkro-api-gateway`

| Field | Value |
|-------|-------|
| **Responsibility** | Single external entry point: checks that a credential is present (does not validate the JWT), routes to the target service, applies rate limiting, CORS and correlation ID |
| **Type** | Infrastructure service |
| **Technology** | NGINX (ADR-008 Decision 1) |
| **Port** | 8000 (published to the host) |
| **DB** | — (stateless) |
| **Externally exposed** | Yes — the only service reachable from outside the `platform` network |
| **Contract** | Route configuration in `synkro-api-gateway` repository |

**Key behavior:** does NOT validate the JWT — each downstream service validates locally (ADR-006). Does not forward `X-User-Id` or `X-User-Roles` headers — identity comes only from the validated token inside each service. CORS is configured once here for `synkro-front`'s origin only (cross-cutting.md §6).

---

### 06 — `synkro-workflow`

| Field | Value |
|-------|-------|
| **Responsibility** | Orchestrates the `register-sale` saga with persisted state: validate-customer (step 1), reserve-stock (step 2), register-sale (step 3), with release-stock as compensation |
| **Type** | Orchestrator |
| **Technology** | Java (Spring Boot) — 3-module Maven per ADR-008 |
| **Port** | 8080 |
| **DB** | PostgreSQL, shared instance — schema `workflow_schema`, own service user `WORKFLOW_APP_USER`, member of role `workflow_writer` (ADR-009). Stores `saga_instance` with status, completed steps, failed step and step results |
| **Externally exposed** | Via the gateway (`/api/v1/sagas/*`). Calls `synkro-customers-api`, `synkro-products-api` and `synkro-sales-api` with its own service token |
| **Contract** | `07-api/contracts/openapi/synkro-workflow.yaml` |

**Key behavior:** the saga state is saved after every step (ADR-007 Decision 1). A RUNNING saga resumes after a restart. Each step sends `Idempotency-Key: <sagaId>:<step>` and the workflow's service token. A business rejection (4xx) is never retried — it starts compensation. A compensation that fails after bounded retries ends the saga in FAILED for a person to decide.

---

### 07 — `synkro-worker`

| Field | Value |
|-------|-------|
| **Responsibility** | Runs a scheduled low-stock alert job: queries products below the threshold, opens alerts, resolves alerts when stock is restored |
| **Type** | Scheduled background job |
| **Technology** | Go — `cmd/`, `internal/`, `pkg/` layout per ADR-008 |
| **Port** | — (no HTTP endpoint, no health check) |
| **DB** | — (calls `synkro-products-api` via REST, does not connect to the database directly) |
| **Externally exposed** | No. Not routed by the gateway. Calls `synkro-products-api` with its own service token (`stock-alerts:write`, `stock-alerts:read`, `products:read`) |
| **Contract** | — (no contract of its own; it consumes `synkro-products-api.yaml` Stock Alerts and Products endpoints) |

**Key behavior:** runs every `LOW_STOCK_EVERY` (default 15 minutes). Each run logs its result with a `correlationId`. The job is bounded by `RUN_TIMEOUT` (default 2 minutes) and processes products in batches of `BATCH_SIZE` (default 100). A failed run is visible in the logs by its `correlationId`; the next run retries. Does NOT consume from a message broker — ADR-007 Decision 5 deferred RabbitMQ.

---

## Service communication matrix

| Source | Destination | Channel | Auth | Description |
|--------|-------------|---------|------|-------------|
| `synkro-front` | `synkro-api-gateway` | HTTP :8000 | Person's JWT | All frontend traffic enters here |
| `synkro-api-gateway` | `synkro-auth-api` | HTTP :8080 | Forwarded JWT | `/api/v1/auth/*` |
| `synkro-api-gateway` | `synkro-customers-api` | HTTP :8080 | Forwarded JWT | `/api/v1/customers/*` |
| `synkro-api-gateway` | `synkro-products-api` | HTTP :8080 | Forwarded JWT | `/api/v1/products/*`, `/api/v1/stock-alerts/*` |
| `synkro-api-gateway` | `synkro-sales-api` | HTTP :8080 | Forwarded JWT | `/api/v1/sales/*` |
| `synkro-api-gateway` | `synkro-workflow` | HTTP :8080 | Forwarded JWT | `/api/v1/sagas/*` |
| `synkro-workflow` | `synkro-customers-api` | HTTP :8080 | Workflow service token | Step 1: validate-customer |
| `synkro-workflow` | `synkro-products-api` | HTTP :8080 | Workflow service token | Step 2: reserve-stock / release-stock (compensation) |
| `synkro-workflow` | `synkro-sales-api` | HTTP :8080 | Workflow service token | Step 3: register-sale |
| `synkro-worker` | `synkro-products-api` | HTTP :8080 | Worker service token | Low-stock job: list products, create/resolve alerts |

**No message broker in the MVP** — ADR-007 Decision 5 deferred RabbitMQ until an event has a consumer that a requirement needs (see `technical-backlog.md` TD-002).

**JWT validation:** every service validates the JWT locally with `synkro-auth-api`'s public key (ADR-006). The gateway only checks that a credential is present — it does not validate the token, and no `X-User-Id` or `X-User-Roles` headers are forwarded.

---

## Correlations

- Deployment topology, compose files and startup → `05-architecture/deployment.md`
- Data model per schema → `06-data/models.md`
- OpenAPI contracts → `07-api/contracts/openapi/`
- Architecture overview and C4 → `05-architecture/overview.md` §3, §4
- Saga, stock reservations and stock alerts → ADR-007
- Token validation and service tokens → ADR-006
- Technology stack decisions → ADR-008
- Schema-per-domain isolation → ADR-009
- Security policy and RBAC → `00-governance/security-policy.md`
- Bounded contexts → `02-domain/domain-map.md`