# Stack-Specific Implementation Guides

> The concepts of DDD, hexagonal architecture, TDD and patterns in this repository
> do not depend on a language. This folder holds the **stack-specific guides**:
> the real folder structure, the commands, the libraries and code examples of the
> languages the project uses.
>
> The main documents tell you the **what and why**; your stack guide tells you the
> **concrete how**.

---

## The stacks of this project

The backend is written in Java and Go (ADR-001); the versions are pinned in
ADR-008.

| Stack | Guide | Services that use it |
|-------|-------|----------------------|
| Java 21 + Spring Boot 3.5, Maven | [java-spring.md](./java-spring.md) | `synkro-auth-api`, `synkro-customers-api`, `synkro-workflow` |
| Go | [go.md](./go.md) | `synkro-products-api`, `synkro-sales-api`, `synkro-worker` |

The gateway is NGINX with declarative configuration and has no guide. The frontend
host and three of the four portals (`synkro-front`, `synkro-auth-portal`,
`synkro-products-portal`, `synkro-sales-portal`) use React 19, TypeScript, Vite
and Node 22 LTS (ADR-008); `synkro-customers-portal` uses Angular 21 instead,
mounted as a custom element inside the React host (ADR-011). Neither frontend
framework has a guide yet — planned in HU-DOCS-85.

### Guides that do not apply

`node-typescript.md` and `python-fastapi.md` come from the documentation template.
No service of this project is written in Node.js or Python, so they are kept only
as reference and no document of the project relies on them. Their examples use the
template's domain, not SynkroTech's.

---

## How to use these guides

1. Find your service in the table above and open its guide.
2. Use the folder structure of the guide to create or read the repository: it is the
   structure expected for a `-api`, with the worker and the workflow as variants.
3. The code examples of the main documents (hexagonal architecture, TDD, patterns)
   are conceptual; the guide shows them with the real packages and names.
4. The detail of each service (responsibility, port, schema, contract) is in
   `09-microservices/service-catalog.md`.

---

## The example domain

The Go guide uses the **Products** catalog (`Product`, `StockAdjustment`) and the
Java guide uses **Customers** (`Customer`), both taken from
`02-domain/entities-and-rules.md`. The patterns are the same for the other
services; only the entities and their invariants change. `synkro-sales-api` is
also a Go service, but its persistence adapter uses the MongoDB driver instead of
`database/sql` — see `go.md`, "Sales: the same shape, a different persistence
adapter" (ADR-010).

---

## What each stack guide contains

- **Folder structure** of the microservice in that language, and where each layer lives
- **Dependencies** per layer (domain, application, adapters), and which ones are fixed by a decision
- **Configuration** through environment variables and the explicit limits
- **Code example** of a domain entity, its ports, a use case, an adapter and a test written first
- **Variant** for the other service of the same language (the worker or the workflow)
- **Commands** for development, tests and build
- **Naming conventions** of the language

---

## Correlations

- Hexagonal architecture → `05-architecture/hexagonal-architecture.md`
- Testing strategy → `11-quality/testing-strategy.md`
- Technology decisions → `05-architecture/decisions/records/ADR-008-cross-cutting-stack.md`
- Sales domain on MongoDB → `05-architecture/decisions/records/ADR-010-sales-on-mongodb.md`
- Angular Customers portal → `05-architecture/decisions/records/ADR-011-angular-customers-portal.md`
- Service catalog → `09-microservices/service-catalog.md`
