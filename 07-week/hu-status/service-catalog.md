# Microservices Catalog

> **What to fill in here:** The complete inventory of system services.
> For the detail of each service, go to `09-microservices/services/NN-service-name/`.
> This catalog is the executive view — one row per service.

---

## Services map

```
   synkro-front (shell) packages 4 domain UIs.
   All external traffic enters through the API Gateway.

                    ┌──────────────────────────────┐
                    │       synkro-api-gateway      │
                    │  JWT validation · routing ·   │
                    │  rate limiting · CORS          │
                    └──┬──────┬──────┬──────┬───────┘
                       │      │      │      │
         /api/auth/*   │      │      │      │  /api/sales (POST)
                       ▼      ▼      ▼      ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌─────────────────┐
│ auth-service │ │  customers-  │ │  products-   │ │ synkro-workflow │
│    :8081     │ │   service    │ │   service    │ │  Saga orch.     │
│ (Java/Spring)│ │    :8082     │ │    :8083     │ │  (stateless)    │
└──────────────┘ │ (Java/Spring)│ │    (Go)      │ └────┬──────┬────┘
                 └──────▲───────┘ └──────▲───────┘      │      │
                        │                │               │      │
                        │  internal      │  internal     │      │
                        └────────────────┴───────────────┘      │
                                                                │
                 ┌──────────────────────────────┐               │
                 │         sales-service         │◄──────────────┘
                 │            :8084 (Go)         │   internal
                 └──────────────┬────────────────┘
                                │
                    sales.outbox │ relay
                                ▼
                 ┌──────────────────────────────┐
                 │          RabbitMQ             │
                 │         (AMQP 5672)          │
                 └──────────────┬────────────────┘
                                │
                                ▼
                 ┌──────────────────────────────┐
                 │        synkro-worker          │
                 │  background jobs, no HTTP     │
                 └──────────────────────────────┘

         All domain services share one PostgreSQL instance,
         one schema per service: auth, customers, products, sales
         (own DB user per schema — see security-policy.md)
         Gateway, Workflow, and Worker have NO database.
```

---

## Service registry

| # | Service | Main responsibility | Port | Repo | DB | DB type | Status |
|---|---------|---------------------|------|------|-----|---------|--------|
| 01 | `auth-service` | Identity of internal users: credentials, roles, JWT (RS256) issuance | 8081 | `auth-service` | PostgreSQL — schema `auth` | Relational | 🔴 Planned |
| 02 | `customers-service` | Register, update, query, deactivate customers | 8082 | `customers-service` | PostgreSQL — schema `customers` | Relational | 🔴 Planned |
| 03 | `products-service` | Product catalog, categories, stock control | 8083 | `products-service` | PostgreSQL — schema `products` | Relational | 🔴 Planned |
| 04 | `sales-service` | Sale queries, report generation, Outbox relay (sale registration moved to `workflow`, ADR-003 §4) | 8084 | `sales-service` | PostgreSQL — schema `sales` | Relational | 🔴 Planned |
| 05 | `api-gateway` | Single entry point: JWT validation, routing, rate limiting, CORS | — | `synkro-api-gateway` | — | — | 🔴 Planned |
| 06 | `workflow` | Saga orchestration of multi-domain processes (sale registration) | — | `synkro-workflow` | — | — | 🔴 Planned |
| 07 | `worker` | Async background jobs: emails, recalculations, scheduled tasks | — | `synkro-worker` | — | — | 🔴 Planned |

**Statuses:** 🟢 Active in prod | 🟡 In development | 🔴 Planned | ⏸ Deprecated
All four are 🔴 Planned — the MVP monolith (separate `mvp-synkro-tech` repo, port 8080) is a throwaway spike and doesn't count as progress on these.

### Frontend & shared repositories

| Repo | Pairs with | Type | Branches |
|------|------------|------|----------|
| `synkro-auth-portal` | `synkro-auth-api` | React SPA | `main/qa/dev` |
| `synkro-customers-portal` | `synkro-customers-api` | React SPA | `main/qa/dev` |
| `synkro-products-portal` | `synkro-products-api` | React SPA | `main/qa/dev` |
| `synkro-sales-portal` | `synkro-sales-api` | React SPA | `main/qa/dev` |
| `synkro-front` | all 4 portals | Frontend shell, packages the 4 domain UIs | `main/qa/dev` |
| `synkro-auth-db` | `synkro-auth-api` | Migrations for schema `auth` | `main/qa/dev` |
| `synkro-customers-db` | `synkro-customers-api` | Migrations for schema `customers` | `main/qa/dev` |
| `synkro-products-db` | `synkro-products-api` | Migrations for schema `products` | `main/qa/dev` |
| `synkro-sales-db` | `synkro-sales-api` | Migrations for schema `sales` (+ `outbox`) | `main/qa/dev` |
| `synkro-infra` | all services | Shared PostgreSQL instance init script, docker-compose, RabbitMQ | `main/qa/dev` |
| `synkro-docs` | — | This documentation | `main` only |

