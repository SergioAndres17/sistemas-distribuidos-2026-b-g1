# ADRs — Architecture Decision Records

An ADR records one architectural decision: the problem, the options the team really considered, what tipped the decision, what it costs and what it changes. Each ADR is one file in `records/`.

## How to create an ADR

1. Copy `_template-adr.md` to `records/ADR-NNN-short-title.md`, using the next sequential number.
2. Fill in every section. An ADR needs at least two real options, a dominant criterion and an accepted cost; without the last two it is a statement, not a decision.
3. Open a `docs/` branch and a PR that contains the ADR and its row in the register below. The whole team reviews it.
4. While its status is `Proposed`, the ADR can still be edited.
5. Once `Accepted`, the ADR is never edited. A change is a new ADR that names the sections it replaces in its **Modifies** field, and the register records the link in both directions.

## Statuses

| Status | Meaning |
|--------|---------|
| `Proposed` | Under discussion; it can still be edited |
| `Accepted` | Approved by the team; immutable from this point on |
| `Rejected` | Evaluated and discarded; the ADR keeps the reason |
| `Superseded by ADR-NNN` | Every decision in it has been replaced by a newer ADR |

When a newer ADR replaces only some sections of an older one, the older ADR stays `Accepted`, and the register lists the replaced sections in the **Modified or superseded by** column.

## ADR register

| ID | Title | Status | Date | Modifies | Modified or superseded by | Builds on / Context |
|----|-------|--------|------|----------|---------------------------|---------------------|
| [ADR-001](records/ADR-001-architecture.md) | Sales Management System Architecture | Accepted | 2026-08 | — | §1 → ADR-008 → ADR-011; §2 → ADR-005 → ADR-009 → ADR-010 (Sales); §5 → ADR-003; §6 → ADR-003; §7 → ADR-002, ADR-005 → ADR-010 (Sales); §8 → ADR-004 | — |
| [ADR-002](records/ADR-002-sale-authorship-traceability.md) | Sale Authorship Traceability and `sales_summary` Status | Accepted | 2026-09 | ADR-001 §7 | Source of `created_by` → ADR-006; "Resolution of `sales_summary`" → ADR-005 | — |
| [ADR-003](records/ADR-003-gateway-saga-async.md) | API Gateway, Saga Workflow, and Async Messaging Adoption | Accepted | 2026-09 | ADR-001 §5, §6 | Decision 1 (gateway scope), Decision 2 → ADR-006; Decision 1 (routing…), Decisions 3, 4, 5 → ADR-007 | — |
| [ADR-004](records/ADR-004-api-contract-extensions.md) | API Contract Extensions | Accepted | 2026-09-28 | ADR-001 §8: catalog → extended; stock `PATCH` → replaced | — | ADR-006, ADR-007, ADR-008 |
| [ADR-005](records/ADR-005-data-isolation-per-domain.md) | Data Isolation and Data Model per Domain | Accepted | 2026-09-26 | ADR-001 §2, §7; ADR-002 "Resolution of `sales_summary`" | Decision 1 → ADR-009; Decisions 2, 3 and 4 (Sales only) → ADR-010; Decision 2 (extended: per-domain history table, rebuild check) → ADR-012 | — |
| [ADR-006](records/ADR-006-token-validation-per-service.md) | Token Validation in Every Service and Service Credentials | Accepted | 2026-09-27 | ADR-003 Decisions 1 and 2; ADR-002 (source of `created_by`) | — | ADR-013 (Auth confirmed as the system's cross-cutting security service, pending) |
| [ADR-007](records/ADR-007-persistent-saga-and-scheduled-work.md) | Persistent Saga Execution and Scheduled Work | Accepted | 2026-09-27 | ADR-003 Decisions 1, 3, 4 and 5 | Decision 1 (own instance) → ADR-009 | — |
| [ADR-008](records/ADR-008-cross-cutting-stack.md) | Technology Stack of the Cross-Cutting Repositories | Accepted | 2026-09-27 | None | Version table, "Databases and migrations" row → ADR-010; Decision 4 (every portal in React) and "Frontend" version row → ADR-011 | ADR-001 (versions), ADR-003 (stack gap) |
| [ADR-009](records/ADR-009-shared-instance-schema-per-domain.md) | Shared Instance with Schema-per-Domain Isolation | Accepted | 2026-09-28 | ADR-005 Decision 1; ADR-007 Decision 1 (own instance) | `sales_schema` (Sales leaves the shared instance) → ADR-010; repository name `synkro-infra` → `synkro-infra-postgres` (ADR-010); init script and connection credentials → ADR-012 | ADR-005 (reassessed trade-off), ADR-007 (workflow store) |
| [ADR-010](records/ADR-010-sales-on-mongodb.md) | Sales Domain on MongoDB | Accepted | 2026-10-03 | ADR-005 Decisions 2, 3 and 4 (Sales only); ADR-009 (`sales_schema`; repository name `synkro-infra`); ADR-008 version table ("Databases and migrations") | — | ADR-002 (live report aggregation, unchanged), ADR-007 (saga and frozen prices) |
| [ADR-011](records/ADR-011-angular-customers-portal.md) | Angular Customers Portal in the React Host | Accepted | 2026-10-03 | ADR-008 Decision 4 (every portal in React as a Module Federation remote) and its "Frontend" version row; ADR-001 §1 (single frontend framework) | — | ADR-006 (session and token validation) |
| [ADR-012](records/ADR-012-instance-bootstrap-and-environments.md) | Instance Bootstrap, Service Users and Environment Files | Accepted | 2026-10-03 | ADR-009 (init script that creates schemas and roles; per-domain connection credentials); ADR-005 Decision 2 (extended: migration history per domain, rebuild check) | — | ADR-010 (Sales leaves the shared instance; repository renamed to `synkro-infra-postgres`), ADR-007 (`workflow_schema`) |
| [ADR-013](records/ADR-013-identity-as-cross-cutting-security.md) | Identity Service as the Cross-Cutting Security Service | Proposed | — | — | — | ADR-006 (private key holder, token validation in every service), ADR-001 (identity and roles) |

Every new ADR adds its own row in the same PR, and updates the **Modified or superseded by** cell of each ADR it modifies.