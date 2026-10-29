# User Stories — Backlog

> **What to fill in here:** The product's User Story backlog.
> Each HU uses the standard format with Acceptance Criteria in Given/When/Then.
> Refined (Ready) HUs go to the sprint. Unrefined ones are epics or ideas.

---

## Backlog status

| Cut | Sprint | Total HUs | Refined | In progress | Completed |
|-----|--------|-----------|---------|-------------|-----------|
| Cut 1 | Sprint 1-3 (docs discovery) | 22 | 22 | 0 | 22 |
| Cut 2 | Sprint 4 (catalog + product def. + panoramic MVP) | 5 | 5 | 0 | 5 |
| Cut 3 | Sprint 5-7 (architecture decisions, domain events, UML, review corrections) | 15 | 15 | 0 | 15 |
| Cut 4 | Sprint 8-9 (DB topology correction, API contracts rewrite, governance alignment, pre-code readiness) | 52 | 52 | 0 | 52 |
| Cut 5 | Sprint 9-10 (two database engines, Angular Customers portal — ADR-010 to ADR-012 and downstream documents) | 13 | 13 | 0 | 13 |
| Cut 6 | Sprint 10-11 (assigned first-code-delivery stories) | 16 | 16 | 1 | 15 |
| Cut 7 | Week 10 (Products Portal completion) | 3 | 3 | 0 | 3 |
| Cut 8 | Additional implementation delivery; no calendar redistribution | 39 | 39 | 0 | 39 |
| Cut 9 | Sprint 12 — Week 12 (documentation and architecture reconciliation) | 5 | 5 | 4 | 1 |