**Note on naming:** the professor's repositories use the `-api` suffix (`synkro-auth-api`) while this catalog and `overview.md` use the `-service` suffix (`auth-service`) for the same thing. Both names refer to the same service — this is a naming-convention gap the team should resolve (pick one, use it everywhere), but it does not block HU-ARQ-14.

---

## Detail per service

### 01 — `auth-service`

| Field | Value |
|-------|-------|
| **Folder** | `09-microservices/services/01-auth-service/` |
| **Responsibility** | Manages the identity of SynkroTech SAS's internal users: credentials, roles, JWT (RS256) issuance and refresh |
| **Type** | Supporting service (cross-cutting, not a core business process) |
| **Port** | 8081 |
| **Technology** | Java (Spring Boot) |
| **DB** | PostgreSQL, shared instance — schema `auth`, own `auth_user` credential |
| **Bounded Context** | `02-domain/domain-map.md` → Authentication and Users |
| **Externally exposed** | Yes — called directly by all 4 frontends for login. Called by no other backend service: the other 3 validate its JWT locally instead |

**Key endpoints:** to be completed in `09-microservices/services/01-auth-service/README.md` (future HU)
**Events published/consumed:** none planned for the MVP

---

### 02 — `customers-service`

| Field | Value |
|-------|-------|
| **Folder** | `09-microservices/services/02-customers-service/` |
| **Responsibility** | Register, update, query, and deactivate customers |
| **Type** | Core service |
| **Port** | 8082 |
| **Technology** | Java (Spring Boot) |
| **DB** | PostgreSQL, shared instance — schema `customers`, own `customers_user` credential |
| **Bounded Context** | `02-domain/domain-map.md` → Customers |
| **Externally exposed** | Yes — called via the Gateway by its frontend. Called internally by `synkro-workflow` to validate a customer's existence/active status (Saga step 1, ADR-003 §4) |

**Key endpoints:** to be completed in `09-microservices/services/02-customers-service/README.md` (future HU)

---

### 03 — `products-service`

| Field | Value |
|-------|-------|
| **Folder** | `09-microservices/services/03-products-service/` |
| **Responsibility** | Product catalog, categories, and stock control |
| **Type** | Core service |
| **Port** | 8083 |
| **Technology** | Go |
| **DB** | PostgreSQL, shared instance — schema `products`, own `products_user` credential |
| **Bounded Context** | `02-domain/domain-map.md` → Products and Inventory |
| **Externally exposed** | Yes — called via the Gateway by its frontend. Called internally by `synkro-workflow` to reserve/release stock (Saga step 2 + compensation, ADR-003 §4) |

**Key endpoints:** to be completed in `09-microservices/services/03-products-service/README.md` (future HU)

---

### 04 — `sales-service`

| Field | Value |
|-------|-------|
| **Folder** | `09-microservices/services/04-sales-service/` |
| **Responsibility** | Owns the `sales` schema: serves sale queries and report generation; relays Outbox events to RabbitMQ. Sale *registration* is orchestrated by `synkro-workflow`, not by this service (ADR-003 §4) |
| **Type** | Core service |
| **Port** | 8084 |
| **Technology** | Go |
| **DB** | PostgreSQL, shared instance — schema `sales`, own `sales_user` credential |
| **Bounded Context** | `02-domain/domain-map.md` → Sales |
| **Externally exposed** | Yes — called via the Gateway for reads/reports by its frontend, and internally by `synkro-workflow` to register a sale (Saga step 3, ADR-003 §4). Makes no outgoing calls to other domain services — it only receives calls and relays to RabbitMQ |

**Key endpoints:** to be completed in `09-microservices/services/04-sales-service/README.md` (future HU)

---

### 05 — `api-gateway`

| Field | Value |
|-------|-------|
| **Responsibility** | Single external entry point: JWT validation (RS256), request routing, rate limiting, CORS |
| **Type** | Infrastructure service |
| **Port** | TBD (assigned when code phase begins) |
| **Technology** | TBD |
| **DB** | None — stateless |
| **Externally exposed** | Yes — the only service reachable from the outside |

**Key behavior:** validates the JWT once, extracts `sub`/`roles`/`permissions`, and forwards them as `X-User-Id` and `X-User-Roles` headers to the target domain service. Domain services trust these headers without re-validating the token. This is secure only because the Docker network prevents external traffic from reaching domain services directly — see `deployment.md`.

**Events published/consumed:** none

---

### 06 — `workflow`

| Field | Value |
|-------|-------|
| **Responsibility** | Orchestrate multi-domain business processes using the Saga pattern |
| **Type** | Orchestrator |
| **Port** | TBD (assigned when code phase begins) |
| **Technology** | TBD |
| **DB** | None — stateless orchestrator, no persisted saga state |
| **Externally exposed** | No — only reachable through the API Gateway (`POST /api/sales` is routed here) |

**Current Sagas:** `SaleRegistrationSaga` — validates customer (customers-api), reserves stock (products-api), registers sale (sales-api). See ADR-003 §4 for the full sequence.

**Events published/consumed:** none directly — the final event (`SaleCompleted`/`SaleFailed`) is published by `sales-api` via its Outbox, not by `workflow` itself.

---

### 07 — `worker`

| Field | Value |
|-------|-------|
| **Responsibility** | Process asynchronous background jobs |
| **Type** | Background consumer |
| **Port** | None — does not expose HTTP |
| **Technology** | TBD |
| **DB** | None — acts on domain services via their APIs or performs external actions (emails) |
| **Externally exposed** | No |

**Consumes from RabbitMQ:** `SaleCompleted`, `SaleFailed` (published by `sales-api`'s Outbox relay).

**Example jobs:** send sale confirmation email, recalculate `sales_summary` projection, scheduled reports (FR-008, FR-009 if moved to background).

**Events published/consumed:** consumes only — does not publish events back.

---

## Service communication matrix

| Source service | Destination service | Channel | Type | Description |
|-----------------|---------------------|---------|------|--------------|
| sales-service | customers-service | HTTP/REST | Synchronous | `GET /api/customers/{id}` at read time — resolves the customer's name when displaying a sale's detail |
| sales-service | products-service | HTTP/REST | Synchronous | `GET /api/products/{id}` at read time, per line — resolves each product's name when displaying a sale's detail |
| workflow | customers-api | HTTP/REST | Synchronous (internal) | `GET /api/customers/{id}` — Saga step 1: validate customer active |
| workflow | products-api | HTTP/REST | Synchronous (internal) | `PATCH /api/products/{id}/stock` — Saga step 2: reserve stock; compensation: release stock |
| workflow | sales-api | HTTP/REST | Synchronous (internal) | `POST /api/sales` — Saga step 3: register sale + outbox event |
| sales-api (relay) | RabbitMQ | AMQP | Asynchronous | Outbox relay publishes `SaleCompleted` / `SaleFailed` |
| worker | RabbitMQ | AMQP | Asynchronous | Consumes `SaleCompleted` / `SaleFailed` for background jobs |

**Note on JWT validation:** since ADR-003 §2, the API Gateway validates the RS256 JWT issued by `auth-service` **once**, and forwards trusted `X-User-Id`/`X-User-Roles` headers to the target domain service. Domain services no longer validate the JWT themselves. This isn't a row in the matrix above because it isn't a service-to-service call in the traditional sense — it's the Gateway's own routing behavior. See ADR-003 §2 and `05-architecture/deployment.md`, "Network Guarantee", for the security condition this depends on.

**Accepted N+1 limitation (MVP):** listing many sales with customer/product names costs one HTTP call per related entity. Accepted for the MVP — a batch-lookup endpoint or a small read cache in `sales-service` are the two options already on the table if this becomes a real bottleneck (see `02-domain/entities-and-rules.md`).

---

## Data ownership matrix

| Entity / Data | Owner service (Source of Truth) | How other services access |
|-----------------|----------------------------------|------------------------------|
| User / credentials / roles | auth-service | Not accessed by other services directly — the API Gateway validates the JWT and forwards trusted headers instead (ADR-003 §2) |
| Customer | customers-service | REST API (`GET /api/customers/{id}`) — called by `synkro-workflow` (Saga step 1) and by `sales-service` (read-time display only) |
| Product, Category | products-service | REST API (`GET /api/products/{id}`, `PATCH /api/products/{id}/stock`) — called by `synkro-workflow` (Saga step 2 + compensation) and by `sales-service` (read-time display only) |
| Sale, SaleDetail | sales-service | REST API (`GET /api/sales`, `GET /api/sales/{id}`) — called by its own frontend only |
| SalesSummary (reports) | sales-service (derived, not stored) | Calculated on demand via `SUM`/`GROUP BY` over Sale/SaleDetail — not an independent entity |

---

## Correlations

- Architecture and patterns → `05-architecture/overview.md`, ADR-001
- Domain source for this catalog → `02-domain/domain-map.md`, `02-domain/entities-and-rules.md`
- API contracts per service → `07-api/contracts/openapi/` (not yet created — next in the SDD fill-in order)
- Full detail per service → `09-microservices/services/NN-[name]/` (deferred to a future HU)