> *In progress* counts open assigned HUs, started or not. The implementation
> snapshot contains 58 assigned issues: 57 closed and HU-INF-02/#239 open.
> The original Cut 6 plan contained 19 slots; VEN-01/02 and INF-03 did not
> become assigned issues and are not counted as closed implementation HUs.
> A closed issue is recorded separately from its remaining acceptance limits.
>
> Cuts group scope; they do not replace calendar weeks. HU-DOCS-94,
> HU-DOCS-95, HU-ARQ-28, HU-DOCS-96 and HU-DOCS-97 belong to Week 12.
> Earlier weekly totals are not inferred from issue creation or release dates.
> Live Project Status/Environment values are maintained on the [board](https://github.com/orgs/code-corhuila/projects/24);
> this preparation does not claim to have inspected or changed those fields.

---

## Cut 3 — Documentation Track (Weeks 5-7)

> These HUs follow the documentation formalization workflow, not the
> code sprint format below. Each one already has its full specification
> (User Story + Gherkin Acceptance Criteria + DoD) written directly in
> its GitHub Issue — repeating it here would duplicate content, which
> this file's own Definition of Done prohibits. This table exists only
> to satisfy that same rule's requirement to mark each HU complete here.

| HU ID | Title | Epic | Status | Resolved in |
|---|---|---|---|---|
| HU-ARQ-09 | Publish ADR-001 | HU-04 | Done | `ADR-001-architecture.md` |
| HU-ARQ-12 | Deployment + threat model | HU-04 | Done | `deployment.md`, `security-threat-model.md` |
| HU-ARQ-13 | ADR-002 — sale authorship | HU-04 | Done | `ADR-002-sale-authorship-traceability.md` |
| HU-DOCS-25/26 | Unify FR/NFR, disconnect PDR | HU-05 (#5) | Done | 13 files, incl. `navigation-map.md` |
| HU-DOCS-27 | Rebuild traceability matrix | HU-05 (#5) | Done | `traceability-matrix.md` |
| HU-DOCS-28 | Align git-conventions.md | #15 | Done | `git-conventions.md`, `documentation-rules.md` |
| HU-ARQ-14 | ADR-003 + downstream | HU-06 (#19) | Done | `ADR-003-gateway-saga-async.md` + 5 files |
| HU-ARQ-15 | Hexagonal architecture guide | HU-06 (#19) | Done | `hexagonal-architecture.md` |
| HU-DOCS-29 | Fill domain-events.md | HU-07 (#22) | Done | `domain-events.md` |
| HU-DOCS-30 | Add BPMN/C4 diagrams | HU-07 (#22) | Done | `diagram-index.md` + 10 files |
| HU-DOCS-31 | Cite PR #17 in NFR record | HU-08 (#31) | Done | `non-functional.md` |
| HU-DOCS-33 | C4 sync cross-reference | HU-08 (#31) | Done | `overview.md` |
| HU-DOCS-32 | Governance corrections | HU-08 (#31) | Done | `definition-of-done.md`, `definition-of-ready.md`, `user-stories.md` (Cut 3 section) |
| HU-ARQ-16 | Java appendix + `_stacks/` refs | HU-08 (#31) | Done | `hexagonal-architecture.md` (Java code moved to appendix, Go-only main body) |

> HU-DOCS-32 and HU-ARQ-16 belong to HU-08 (#31) and stay in this table with their epic; they were executed in week 8.
> HU-DOCS-25 and HU-DOCS-26 share one row, so this table has 14 rows for 15 HUs: 3 (weeks 5-6) + 10 (week 7) + 2 (week 8).

---

## Cut 4 — Documentation Track (Weeks 8-9)

> Weeks 8-9 continued the architecture decision formalization (HU-10)
> and completed the first-pass API contracts (HU-09). Week 9
> corrected the database topology after the professor clarified that
> the model is one instance per environment with schemas per domain
> (ADR-009 supersedes ADR-005 Decision 1), rewrote the Products, Sales
> and Workflow contracts from scratch (HU-11) and aligned the
> governance, context, requirements and architecture documents
> (HU-12). HU-DOCS-55 and HU-DOCS-56 were repurposed from API
> contracts to DB topology corrections; their original scope, plus the
> follow-ups found while closing HU-11 and HU-12, is scheduled in HU-13.

### HU-08 — Address Professor Review Feedback (continued)

| HU ID | Title | Status | Resolved in |
|---|---|---|---|
| HU-DOCS-34 | Update stale status references in context and vision | Done | `01-context/overview.md`, `03-product/vision.md` |
| HU-DOCS-41 | Fix pre-ADR-003 JWT and CORS model in security-policy and cross-cutting | Done | `security-policy.md`, `cross-cutting.md` |

### HU-09 — API Contracts (first pass, pre-ADR-004)

| HU ID | Title | Status | Resolved in |
|---|---|---|---|
| HU-DOCS-35 | REST guidelines and authentication strategy | Done | `07-api/guidelines.md`, `07-api/authentication.md` |
| HU-DOCS-36 | Shared components and auth contract | Done | `07-api/contracts/openapi/_shared.yaml`, `synkro-auth-api.yaml` |
| HU-DOCS-37 | Customers contract | Done | `synkro-customers-api.yaml` |
| HU-DOCS-38 | Products contract (first pass) | Done | `synkro-products-api.yaml` (superseded by HU-DOCS-57) |
| HU-DOCS-39 | Sales contract (first pass) | Done | `synkro-sales-api.yaml` (superseded by HU-DOCS-59) |
| HU-DOCS-40 | Workflow contract (first pass) | Done | `synkro-workflow.yaml` (superseded by HU-DOCS-60) |
| HU-DOCS-42 | ADR-004 — API contract extensions (proposed) | Done | `ADR-004-api-contract-extensions.md` (accepted in HU-DOCS-54) |

### HU-10 — Architecture Decisions

| HU ID | Title | Status | Resolved in |
|---|---|---|---|
| HU-ARQ-17 | Add dominant criterion and accepted cost to ADR template | Done | `_template-adr.md`, `decisions/README.md` |
| HU-ARQ-18 | ADR-005 — data isolation and data model per domain | Done | `ADR-005-data-isolation-per-domain.md` (Decision 1 superseded by ADR-009) |
| HU-ARQ-19 | ADR-006 — token validation in every service | Done | `ADR-006-token-validation-per-service.md` |
| HU-ARQ-20 | ADR-007 — persistent saga execution and scheduled work | Done | `ADR-007-persistent-saga-and-scheduled-work.md` |
| HU-ARQ-21 | ADR-008 — cross-cutting repository stack | Done | `ADR-008-cross-cutting-stack.md` |
| HU-DOCS-43 | Update domain model, events and glossary | Done | `entities-and-rules.md`, `domain-events.md`, `glossary.md` |
| HU-DOCS-44 | Rewrite deployment: composition, DB and migrations | Done | `deployment.md` (superseded by HU-DOCS-55 for DB topology) |
| HU-DOCS-45 | Rewrite deployment: configuration, dev identity, observability | Done | `deployment.md` §6-§8 |
| HU-DOCS-46 | Rewrite auth and customers data models | Done | `models.md` (auth, customers sections) |
| HU-DOCS-47 | Rewrite products, sales and saga store data models | Done | `models.md` (products, sales, workflow sections) |
| HU-DOCS-48 | Align security policy and authentication strategy | Done | `security-policy.md`, `authentication.md` |
| HU-DOCS-49 | Update security threat model | Done | `security-threat-model.md` (superseded by HU-DOCS-56 for DB topology) |
| HU-DOCS-50 | Realign cross-cutting concerns | Done | `cross-cutting.md` (superseded by HU-DOCS-56 for DB topology) |
| HU-DOCS-51 | Update architecture overview and pattern guide | Done | `overview.md`, `pattern-guide.md` (superseded by HU-DOCS-65 for DB topology) |
| HU-DOCS-52 | Update C4 container and sale registration diagrams | Done | `c4-02-containers.drawio`, `bpmn-sales-registration.drawio` |

### HU-11 — API Contracts and DB Topology Correction

| HU ID | Title | Status | Resolved in |
|---|---|---|---|
| HU-DOCS-53 | Define common REST contract, guidelines and `_shared.yaml` | Done | `07-api/guidelines.md`, `_shared.yaml` |
| HU-DOCS-54 | Extend and accept ADR-004 with the full endpoint register | Done | `ADR-004-api-contract-extensions.md` (accepted) |
| HU-ARQ-22 | ADR-009 — shared instance with schema-per-domain isolation | Done | `ADR-009-shared-instance-schema-per-domain.md`, `decisions/README.md` |
| HU-DOCS-55 | Correct deployment and data models for shared-instance topology | Done | `deployment.md` §1-§9, `models.md` header + P1 + domain headers. **Repurposed** from auth API contract; original scope deferred to HU-DOCS-72 |
| HU-DOCS-56 | Correct security and cross-cutting docs for shared-instance topology | Done | `security-threat-model.md`, `security-policy.md`, `cross-cutting.md`. **Repurposed** from customers API contract; original scope deferred to HU-DOCS-73 |
| HU-DOCS-57 | Align Products API contract — catalog, categories and stock adjustments | Done | `synkro-products-api.yaml` (full rewrite) |
| HU-DOCS-58 | Add Products API contract — stock reservations and stock alerts | Done | `synkro-products-api.yaml` (reservation and alert endpoints added) |
| HU-DOCS-59 | Align Sales API contract | Done | `synkro-sales-api.yaml` (full rewrite) |
| HU-DOCS-60 | Align Workflow and saga contract | Done | `synkro-workflow.yaml` (full rewrite) |

### HU-12 — Governance and Documentation

| HU ID | Title | Status | Resolved in |
|---|---|---|---|
| HU-DOCS-61 | Adopt component naming convention and rename contract files | Done | `documentation-rules.md`, contract file renames |
| HU-DOCS-62 | Amend DoD and branch conventions | Done | `definition-of-done.md`, `git-conventions.md`, `documentation-rules.md` |
| HU-DOCS-63 | Reconcile backlog by semester cut | Done | `user-stories.md` (this section), `agile-conventions.md` |
| HU-DOCS-64 | Fill risk register and tech backlog | Done | `risks.md`, `technical-backlog.md` |
| HU-DOCS-65 | Align context and product documents + correct overview DB topology | Done | `overview.md` (P2, AT-002, §4, C4 Mermaid), `01-context/overview.md` |
| HU-DOCS-66 | Align requirements and contract traceability + correct NFR-007 | Done | `non-functional.md`, `functional.md`, `traceability-matrix.md` |
| HU-DOCS-67 | Define testing strategy for Java and Go | Done | `testing-strategy.md` |
| HU-DOCS-68 | Align hexagonal architecture guide with service layout | Done | `hexagonal-architecture.md` |
| HU-DOCS-69 | Align diagram index and UX flows + correct C4 for shared instance | Done | `diagram-index.md`, `navigation-map.md`, `c4-02-containers.drawio` |
| HU-DOCS-70 | Rewrite service catalog + correct database column | Done | `service-catalog.md` |

### HU-13 — Contract completion and documentation readiness for the code phase

| HU ID | Title | Status | Resolved in |
|---|---|---|---|
| HU-DOCS-71 | Rename DDL schema prefixes to `<domain>_schema` in `models.md` | Done | — |
| HU-DOCS-72 | Rewrite the Auth API contract and align `authentication.md` | Done | — |
| HU-DOCS-73 | Rewrite the Customers API contract | Done | — |
| HU-DOCS-74 | Align the Go and Java stack guides with the project decisions | Done | `_stacks/go.md`, `_stacks/java-spring.md` |
| HU-DOCS-75 | Align `domain-map.md` with the saga and the workflow | Done | `02-domain/domain-map.md` |
| HU-DOCS-76 | Sweep stale references in context and governance documents | Done | — |
| HU-DOCS-77 | Close Cut 4 in the backlog and fix pending references | Done | — |
| HU-DOCS-78 | Complete the ⭐ documents of `09-microservices/` and `15-project-control/` | Done | — |
| HU-DOCS-79 | Adapt contributing, onboarding, TDD and `10-devops/` documents | Done | — |

HU-DOCS-71 to HU-DOCS-73 were deferred from HU-DOCS-55 and HU-DOCS-56 (HU-DOCS-71 was identified while closing HU-DOCS-55); they are scheduled here.

### HU-14 — Two database engines, mixed-framework frontend and code-phase backlog

| HU ID | Title | Status | Resolved in |
|---|---|---|---|
| HU-ARQ-23 | ADR-010 — Sales domain on MongoDB and the MongoDB infrastructure repository | Done | `ADR-010-sales-on-mongodb.md` |
| HU-ARQ-24 | Spike and ADR-011 — Angular customers portal inside the React host | Done | `ADR-011-angular-customers-portal.md` |
| HU-ARQ-25 | ADR-012 — instance bootstrap, service users and environment files | Done | `ADR-012-instance-bootstrap-and-environments.md` |
| HU-ARQ-26 | ADR-013 — the identity service as the cross-cutting security service | Done — the story itself (record ADR-013, ask the instructor) is complete. ADR-013's own status stays `Proposed`, pending the instructor's answer, which does not keep this story open | `ADR-013-identity-as-cross-cutting-security.md` |
| HU-DOCS-80 | Rewrite the Sales data model as documents | Done | `06-data/models.md`, `data-dictionary.md`, `02-domain/entities-and-rules.md`, `09-microservices/data-ownership-matrix.md`, `01-context/glossary.md`, `synkro-sales-api.yaml`, `synkro-workflow.yaml` |
| HU-DOCS-81 | Update deployment for two engines and per-environment files | Done | `05-architecture/deployment.md`, `cross-cutting.md`, `10-devops/` |
| HU-DOCS-82 | Align overview, context, service catalog and C4 | Done | `05-architecture/overview.md`, `01-context/overview.md`, `01-context/scope.md`, `09-microservices/service-catalog.md`, `dependency-map.md`, `_stacks/README.md`, `_stacks/go.md`, `08-uml/diagram-index.md`, `c4-02-containers.drawio` |
| HU-DOCS-83 | Align security documents and non-functional requirements | Done | `00-governance/security-policy.md`, `security-rules.md`, `05-architecture/security-threat-model.md`, `04-requirements/non-functional.md` |
| HU-DOCS-84 | Extend the testing strategy for frontend, MongoDB and mocks | Done | `11-quality/testing-strategy.md`, `tdd-guide.md` |
| HU-DOCS-85 | Add the frontend stack guide and shared design tokens | Done | `_stacks/frontend.md` (new), `12-ux-ui/design-system.md` |
| HU-DOCS-86 | Assign screens to portals and define view states | Done | `12-ux-ui/navigation-map.md`, `wireframes.md` |
| HU-DOCS-87 | Add the code-phase backlog, story prefixes and traceability | Done | `agile-conventions.md`, `user-stories.md`, `traceability-matrix.md` |
| HU-DOCS-88 | Update open questions, risks, dependencies and technical backlog | Closed — see linked issue | https://github.com/code-corhuila/synkro-docs/issues/176 |

---

## Cut 5 — Two Database Engines and the Angular Customers Portal (Weeks 9-10)

> See the HU-14 table above for the 13 stories and their current status.
> This cut corrects the single-PostgreSQL-instance model to two engines
> (ADR-010) and introduces the Angular Customers portal (ADR-011), both
> required by the course architecture; ADR-012 and ADR-013 are the
> operational decisions that correction needed.

---

## Cut 6 — First Code Delivery (Weeks 10-11)

> Code stories. Each one has its full specification (story, acceptance
> criteria with at least one error scenario, technical notes, first test to
> write, dependencies and points) in its GitHub Issue; this table marks its
> status. Every code pull request references its story as
> `code-corhuila/synkro-docs#<issue>`. The test is written before the code
> it verifies (`11-quality/tdd-guide.md`).
>
> Delivery: the assigned Host, Gateway, service and database stories are
> reconciled below. Real Sales, Auth and Customers evidence now supersedes
> the initial simulated-service plan. Current CI and root acceptance remain
> separately recorded; an issue's closed state is not a green-check claim.

| HU ID | Title | Repository | Points | Priority | Status |
|---|---|---|---|---|---|
| HU-INF-01 | Common files in every code repository | all code repositories | 3 | Must | Closed — [#164](https://github.com/code-corhuila/synkro-docs/issues/164) |
| HU-INF-02 | Persistent PostgreSQL Infrastructure and SynkroTech Root Composition | `synkro-infra-postgres` | 5 | Must | Open — [#239](https://github.com/code-corhuila/synkro-docs/issues/239) |
| HU-INF-03 | Simulated services from the contracts | `synkro-infra-postgres` | 3 | Must | Unassigned plan; no cancellation inferred |
| HU-GTW-01 | Gateway routes and its own behavior | `synkro-api-gateway` | 3 | Must | Closed — [#182](https://github.com/code-corhuila/synkro-docs/issues/182) |
| HU-FE-03 | Host: single HTTP client, session and development sign-in | `synkro-front` | 5 | Must | Closed — [#183](https://github.com/code-corhuila/synkro-docs/issues/183) |
| HU-FE-04 | Host: portal registry, isolation, layout and not found | `synkro-front` | 3 | Must | Closed — [#188](https://github.com/code-corhuila/synkro-docs/issues/188) |
| HU-PRO-01 | Products portal: list products | `synkro-products-portal` | 3 | Must | Closed — [#202](https://github.com/code-corhuila/synkro-docs/issues/202) |
| HU-PRO-02 | Products portal: register a product | `synkro-products-portal` | 3 | Must | Closed — [#205](https://github.com/code-corhuila/synkro-docs/issues/205) |
| HU-VEN-01 | Sales portal: register a sale | `synkro-sales-portal` | 5 | Must | Unassigned plan; delivered scope HU-VEN-12/#246 |
| HU-VEN-02 | Sales portal: sales history | `synkro-sales-portal` | 3 | Could | Unassigned plan; delivered scope HU-VEN-11/#241 |
| HU-AUTH-01 | Base structure of the Auth service | `synkro-auth-api` | 3 | Should | Closed — [#184](https://github.com/code-corhuila/synkro-docs/issues/184) |
| HU-CLI-01 | Base structure of the Customers service | `synkro-customers-api` | 3 | Should | Closed — [#187](https://github.com/code-corhuila/synkro-docs/issues/187) |
| HU-PRO-03 | Base structure of the Products service | `synkro-products-api` | 2 | Should | Closed — [#181](https://github.com/code-corhuila/synkro-docs/issues/181) |
| HU-VEN-03 | Base structure of the Sales service | `synkro-sales-api` | 2 | Should | Closed — [#186](https://github.com/code-corhuila/synkro-docs/issues/186) |
| HU-WKF-01 | Base structure of the workflow | `synkro-workflow` | 3 | Should | Closed — [#185](https://github.com/code-corhuila/synkro-docs/issues/185) |
| HU-WRK-01 | Base structure of the worker | `synkro-worker` | 2 | Should | Closed — [#189](https://github.com/code-corhuila/synkro-docs/issues/189) |
| HU-AUTH-02 | Base structure of the Auth database repository | `synkro-auth-db` | 2 | Should | Closed — [#196](https://github.com/code-corhuila/synkro-docs/issues/196) |
| HU-CLI-02 | Customers schema and migration runner | `synkro-customers-db` | 2 | Should | Closed — [#244](https://github.com/code-corhuila/synkro-docs/issues/244) |
| HU-PRO-04 | Base structure of the Products database repository | `synkro-products-db` | 2 | Should | Closed — [#191](https://github.com/code-corhuila/synkro-docs/issues/191) |

The table retains the original estimates as planning data. Its 16 assigned issues contain 15 closed and one open; the three unassigned plans do not consume IDs or establish delivered points. No weekly velocity is inferred from these totals.

**Not in this cut, and why:**

| Repository | Follow-up |
|---|---|
| `synkro-customers-portal` | HU-CLI-04/#249 delivered the routed Angular portal; R-008 reassessment belongs to DOCS-97 |
| `synkro-auth-portal` | HU-FE-08/#248 delivered real login through the Host; administration UI is separate |

**Next cut — delivery and remaining scope:**

| Planned story | Prefix | Requirement |
|---|---|---|
| Customer lifecycle, API and portal: HU-CLI-02/03/04 delivered | `CLI` | FR-001 |
| Real RS256 login/refresh and authorization delivered; session policy formalization remains ARQ-28 | `AUTH`, `FE` | FR-010 |
| Saga, persistence and Sales registration/history/detail delivered; root acceptance remains INF-02 | `WKF`, `VEN` | FR-005, FR-006, FR-007 |
| HU-VEN-10 delivers report APIs; reporting/dashboard UI remains Must Have pending implementation planning | `VEN` | FR-008, FR-009 |
| Low-stock business job remains missing; SantiagoPlazas2005 is the proposed planning coordinator | `WRK` | FR-004 |
| Stock-alert and sale-detail screens exist; Users and Service tokens administration screens remain separate | `PRO`, `VEN`, `AUTH` | FR-004, FR-010 |
| Real Mongo adapter/migration evidence exists; model drift is owned by DOCS-95 | `VEN` | FR-005, FR-006, FR-007 |

---

## Cut 7 — Products Portal Completion (Week 10)

> Closes the "Product catalog on the real service" item planned in Cut 6's
> "Next cut" table — superseded by these three stories.

| HU ID | Title | Repository | Points | Priority | Status |
|---|---|---|---|---|---|
| [HU-PRO-11](https://github.com/code-corhuila/synkro-docs/issues/200) | Products portal: base structure (Module Federation remote, shared HTTP client, registration in `synkro-front`, CI, failure-isolation test) | `synkro-products-portal` | ? | Must | Closed |
| [HU-PRO-12](https://github.com/code-corhuila/synkro-docs/issues/206) | Products portal: edit, adjust stock and deactivate products; categories section (create, rename, deactivate) | `synkro-products-portal` | ? | Must | Closed |
| [HU-PRO-13](https://github.com/code-corhuila/synkro-docs/issues/204) | Products portal: list design (summary cards, table with badges, skeleton, states and filters), all in Spanish | `synkro-products-portal` | ? | Should | Closed |

---

## Cut 8 — Additional Implementation Delivery

> These 39 assigned implementation issues were absent from the earlier
> first-delivery view. Source, test and acceptance references are in the
> [traceability matrix](traceability-matrix.md#fr--hu--test--service-matrix).
> Estimates and individual calendar weeks are not invented by this reconciliation.

| HU ID | Title | Repository | Points | Priority | Status |
|---|---|---|---|---|---|
| [HU-WKF-02](https://github.com/code-corhuila/synkro-docs/issues/190) | Fix architecture-check guard and UTF-8 test coverage | `synkro-workflow` | Per linked issue | Per linked issue | Closed; JordanRG420 |
| [HU-PRO-05](https://github.com/code-corhuila/synkro-docs/issues/192) | Category and Product tables | `synkro-products-db` | Per linked issue | Per linked issue | Closed; SergioAndres17 |
| [HU-PRO-06](https://github.com/code-corhuila/synkro-docs/issues/193) | Stock adjustment and reservation tables, plus the idempotency key table | `synkro-products-db` | Per linked issue | Per linked issue | Closed; SergioAndres17 |
| [HU-PRO-07](https://github.com/code-corhuila/synkro-docs/issues/194) | Stock alert table | `synkro-products-db` | Per linked issue | Per linked issue | Closed; SergioAndres17 |
| [HU-PRO-08](https://github.com/code-corhuila/synkro-docs/issues/195) | Categories and Products endpoints | `synkro-products-api` | Per linked issue | Per linked issue | Closed; JordanRG420 |
| [HU-AUTH-03](https://github.com/code-corhuila/synkro-docs/issues/197) | Auth issues real RS256-signed tokens | `synkro-auth-api` | Per linked issue | Per linked issue | Closed; AsolanoT |
| [HU-PRO-09](https://github.com/code-corhuila/synkro-docs/issues/198) | Stock adjustment and reservation endpoints | `synkro-products-api` | Per linked issue | Per linked issue | Closed; JordanRG420 |
| [HU-PRO-10](https://github.com/code-corhuila/synkro-docs/issues/199) | Stock-alert endpoints | `synkro-products-api` | Per linked issue | Per linked issue | Closed; JordanRG420 |
| [HU-FE-05](https://github.com/code-corhuila/synkro-docs/issues/201) | Host client: query parameters, idempotency key and caller cancellation | `synkro-front` | Per linked issue | Per linked issue | Closed; AsolanoT |
| [HU-FE-06](https://github.com/code-corhuila/synkro-docs/issues/203) | Host: design tokens and a ready `synkro-front` | `synkro-front` | Per linked issue | Per linked issue | Closed; AsolanoT |
| [HU-AUTH-04](https://github.com/code-corhuila/synkro-docs/issues/208) | `system_user` Table and `auth_reader` Role | `synkro-auth-db` | Per linked issue | Per linked issue | Closed; SergioAndres17 |
| [HU-AUTH-05](https://github.com/code-corhuila/synkro-docs/issues/209) | `refresh_token` table, foreign key to `system_user` and its index | `synkro-auth-db` | Per linked issue | Per linked issue | Closed; SergioAndres17 |
| [HU-AUTH-06](https://github.com/code-corhuila/synkro-docs/issues/210) | `service_token` and `idempotency_key` tables, with the service-token foreign key and index | `synkro-auth-db` | Per linked issue | Per linked issue | Closed; SergioAndres17 |
| [HU-AUTH-07](https://github.com/code-corhuila/synkro-docs/issues/211) | Real token validation and role checks in `synkro-auth-api` | `synkro-auth-api` | Per linked issue | Per linked issue | Closed; SantiagoPlazas2005 |
| [HU-AUTH-08](https://github.com/code-corhuila/synkro-docs/issues/212) | PostgreSQL persistence for users and refresh tokens in `synkro-auth-api` | `synkro-auth-api` | Per linked issue | Per linked issue | Closed; SantiagoPlazas2005 |
| [HU-AUTH-09](https://github.com/code-corhuila/synkro-docs/issues/214) | User registration, user lookup and the common error envelope in `synkro-auth-api` | `synkro-auth-api` | Per linked issue | Per linked issue | Closed; SantiagoPlazas2005 |
| [HU-AUTH-10](https://github.com/code-corhuila/synkro-docs/issues/216) | Logout and refresh-token reuse detection in `synkro-auth-api` | `synkro-auth-api` | Per linked issue | Per linked issue | Closed; SantiagoPlazas2005 |
| [HU-AUTH-11](https://github.com/code-corhuila/synkro-docs/issues/217) | Service tokens in `synkro-auth-api` | `synkro-auth-api` | Per linked issue | Per linked issue | Closed; SantiagoPlazas2005 |
| [HU-VEN-04](https://github.com/code-corhuila/synkro-docs/issues/224) | Sales migration skeleton — Liquibase runner over MongoDB | `synkro-sales-db` | Per linked issue | Per linked issue | Closed; SergioAndres17 |
| [HU-VEN-05](https://github.com/code-corhuila/synkro-docs/issues/226) | Sale collection, validator and permissions | `synkro-sales-db` | Per linked issue | Per linked issue | Closed; SergioAndres17 |
| [HU-VEN-06](https://github.com/code-corhuila/synkro-docs/issues/228) | Sale/SaleDetail domain aggregate | `synkro-sales-api` | Per linked issue | Per linked issue | Closed; JordanRG420 |
| [HU-VEN-16](https://github.com/code-corhuila/synkro-docs/issues/230) | Harden Sales MongoDB Migrations and CI Failure Detection | `synkro-sales-db` | Per linked issue | Per linked issue | Closed; SergioAndres17 |
| [HU-GTW-02](https://github.com/code-corhuila/synkro-docs/issues/231) | Public login/refresh, protected register | `synkro-api-gateway` | Per linked issue | Per linked issue | Closed; AsolanoT |
| [HU-AUTH-12](https://github.com/code-corhuila/synkro-docs/issues/232) | Transactional User-Creation Audit Schema | `synkro-auth-db` | Per linked issue | Per linked issue | Closed; SergioAndres17 |
| [HU-VEN-07](https://github.com/code-corhuila/synkro-docs/issues/233) | Idempotent Sale Registration Use Case and Ports | `synkro-sales-api` | Per linked issue | Per linked issue | Closed; JordanRG420 |
| [HU-AUTH-13](https://github.com/code-corhuila/synkro-docs/issues/234) | Admin-Only Registration with Transactional Audit | `synkro-auth-api` | Per linked issue | Per linked issue | Closed; JordanRG420 |
| [HU-VEN-08](https://github.com/code-corhuila/synkro-docs/issues/235) | MongoDB Sale Persistence Adapter, BSON Mapping and Replica Set Integration | `synkro-sales-api` | Per linked issue | Per linked issue | Closed; JordanRG420 |
| [HU-INF-04](https://github.com/code-corhuila/synkro-docs/issues/236) | Persistent MongoDB Instance and Integration with Sales Migrations | `synkro-infra-mongo` | Per linked issue | Per linked issue | Closed; JordanRG420 |
| [HU-VEN-17](https://github.com/code-corhuila/synkro-docs/issues/237) | Protect Credentials in the MongoDB Migration Runner | `synkro-sales-db` | Per linked issue | Per linked issue | Closed; SergioAndres17 |
| [HU-AUTH-14](https://github.com/code-corhuila/synkro-docs/issues/238) | Audited Initial Administrator CLI | `synkro-auth-api` | Per linked issue | Per linked issue | Closed; JordanRG420 |
| [HU-VEN-09](https://github.com/code-corhuila/synkro-docs/issues/240) | Sales HTTP Endpoints and Role-Based Authorization | `synkro-sales-api` | Per linked issue | Per linked issue | Closed; JordanRG420 |
| [HU-VEN-11](https://github.com/code-corhuila/synkro-docs/issues/241) | Sales Portal Remote Integration and Real Sales History | `synkro-sales-portal` | Per linked issue | Per linked issue | Closed; AsolanoT |
| [HU-FE-07](https://github.com/code-corhuila/synkro-docs/issues/242) | Restore Remote Isolation When Switching Portals | `synkro-front` | Per linked issue | Per linked issue | Closed; AsolanoT |
| [HU-WKF-03](https://github.com/code-corhuila/synkro-docs/issues/243) | Persistent and Recoverable Sales Registration Orchestration | `synkro-workflow` | Per linked issue | Per linked issue | Closed; AsolanoT |
| [HU-VEN-10](https://github.com/code-corhuila/synkro-docs/issues/245) | Daily, Monthly and Top-Products Sales Aggregation Reports | `synkro-sales-api` | Per linked issue | Per linked issue | Closed; JordanRG420 |
| [HU-VEN-12](https://github.com/code-corhuila/synkro-docs/issues/246) | Real Sale Registration Through Workflow and Composed Sale Detail | `synkro-sales-portal` | Per linked issue | Per linked issue | Closed; AsolanoT |
| [HU-CLI-03](https://github.com/code-corhuila/synkro-docs/issues/247) | Functional Customers API with Persistence, Security, and Idempotent Registration | `synkro-customers-api` | Per linked issue | Per linked issue | Closed; AsolanoT |
| [HU-FE-08](https://github.com/code-corhuila/synkro-docs/issues/248) | Auth Portal and Production Authentication Through the Host | `synkro-front` | Per linked issue | Per linked issue | Closed; AsolanoT |
| [HU-CLI-04](https://github.com/code-corhuila/synkro-docs/issues/249) | Complete Customers Portal and Host Integration | `synkro-customers-portal` | Per linked issue | Per linked issue | Closed; JordanRG420 |

---

## Cut 9 — Documentation and Architecture Reconciliation (Week 12)

> The five approved stories below belong to Sprint 12 — Week 12.
> Their issues consume the assigned IDs. Preparation is not merge or closure.

| Story / issue | Responsible person | Status in Week 12 | Bounded outcome |
|---|---|---|---|
| [HU-DOCS-94 / #252](https://github.com/code-corhuila/synkro-docs/issues/252) | JordanRG420 | Closed; merged PR #257 | Identifier register, FR/NFR and implementation traceability, current governance and evidence index |
| [HU-DOCS-95 / #253](https://github.com/code-corhuila/synkro-docs/issues/253) | SergioAndres17 | Complete review package prepared; ARQ-28 dependency, peer review and merge pending | Domain/data/public-contract reconciliation |
| [HU-ARQ-28 / #254](https://github.com/code-corhuila/synkro-docs/issues/254) | AsolanoT | Open; no ADR accepted here | Separate saga/session/integration decision records; next ADR-015 |
| [HU-DOCS-96 / #255](https://github.com/code-corhuila/synkro-docs/issues/255) | SantiagoPlazas2005 | Open; no delivery claimed here | Actual deployment/runbooks/release/control |
| [HU-DOCS-97 / #256](https://github.com/code-corhuila/synkro-docs/issues/256) | AsolanoT | Open; no delivery claimed here | Service catalog, portal flows and architecture views |

---

## Epics

| ID | Epic | Description |
|----|------|--------------|
| EP-001 | Docs & Architecture Formalization | Formalize the domain-to-repository catalog and fill the remaining gap in product definition (`03-product`) |
| EP-002 | Panoramic MVP | Single-repo monolith prototype (Spring Boot + React) to validate business understanding before the 4 real hexagonal microservices are built |

---

## User Stories

### HU-ARQ-01 — Formalize the Domain-to-Repository Service Catalog {#HU-ARQ-01}

**Epic:** EP-001

> **As** the technical lead
> **I want** to formalize the final list of the 4 bounded contexts as future microservices, with concrete repository names, languages, and DB schemas
> **so that** the course instructor can create the exact repository ecosystem without ambiguity, and the team has a single source of truth linking `domain-map.md` to the real repos

**Acceptance Criteria:**

```gherkin
Scenario 1: Catalog matches the bounded contexts
  Given the 4 bounded contexts already defined in 02-domain/domain-map.md
        (Auth, Customers, Products and Inventory, Sales)
  When  the catalog is filled in
  Then  each context has a corresponding backend repo name, frontend repo
        name, language, and DB schema

Scenario 2: Languages match ADR-001 exactly
  Given ADR-001's technology decision (Java for Auth/Customers, Go for
        Products/Sales)
  When  the catalog lists each service's language
  Then  it matches ADR-001 exactly with no discrepancy

Scenario 3: All 10 repositories are accounted for
  Given the fixed repository ecosystem (4 backend + 4 frontend + 1 database
        + 1 docs)
  When  the catalog is complete
  Then  all 10 repositories are listed with their name and branch strategy
        (main/qa/dev, except docs which is main-only)

Scenario 4: Service numbering follows the convention
  Given the service numbering convention already defined in
        09-microservices/README.md (01 = IAM/Security, 02 = reference data,
        03-0N = domain services)
  When  services are numbered
  Then  Auth = 01, Customers = 02, Products = 03, Sales = 04
```

**Definition of Done:**
- [x] `09-microservices/service-catalog.md` filled in with the table below
- [x] Reviewed and approved by at least one other team member
- [x] No discrepancy with ADR-001 or `domain-map.md`
- [x] Per-service detail files (endpoints, events) explicitly deferred — not required for this HU

**Reference table (drop directly into `09-microservices/service-catalog.md`):**

| # | Bounded Context | Backend Repo | Language | Frontend Repo | DB Schema |
|---|---|---|---|---|---|
| 01 | Authentication & Users | `auth-service` | Java (Spring Boot) | `auth-frontend` | `auth` |
| 02 | Customers | `customers-service` | Java (Spring Boot) | `customers-frontend` | `customers` |
| 03 | Products & Inventory | `products-service` | Go | `products-frontend` | `products` |
| 04 | Sales | `sales-service` | Go | `sales-frontend` | `sales` |

Plus the two fixed non-domain repos: `database` (single PostgreSQL instance, 4 schemas above) and `docs` (`main`-only).

| Field | Value |
|-------|-------|
| Story Points | 3 |
| Priority | Must Have |
| Target sprint | Sprint 4 |
| Assigned to | Sergio Andrés Ordóñez Díaz |
| Status | Done |
| Dependencies | — |
| Affected service(s) | N/A (cross-cutting, `09-microservices/`) |

---

### HU-ARQ-02 — MVP Monolith: Functional Backend {#HU-ARQ-02}

**Epic:** EP-002

> **As** the Product Owner (course instructor)
> **I want** a functional Spring Boot monolith exposing the core business flows (customer management, product/stock catalog, sale registration with stock deduction)
> **so that** the end-to-end business flow can be validated before the 4 real hexagonal microservices from ADR-001 are built

**Acceptance Criteria:**

```gherkin
Scenario 1: Product price and stock invariants
  Given a product with price and stock fields
  When  a product is created via the API
  Then  price must be greater than 0 and stock can never become negative
  And   the request is rejected otherwise
        (mirrors the Product invariant in entities-and-rules.md)

Scenario 2: Sale requires an active customer
  Given an existing customer
  When  a sale is created for a customer with active = true
  Then  the sale is accepted
  And   given a deactivated customer, the sale is rejected
        (mirrors the Sale invariant "customerId must correspond to an
        active customer")

Scenario 3: Stock deduction on sale confirmation
  Given a sale with one or more line items
  When  it is confirmed
  Then  each product's stock is reduced by the sold quantity
  And   the request is rejected if the requested quantity exceeds
        available stock (mirrors Product.reduceStock())

Scenario 4: Total calculation with frozen unit price
  Given a confirmed sale
  When  its total is calculated
  Then  total equals the sum of each line's subtotal (quantity * unitPrice)
  And   unitPrice stays frozen at the moment of sale — it does not change
        if the product's price changes afterward
        (mirrors the SaleDetail invariant)

Scenario 5: Internal modularity without full hexagonal ports
  Given this is a single-repo monolith and not the 4 real microservices
  When  the code is organized
  Then  Auth/Customers/Products/Sales are kept as separate packages/modules
        internally, to ease a future split
  And   full hexagonal ports-and-adapters per module is explicitly not
        required here — that belongs to the real ADR-001 implementation
```

**Definition of Done:**
- [x] Code reviewed and approved
- [x] Backend actually runs and responds (no blank screen, per the instructor's rule)
- [x] Acceptance criteria verified manually
- [x] README explains how to run it locally
- [x] Embedded H2 DB is acceptable — full 4-schema PostgreSQL setup NOT required for this HU

| Field | Value |
|-------|-------|
| Story Points | 8 |
| Priority | Must Have |
| Target sprint | Sprint 4 |
| Assigned to | Angel Gustavo Solano Trujillo |
| Status | Done |
| Dependencies | — |
| Affected service(s) | New temporary repo `mvp-demo` (`/backend`) — **not** one of the 4 fixed backend repos from HU-ARQ-01 |

> **Technical notes:** Auth can be a simple mocked role selector (no real JWT/RS256 signing needed). Circuit Breaker, Saga, Outbox, and CQRS are out of scope — those are ADR-001's target-architecture patterns, not part of this throwaway spike.

---

### HU-ARQ-27 — ADR-014: Admin-Only User Registration and Initial Administrator Creation {#HU-ARQ-27}

**Epic:** EP-001

> **As** the development team
> **I want** to record admin-only user registration, transactional creation attribution and a controlled initial-administrator procedure as an architectural decision
> **so that** implementation follows one accepted authorization and initialization design.

**Acceptance Criteria:**

```gherkin
Scenario 1: Accepted decisions and merged documentation are traceable
  Given ADR-014 is Accepted
  When a reviewer follows issue #219 and PR #220
  Then the accepted record and its affected design documents are available on main
  And the decision index limits the modification of ADR-001 section 8 to registration visibility

Scenario 2: Initialization does not become an administrator recovery path
  Given an ADMIN account exists, including a deactivated account
  When the documented initialization existence check is evaluated
  Then initialization is a no-op
  And administrator recovery remains a separate pending decision
```

**Definition of Done:**

- [x] The Accepted ADR-014 record and its affected design documentation are merged through [PR #220](https://github.com/code-corhuila/synkro-docs/pull/220).
- [x] The accepted record identifies the decisions, alternatives, dominant criteria, accepted costs and consequences.
- [x] [Issue #219](https://github.com/code-corhuila/synkro-docs/issues/219) is closed, and the accepted record identifies the development-team reviewers.

| Field | Value |
|-------|-------|
| Story Points | Not applicable — documentation story |
| Priority | Must Have |
| Assigned to | Angel Gustavo Solano Trujillo (`@AsolanoT`) |
| Status | Done |
| Dependencies | ADR-001, ADR-004, ADR-006, ADR-009, ADR-012 |
| Affected service(s) | Auth design in `synkro-docs`; no production-code delivery claimed |
| Issue | [HU-ARQ-27 — #219](https://github.com/code-corhuila/synkro-docs/issues/219) |
| Delivery evidence | [Merged PR #220](https://github.com/code-corhuila/synkro-docs/pull/220) |

**Technical notes:** [ADR-014](../05-architecture/decisions/records/ADR-014-admin-only-registration-and-initial-administrator.md) is the decision source. PR #220 also merged the contract, security, domain, data, deployment, operations, UX and planned-test alignment. [HU-DOCS-91](https://github.com/code-corhuila/synkro-docs/issues/221) reconciles the remaining catalog and tracking records; it does not repeat that alignment.

---

### HU-FE-01 — Interactive MVP Walkthrough — Sales Management System {#HU-FE-01}

**Epic:** EP-002

> **As** the Product Owner (course instructor)
> **I want** to navigate a React interface covering login, customer management, product/stock catalog, and sale registration
> **so that** I can validate the team's understanding of the business flow before the distributed architecture is implemented

**Acceptance Criteria:**

```gherkin
Scenario 1: Simulated role-based login
  Given the app loads
  When  the user opens it
  Then  a login screen lets them pick a role (ADMIN, SALESPERSON, INVENTORY)
  And   no real JWT is required — this is a simulated session

Scenario 2: ADMIN sees all modules
  Given a user logs in as ADMIN
  When  they reach the main navigation
  Then  all modules are visible (Customers, Products, Sales, basic summary)

Scenario 3: SALESPERSON sees a restricted menu
  Given a user logs in as SALESPERSON
  When  they reach the main navigation
  Then  only Customers, Sales, and a read-only stock lookup are visible
  And   product/category management is not visible

Scenario 4: INVENTORY sees a restricted menu
  Given a user logs in as INVENTORY
  When  they reach the main navigation
  Then  only Products/Categories/Stock are visible
  And   Customers and Sales are not visible

Scenario 5: Customer management reflects immediately
  Given the Customers module
  When  the user creates or edits a customer
  Then  the change is reflected immediately in the list

Scenario 6: Product registration
  Given the Products module
  When  the user registers a product with price and stock
  Then  it appears in the catalog with its current stock

Scenario 7: Confirming a sale
  Given the Sales module
  When  the user selects a customer + products + quantities and confirms
  Then  the UI shows the total, deducts stock, and the sale appears in a
        sales history list

Scenario 8: Insufficient stock is blocked
  Given a product has insufficient stock
  When  the user tries to sell more than available
  Then  the sale is blocked with a clear message
```

**Definition of Done:**
- [x] Mockup actually runs end-to-end in a browser (no blank screen)
- [x] Visual flow matches the diagrams already documented in `12-ux-ui/navigation-map.md`
- [x] Reviewed and approved by at least one other team member
- [x] README explains how to run it locally

| Field | Value |
|-------|-------|
| Story Points | 8 |
| Priority | Must Have |
| Target sprint | Sprint 4 |
| Assigned to | Jordan Ramirez Gallego |
| Status | Done |
| Dependencies | — (intentionally decoupled from HU-ARQ-02's backend; connecting to the real API is a stretch goal, not a blocker) |
| Affected service(s) | Same temporary repo `mvp-demo` (`/frontend`) — **not** one of the 4 fixed frontend repos from HU-ARQ-01 |

> **Technical notes:** Stack: React (Vite recommended). Data layer: local/in-memory mock (Context or a simple store).

---

### HU-DOCS-12 — Fill In Problem Framing and Product Vision {#HU-DOCS-12}

**Epic:** EP-001

> **As** the Product Owner (course instructor) and the team
> **I want** `03-product/problem-framing.md` and `03-product/vision.md` filled in, following the Week 2 order in `00-sdd-guide.md` that was skipped while the team fixed the ADR and context-map
> **so that** the product rationale (why SynkroTech SAS needs this system, and what "done" looks like) is documented before requirements and architecture keep building on top of a gap

**Acceptance Criteria:**

```gherkin
Scenario 1: Problem framing names real segments and a metric
  Given the _template-problem-framing.md structure
  When  problem-framing.md is filled in
  Then  it names the affected user segments — the three internal roles
        already defined in RBAC (ADMIN, SALESPERSON, INVENTORY) — the
        current pain each one has, and at least one North Star success metric

Scenario 2: Vision statement stays inside the approved MVP scope
  Given the vision.md template (Geoffrey Moore format)
  When  it's filled in
  Then  it produces one vision statement consistent with what's already
        fixed in 01-context/scope.md
  And   no feature is introduced that falls outside the documented MVP scope

Scenario 3: Domain modeling is not redone here
  Given 02-domain/domain-map.md already exists and is approved
  When  problem-framing is written
  Then  it does not redefine bounded contexts — this section only frames
        the business problem, per the correlation rule in 03-product/README.md

Scenario 4: Pending note gets resolved
  Given the project's own tracking flagged this as "Pendiente inmediato"
        (03-product before 04-requirements/05-architecture)
  When  this HU is closed
  Then  that pending note is resolved
```

**Definition of Done:**
- [x] Both files consistent with `01-context/overview.md` and `03-product/problem-framing.md`
- [x] "Evidence of the problem" section explicitly labeled as sourced from the professor's original business brief (academic project — no fabricated interviews or metrics)
- [x] Reviewed and approved by at least one other team member

| Field | Value |
|-------|-------|
| Story Points | 5 |
| Priority | Should Have |
| Target sprint | Sprint 4 |
| Assigned to | Fredman Santiago Plazas Artunduaga |
| Status | Done |
| Dependencies | — |
| Affected service(s) | N/A (product definition, cross-cutting) |

---

### HU-DOCS-13 — Formalize the MVP Backlog and Non-Functional Requirements {#HU-DOCS-13}

**Epic:** EP-001

> **As** the Product Owner (course instructor) and the team
> **I want** `04-requirements/user-stories.md` (10–15 MVP user stories) and `04-requirements/non-functional.md` (NFRs with measurable metrics) filled in
> **so that** the backlog and the MVP's quality level are made explicit, and the NFR IDs already referenced in other documents (NFR-03, NFR-004, NFR-007) finally get a real, measurable definition instead of remaining loose references

**Acceptance Criteria:**

```gherkin
Scenario 1: User stories formalize what's already decided, not invented from scratch
  Given the MVP scope already fixed in 01-context/scope.md, and the
        concrete flows already built in the MVP monolith (HU-ARQ-02, HU-FE-01)
  When  user-stories.md is filled in
  Then  it must contain 10–15 user stories in the _template-hu.md
        format, each traceable to an item already present in scope.md's
        "MVP Scope" table
  And   no story introduces functionality outside that table

Scenario 2: Non-functional requirements get real, measurable definitions
  Given NFR-03, NFR-004, and NFR-007 are already mentioned by ID in
        01-context/overview.md but were never formally defined
  When  non-functional.md is filled in
  Then  each of those three IDs gets a complete definition with a
        measurable metric (per _template-nfr.md)
  And   any additional NFR the team identifies is added with its own ID

Scenario 3: Each user story is mapped to a responsible service
  Given the 4 bounded contexts already fixed in 02-domain/domain-map.md
  When  each user story is written
  Then  it must state which of Auth/Customers/Products/Sales is
        responsible, consistent with 09-microservices/service-catalog.md
```

**Definition of Done:**
- [x] Both files are consistent with `01-context/scope.md`, `02-domain/domain-map.md`, and `09-microservices/service-catalog.md`
- [x] No user story contradicts an acceptance criterion already implemented in the MVP monolith (HU-ARQ-02 / HU-FE-01)
- [x] Reviewed and approved by at least one other team member

| Field | Value |
|-------|-------|
| Story Points | 8 |
| Priority | Should Have |
| Target sprint | Sprint 4 |
| Assigned to | Fredman Santiago Plazas Artunduaga + Angel Gustavo Solano Trujillo |
| Status | Done |
| Dependencies | HU-DOCS-12 (problem-framing/vision must exist first — already done) |
| Affected service(s) | N/A (product/requirements definition, cross-cutting) |

---

### HU-DOCS-89 — Add the Products Portal Stories to the Traceability Matrix {#HU-DOCS-89}

**Epic:** EP-001
> **As** the development team
> **I want** record Products portal delivery in requirements traceability
> **so that** reviewers can distinguish real Products coverage from remaining Sales gaps.

**Acceptance Criteria:**
```gherkin
Scenario 1: Documented delivery
  Given the story scope recorded in issue #207
  When its documentation evidence is reviewed
  Then HU-PRO-11 and HU-PRO-12 are traced and Products is distinguished from Sales
Scenario 2: Incorrect documentation is rejected
  Given outdated or misleading descriptions
  When they are compared with the story criteria
  Then Products is not incorrectly described as a simulated service
```

**Definition of Done:** Apply the [documentation DoD](../00-governance/definition-of-done.md); the linked issue is the historical review record. Its closed state is recorded here without inventing additional test execution.
| Field | Value |
|-------|-------|
| Story Points | Not applicable — documentation story |
| Assigned to | `@SantiagoPlazas2005` |
| Status | Closed — historical issue record |
| Issue | [HU-DOCS-89 — #207](https://github.com/code-corhuila/synkro-docs/issues/207) |
| Delivery scope | Products traceability in `04-requirements/traceability-matrix.md` |

---

### HU-DOCS-90 — Clarify Modal and Button Tokens, Fix Sale Amount Validation Wording {#HU-DOCS-90}

**Epic:** EP-001
> **As** the development team
> **I want** clarify modal/button tokens and the Products/Sales contract descriptions
> **so that** developers implement documented UI and amount-validation behavior.

**Acceptance Criteria:**
```gherkin
Scenario 1: Documented delivery
  Given the story scope recorded in issue #215
  When its documentation evidence is reviewed
  Then modal/button guidance and API descriptions match the issue scope
Scenario 2: Incorrect documentation is rejected
  Given outdated or misleading descriptions
  When they are compared with the story criteria
  Then sale amounts are not incorrectly documented as caller-supplied values
```

**Definition of Done:** Apply the [documentation DoD](../00-governance/definition-of-done.md); the linked issue is the historical review record. Its closed state is recorded here without inventing additional test execution.
| Field | Value |
|-------|-------|
| Story Points | Not applicable — documentation story |
| Assigned to | `@JordanRG420` |
| Status | Closed — historical issue record |
| Issue | [HU-DOCS-90 — #215](https://github.com/code-corhuila/synkro-docs/issues/215) |
| Delivery scope | Modal/button tokens in `12-ux-ui/design-system.md` and Products/Sales API wording |

---

### HU-DOCS-91 — Reconcile ADR-014 Documentation Tracking and User Story Catalog {#HU-DOCS-91}

**Epic:** EP-001

> **As** the development team
> **I want** to reconcile the user-story catalog, decision index and traceability records with the ADR-014 documentation already merged
> **so that** reviewers and implementers can distinguish completed design documentation from pending implementation and administrator recovery work.

**Acceptance Criteria:**

```gherkin
Scenario 1: Catalog and documentation evidence agree
  Given HU-ARQ-27 is closed and PR #220 is merged
  When a reviewer follows the ARQ and DOCS catalog entries
  Then both stories use the existing catalog structure and their actual issue links
  And the merged design alignment is attributed to PR #220
  And HU-DOCS-91 identifies only the remaining documentation reconciliation

Scenario 2: Misleading completion statements are rejected
  Given a tracking statement describes merged design documents as pending
        or planned Auth tests as executed
  When the documentation review is performed
  Then the inconsistency is corrected before HU-DOCS-91 can be closed
  And planned implementation evidence and administrator recovery remain pending

Scenario 3: The historical PR scope is corrected
  Given PR #220 included the ADR and its affected documentation
  When its description is reconciled
  Then the summary identifies the documentation already merged
  And the scope identifies only the remaining reconciliation in HU-DOCS-91

Scenario 4: Accepted decisions remain intact
  Given ADR-014 is Accepted
  When the decision index is corrected
  Then its date is 2026-10-10 and its status remains Accepted
  And accepted ADR records and historical cut totals remain unchanged
```

**Definition of Done:**

Apply the [documentation DoD](../00-governance/definition-of-done.md).

- [ ] Catalog entries follow the existing structure, with no blank sections or unresolved placeholders.
- [ ] Corrections are limited to the catalog and tracking sections identified in issue #221; approved historical content and accepted ADR records remain unchanged.
- [ ] Existing design definitions are referenced instead of duplicated.
- [ ] The corrected documents are consistent with ADR-014, the domain rules and the Auth contract.
- [ ] The description of PR #220 accurately states its merged scope.
- [ ] Documentation verification results are recorded without claiming implementation tests or migrations have run.
- [ ] At least one other team member has reviewed the changes, required approval from `ariel5253` is obtained, and applicable checks and review conversations are resolved.
- [ ] The English documentation and completed catalog record are published to `main`.
- [ ] Issue #221, this catalog entry and the board agree on the actual completion state.

| Field | Value |
|-------|-------|
| Story Points | Not applicable — documentation story |
| Priority | Must Have |
| Assigned to | Angel Gustavo Solano Trujillo (`@AsolanoT`) |
| Status | Merged through PR #222; review-evidence corrections tracked in HU-DOCS-92 |
| Dependencies | [HU-ARQ-27 — #219](https://github.com/code-corhuila/synkro-docs/issues/219), closed; [PR #220](https://github.com/code-corhuila/synkro-docs/pull/220), merged |
| Affected service(s) | `synkro-docs` documentation tracking; no production-code changes |
| Issue | [HU-DOCS-91 — #221](https://github.com/code-corhuila/synkro-docs/issues/221) |
| Branch | `docs/reconcile-adr-014-documentation-tracking` |
| Merged delivery | [PR #222](https://github.com/code-corhuila/synkro-docs/pull/222) |
| Evidence follow-up | [HU-DOCS-92 — #223](https://github.com/code-corhuila/synkro-docs/issues/223); [verification record](../15-project-control/documentation-verification.md) |

**Technical notes:** Changes are limited to this catalog, the ADR-014 section in `traceability-matrix.md`, the decision index date, the ADR-014 tracking statement in `technical-backlog.md`, and the DOCS/ARQ next-ID counters in `agile-conventions.md`. The PR #220 description is corrected separately as GitHub metadata. Planned Auth test IDs remain design references, and TD-009 administrator recovery is outside this story.

**Deliverable:** the five corrected Markdown files merged into `main`, an accurate PR #220 description, and linked documentation-review evidence.

---

### HU-DOCS-92 — Align Login Ownership and Frontend Session Lifecycle Documentation {#HU-DOCS-92}

**Epic:** EP-001
> **As** the development team
> **I want** production login ownership and host-managed session behavior documented consistently
> **so that** implementation avoids duplicated token handling and incorrect login or refresh retries.

**Acceptance Criteria:**
```gherkin
Scenario 1: Login and renewal follow the shared host policy
  Given the production Auth portal and host design
  When login succeeds or renewal is due before protected work
  Then only the host consumes tokens and coordinates one in-flight refresh
  And tab reactivation checks expiration before releasing protected requests
Scenario 2: Authentication failures do not cause retry loops
  Given a login 401, refresh rejection, protected 401 or forbidden response
  When the corresponding operation outcome is handled
  Then login 401 shows a credential error without refresh
  And refresh rejection or protected 401 ends the session without replay
  And 403 keeps the session and shows access denied
Scenario 3: Review evidence and identifiers remain traceable
  Given the closed DOCS-89, DOCS-90 and DOCS-91 issues and open issue #223
  When the catalog and verification evidence are followed
  Then each consumed ID has an issue link and the next DOCS number is 93
  And the PR #222 report-link and cross-cutting-scope corrections are explicit
```

**Definition of Done:** Apply the [documentation DoD](../00-governance/definition-of-done.md).
- [ ] Affected English sections are complete, consistent and reference shared definitions.
- [ ] Production design is distinguished from historical prototypes; accepted ADRs and API contracts are preserved.
- [ ] Planned frontend scenarios and the 60-second renewal margin are concrete; no implementation execution is claimed.
- [ ] [Verification evidence](../15-project-control/documentation-verification.md) is linked from the PR descriptions; PR #222/#220 metadata corrections are applied.
- [ ] Teammate review, required `ariel5253` approval, applicable checks and review conversations are complete.
- [ ] The reviewed documentation and completed catalog record are merged into main; issue #223 and the board reflect that actual state.

| Field | Value |
|-------|-------|
| Story Points | Not applicable — documentation story |
| Priority | Must Have |
| Assigned to | Angel Gustavo Solano Trujillo (`@AsolanoT`) |
| Status | Merged through PR #225; issue #223 closed; policy formalization tracked in HU-DOCS-93 |
| Dependencies | ADR-006, ADR-008, ADR-011; [merged PR #222](https://github.com/code-corhuila/synkro-docs/pull/222) |
| Issue | [HU-DOCS-92 — #223](https://github.com/code-corhuila/synkro-docs/issues/223) |
| Branch | `docs/align-frontend-session-and-login-docs` |
| Merged delivery | [PR #225](https://github.com/code-corhuila/synkro-docs/pull/225) |

**Technical notes:** Frontend lifecycle is specified in `07-api/authentication.md`; verification scenarios are FE-AUTH-01/02 and FE-SESSION-01 through 07. This story also adds missing historical DOCS-89/90 catalog records and retrospective evidence for PR #222. The initial HU-DOCS-91 report was not linked in that PR; this follow-up does not claim it was.
**Deliverable:** the aligned authentication, frontend, UX, planned-test and tracking documents, linked verification evidence and accurate historical PR descriptions.

---

### HU-DOCS-93 — Reconcile Auth Documentation and Session Decision Traceability {#HU-DOCS-93}

**Epic:** EP-001
> **As** the development team
> **I want** Auth references, session-policy status and verification records reconciled
> **so that** implementation follows consistent documentation without treating a proposal as an accepted decision or a planned test as execution evidence.

**Acceptance Criteria:**
```gherkin
Scenario 1: Existing documents contain a traceable proposal
  Given this delivery is restricted to Markdown files already in the repository
  When a reviewer follows the browser renewal policy
  Then authentication.md records its Proposed status, options, criterion and costs
  And TD-010 identifies future standalone ADR formalization and team ratification
  And no new ADR file, identifier or Accepted status is claimed
Scenario 2: Auth references agree
  Given the existing Auth model and contract
  When the dictionary, schema references and API guidance are checked
  Then the refresh credential is hash-only in auth_schema
  And URLs compose the server base and path without duplicating api/v1
Scenario 3: Unsupported revocation claims are rejected
  Given the logout contract leaves issued access JWTs valid until expiration
  When a proposed test assumes immediate JWT invalidation
  Then that assumption is identified as the TD-011 scope decision
  And no denylist, token-family table or runtime Auth lookup is invented
Scenario 4: Dated evidence remains historical
  Given an earlier verification entry
  When later work corrects or extends its findings
  Then the original entry remains identifiable
  And the new dated entry identifies its baseline, scope and correction relationship
```

**Definition of Done:** Apply the [documentation DoD](../00-governance/definition-of-done.md).
- [ ] Existing affected sections are complete, consistent and reference shared definitions.
- [ ] Proposed policy status and TD-010/TD-011 boundaries are explicit; accepted ADRs, contracts and persistence structures remain unchanged.
- [ ] Planned backend scenarios are distinct from frontend scenarios and have no fabricated execution evidence.
- [ ] Historical verification content is retained under an explicit maintenance policy; new evidence has stable links.
- [ ] Issue #227 reflects the existing-files-only scope in place of its original new-ADR deliverable.
- [ ] Teammate review, required `ariel5253` approval, applicable checks and review conversations are complete.
- [ ] Reviewed English documentation and the completion record are merged into main; issue and board reflect the actual state.

| Field | Value |
|-------|-------|
| Story Points | Not applicable — documentation story |
| Priority | Must Have |
| Assigned to | Angel Gustavo Solano Trujillo (`@AsolanoT`) |
| Status | Closed — issue #227; merged PR #229 |
| Issue | [HU-DOCS-93 — #227](https://github.com/code-corhuila/synkro-docs/issues/227) |
| Dependencies | [HU-DOCS-92 — #223](https://github.com/code-corhuila/synkro-docs/issues/223), closed; [PR #225](https://github.com/code-corhuila/synkro-docs/pull/225), merged |
| Branch | `docs/clear-auth-documentation-debt` |
| Affected service(s) | Auth documentation and project verification records; no production-code delivery |

**Delivery evidence:** [PR #229](https://github.com/code-corhuila/synkro-docs/pull/229) is merged in the inspected main baseline. Its acceptance checkboxes describe its own original completion criteria; implementation and TD-010/011 decisions remain separate.

**Technical notes:** The revised scope uses existing files only. The renewal proposal lives in `07-api/authentication.md`; it is not a numbered ADR. The decision index points to the proposal and TD-010 rather than registering a nonexistent record. TD-011 records the reuse/revocation scope gap. The catalog does not claim either pending decision is resolved.
**Deliverable:** corrections to existing Auth, quality, UX, governance and project-control Markdown files, with [dated verification evidence](../15-project-control/documentation-verification.md#hu-docs-93-verification-entry--2026-10-11).

---

### HU-DOCS-94 — Reconcile Delivery Traceability and the Identifier Register {#HU-DOCS-94}

| Field | Value |
|---|---|
| Assigned to | JordanRG420 |
| Issue | [#252](https://github.com/code-corhuila/synkro-docs/issues/252) |
| Branch | `docs/reconcile-delivery-traceability-and-identifiers` |
| Peer reviewer | SergioAndres17 |
| Sprint / Week | Sprint 12 — Week 12 |
| Status | Closed — issue #252; merged PR #257 |

**As** the development team, **I want** the requirement matrix, story catalog and identifier register reconciled with actual deliveries, **so that** each claim leads to its story, tests and implementation while gaps, substitutions and unfinished work stay explicit.

**Tasks by phase**
- [x] Inspect the 18 code repository revisions, tags and source differences; record the bounded snapshot.
- [x] Reconcile the seven newly closed issues with preserved evidence boundaries; keep HU-INF-02/#239 open.
- [x] Reconcile DOCS-93's merged delivery, consumed IDs, #230/#237 and documented skips/substitutions without mass renumbering.
- [x] Prepare current FR/NFR, inverse traceability, governance and scenario-to-test evidence sections, with status pointers in the canonical requirement files and a project-control index entry, across twelve existing Markdown files.
- [x] Preserve existing structure and unchanged architecture/contracts/migrations; update content in place and prepare complete replacement files.
- [ ] Record independent teammate review and required approval/checks for the documentation PR.
- [ ] Merge the reviewed files, reconcile the completion entry and update actual issue/board state.

**Acceptance criteria**
- [x] Given FR-008/009, when their rows are followed, then HU-VEN-10/#245, real test paths and frozen source revisions are present, and reporting UI remains explicit pending work.
- [x] Given #230/#237, when the central register is read, then VEN-16 and VEN-17 are distinct, historical #237 aliases are explained and no earlier delivery is renumbered.
- [x] Given a closed issue or unexecuted check, when status is recorded, then closure and evidence limits are separate; no fictitious approval/green CI is introduced.
- [x] Given a numbering gap or unassigned plan, when it is reconciled, then the evidence and any unknown cause are explicit without retroactive issue creation.
- [ ] Given the complete files, when peer review and the main PR finish, then their real references confirm publication and completion.

```gherkin
Feature: Reconcile delivery evidence and assigned identifiers

  Scenario: Follow delivered sales-report functionality
    Given HU-VEN-10 is assigned to issue #245 and its reporting API is delivered
    When a reviewer follows FR-008 and FR-009 through the traceability matrix
    Then the chain identifies the actual report tests and implementation revision
    And the reporting interface remains a separate pending scope

  Scenario: Evidence is insufficient for full acceptance
    Given a required check failed or a required scenario was not executed
    When the related issue is recorded as closed
    Then closure and the evidence limitation remain separate facts
    And no passing result or reviewer approval is invented

  Scenario: Resolve an ambiguous historical identifier
    Given HU-VEN-16 appears in a historical reference
    When its link identifies issue #237
    Then its canonical identifier is HU-VEN-17
    And references to issue #230 retain HU-VEN-16
```

**Deliverable:** complete English replacements for the twelve files listed in the [verification entry](../15-project-control/documentation-verification.md#hu-docs-94-verification-entry--2026-10-24). Apply the current documentation DoD. No merge, approval or board update is claimed by file preparation.

---


**Published delivery:** [PR #257](https://github.com/code-corhuila/synkro-docs/pull/257) merged the original-structure reconciliation into main at 2e07223829a51573574b0c6fc134b4d321a118da. #252 is closed; this factual update does not fabricate review/CI evidence or revise its scope.

---

### HU-DOCS-95 — Reconcile Domain, Data and Public API Contracts {#HU-DOCS-95}

**As** the development team, **I want** domain, data and public contracts reconciled with reviewed delivered behavior, **so that** consumers have one consistent specification and unresolved drift has an owner and closing evidence.

| Field | Value |
|-------|-------|
| Story Points | Not applicable — documentation story |
| Priority | Must Have |
| Assigned to | Sergio Andrés Ordóñez Díaz (SergioAndres17) |
| Peer reviewer | Jordan Ramirez Gallego (JordanRG420) |
| Sprint / Week | Sprint 12 — Week 12 |
| Issue | [#253](https://github.com/code-corhuila/synkro-docs/issues/253) |
| Branch | `docs/reconcile-domain-data-and-api-contracts` |
| Dependencies | DOCS-94/#252 closed via PR #257; relevant ARQ-28/#254 decisions still pending |
| Status | Complete files prepared for review; decision-dependent publication, peer approval/checks and merge pending; issue open |
| Affected service(s) | Domain/data specifications, five HTTP contracts, behavior/security references and bounded evidence/control sections |

**Tasks by phase**

- [x] Compare canonical domain rules first, then models/dictionary, pinned migrations, API sources and actual tests.
- [x] Reconcile V017 immutable adjustment receipts and their explicit legacy fallback.
- [x] Keep SaleDetail.active and inactive-root restoration as a tracked domain/storage defect without silently rewriting the business lifecycle.
- [x] Correct effective /health servers and document observed 400/500 responses and the Products store-health gap.
- [x] Prepare candidate Workflow replay/rejection/recovery clauses with private metadata, unchanged public fields/statuses and explicit decision dependency.
- [x] Reconcile UTC/grouped reports, integer money, frozen prices, ownership and the stale FR-006 CHECK reference.
- [x] Record TD-012–018 as unresolved discrepancies with owners and closing evidence, separately from accepted deferred improvements.
- [ ] Accept the relevant modifying decision through HU-ARQ-28 and reconcile its final references before adoption of dependent normative guidance.
- [ ] Obtain domain/contract peer review, required main approval and applicable checks; integrate the reviewed files and record actual completion.

**Acceptance criteria**

```gherkin
Feature: Domain, data and public-contract reconciliation

  Scenario: Compare a consistent sale and report
    Given the saga reserves products at frozen integer prices
    When domain, storage and API representations are compared
    Then identifiers, creator ownership, subtotals and totals have an explicit trace
    And unsupported lifecycle representation is named in an owned discrepancy

  Scenario: Reject an unsafe or foreign replay
    Given the reviewed candidate POST replay rule
    When another caller reuses a key or the same caller changes validated input
    Then the candidate contract specifies generic 403 or 400 respectively
    And adoption as approved guidance remains pending the accepted ARQ-28 decision
    And no stored owner, input or saga identifier is disclosed

  Scenario: Preserve stock after an uncertain result
    Given a participant may have committed and sends a malformed rejection
    When the recovery boundary is documented
    Then the reservation remains held pending evidence
    And no duplicate key or unsafe compensation is authorized

  Scenario: Detect unsupported inactive restoration
    Given storage and API include an inactive root or detail
    When the decoder cannot faithfully represent that state
    Then TD-012 names its owner and required closing evidence
    And the domain is not silently changed to fit stored fields
```

**Technical notes:** The package modifies no applied migration, accepted ADR or service implementation. The next ADR remains 015; no unrelated unmerged proposal is adopted. Syntax/example validation does not close the seven discrepancies or accept the pending decision.

**Deliverable:** complete English Markdown and five affected OpenAPI files, with [bounded verification](../15-project-control/documentation-verification.md#hu-docs-95-verification-entry--2026-10-24) and the [seven discrepancy records](../15-project-control/technical-backlog.md#unresolved-delivery-discrepancies).

---


## Rules for writing HUs

### 1. The role matters
Do not write "As a user" — that says nothing. Use the specific role:
```
✓ As a system administrator
✓ As a registered customer
✓ As an inventory operator
✗ As a user
✗ As a person
```

### 2. The benefit justifies the work
The "so that" must describe a business benefit, not redescribe the action:
```
✓ so that I can manage my orders without calling support
✗ so that I can see my orders (this only describes the feature)
```

### 3. ACs are verifiable
Each AC must be verifiable manually or automatable as a test:
```
✓ Then the system shows a message "Order #123 confirmed"
✓ Then the confirmation email arrives in less than 30 seconds
✗ Then the system works well (not verifiable)
✗ Then the user is satisfied (not verifiable)
```

### 4. One HU = one unit of value
If the HU has 15 ACs, it is probably 3 HUs.
The team must be able to complete it in one sprint (maximum 2 weeks).

---

## Ready-to-copy HU template

```markdown
### HU-00X — [Name] {#HU-00X}

**Epic:** EP-00X

> **As** [role]
> **I want** [action]
> **so that** [benefit]

**Acceptance Criteria:**

\```gherkin
Scenario 1: [name]
  Given [context]
  When  [action]
  Then  [result]
\```

| Field | Value |
|-------|-------|
| Story Points | |
| Priority | |
| Target sprint | |
| Status | Backlog |
| Dependencies | |
```

---

## Correlations

- Full template with DoD checklist → `04-requirements/_template-hu.md`
- Non-functional requirements → `04-requirements/non-functional.md`
- Traceability matrix → `04-requirements/traceability-matrix.md`
- API contracts derived from these HUs → `07-api/contracts/openapi/`
