# Traceability Matrix

> Traceability connects every line of code to its business justification.
> It allows answering: "Why does this function exist?" and "Which HU covers this part of the system?"
> It also identifies: unimplemented requirements and code without a requirement (possible technical debt).

---

## How to use this matrix

```
Requirement → HU → Test Case → Implementation → Service

If a requirement has no HU: it is not planned
If a HU has no test case: it has no completeness criterion
If a test case has no implementation: there is test technical debt
If there is code without an HU: possible gold-plating or bug introduced without a story
```

---

## FR → HU → Test → Service matrix

> Reconciled for Week 12 from the 2026-10-24 source/issue snapshot.
> Every link identifies its scope; test-file presence does not prove execution.

**Shared acceptance and CI boundary:** full root composition remains open under HU-INF-02/#239. The [dated CI table](../15-project-control/documentation-verification.md#latest-ci-observations) records every repository's exact observed run, branch, head and result, including unsuccessful main runs for Products API/DB, Sales API/Portal, Workflow, Worker, Front and Auth DB. These outcomes apply wherever the component is traced, rather than only to one FR row. A failed check, missing evidence or pending platform acceptance is not erased by a tag or closed component issue.

| FR ID | Description | HU(s) | Contract | Tests that verify it | Service (target) | Status |
|-------|-------------|-------|----------|----------------------|------------------|--------|
| FR-001 | Register, update, view, and deactivate customers | [HU-CLI-02](https://github.com/code-corhuila/synkro-docs/issues/244), [HU-CLI-03](https://github.com/code-corhuila/synkro-docs/issues/247), [HU-CLI-04](https://github.com/code-corhuila/synkro-docs/issues/249) | [synkro-customers-api.yaml](../07-api/contracts/openapi/synkro-customers-api.yaml) | [customers-core/src/test/java/co/edu/corhuila/synkro/customers/application/usecase/CustomerServiceTest.java](https://github.com/code-corhuila/synkro-customers-api/blob/28caff4f2d4c5e562416db7400061675e4b6766b/customers-core/src/test/java/co/edu/corhuila/synkro/customers/application/usecase/CustomerServiceTest.java) → implementation: [customers-core/src/main/java/co/edu/corhuila/synkro/customers/application/usecase/CustomerService.java](https://github.com/code-corhuila/synkro-customers-api/blob/28caff4f2d4c5e562416db7400061675e4b6766b/customers-core/src/main/java/co/edu/corhuila/synkro/customers/application/usecase/CustomerService.java) | `synkro-customers-api` | 🟡 Source, tests and real CRUD acceptance reported; portal is Angular and uses the Host contract. [E-CUSTOMERS](../11-quality/testing-strategy.md#e-customers) |
| FR-002 | Register, update, view, and deactivate products | [HU-PRO-05](https://github.com/code-corhuila/synkro-docs/issues/192), [HU-PRO-08](https://github.com/code-corhuila/synkro-docs/issues/195), [HU-PRO-01](https://github.com/code-corhuila/synkro-docs/issues/202), [HU-PRO-02](https://github.com/code-corhuila/synkro-docs/issues/205), [HU-PRO-12](https://github.com/code-corhuila/synkro-docs/issues/206), [HU-PRO-13](https://github.com/code-corhuila/synkro-docs/issues/204) | [synkro-products-api.yaml](../07-api/contracts/openapi/synkro-products-api.yaml) | [internal/application/usecase/products_test.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/application/usecase/products_test.go) → implementation: [internal/application/usecase/products.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/application/usecase/products.go) | `synkro-products-api` | 🟡 API and portal delivered; the shared acceptance and CI boundaries apply. [E-PRODUCTS](../11-quality/testing-strategy.md#e-products) |
| FR-003 | Organize products by category | [HU-PRO-05](https://github.com/code-corhuila/synkro-docs/issues/192), [HU-PRO-08](https://github.com/code-corhuila/synkro-docs/issues/195), [HU-PRO-12](https://github.com/code-corhuila/synkro-docs/issues/206) | [synkro-products-api.yaml](../07-api/contracts/openapi/synkro-products-api.yaml) | [internal/application/usecase/products_test.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/application/usecase/products_test.go) → implementation: [internal/application/usecase/products.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/application/usecase/products.go) | `synkro-products-api` | 🟡 Category API and portal CRUD exist; visual polish is no longer an unbuilt PRO-13 claim. [E-PRODUCTS](../11-quality/testing-strategy.md#e-products) |
| FR-004 | Control available stock of each product | [HU-PRO-06](https://github.com/code-corhuila/synkro-docs/issues/193), [HU-PRO-07](https://github.com/code-corhuila/synkro-docs/issues/194), [HU-PRO-09](https://github.com/code-corhuila/synkro-docs/issues/198), [HU-PRO-10](https://github.com/code-corhuila/synkro-docs/issues/199), [HU-PRO-12](https://github.com/code-corhuila/synkro-docs/issues/206) | [synkro-products-api.yaml](../07-api/contracts/openapi/synkro-products-api.yaml) | [internal/application/usecase/stock_test.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/application/usecase/stock_test.go) → implementation: [internal/application/usecase/stock.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/application/usecase/stock.go) | `synkro-products-api` | 🟡 Manual stock/reservation/alert API and views exist. Worker low-stock automation is still missing; WRK-01 is scheduler-only. [E-STOCK](../11-quality/testing-strategy.md#e-stock) |
| FR-005 | Register a sale associating a customer with products | [HU-VEN-06](https://github.com/code-corhuila/synkro-docs/issues/228), [HU-VEN-07](https://github.com/code-corhuila/synkro-docs/issues/233), [HU-VEN-08](https://github.com/code-corhuila/synkro-docs/issues/235), [HU-VEN-09](https://github.com/code-corhuila/synkro-docs/issues/240), [HU-WKF-03](https://github.com/code-corhuila/synkro-docs/issues/243), [HU-VEN-11](https://github.com/code-corhuila/synkro-docs/issues/241), [HU-VEN-12](https://github.com/code-corhuila/synkro-docs/issues/246) | [synkro-workflow.yaml](../07-api/contracts/openapi/synkro-workflow.yaml); [synkro-sales-api.yaml](../07-api/contracts/openapi/synkro-sales-api.yaml) (POST /sales, saga-internal) | [workflow-core/src/test/java/co/edu/corhuila/synkro/workflow/RegisterSaleTest.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-core/src/test/java/co/edu/corhuila/synkro/workflow/RegisterSaleTest.java); [internal/domain/model/sale_test.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/domain/model/sale_test.go) → implementation: [workflow-core/src/main/java/co/edu/corhuila/synkro/workflow/application/usecase/RegisterSale.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-core/src/main/java/co/edu/corhuila/synkro/workflow/application/usecase/RegisterSale.java); [internal/domain/model/sale.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/domain/model/sale.go) | `synkro-workflow` → `synkro-sales-api` | 🟡 Real saga-driven registration and history/detail reported. Root acceptance stays open; replay/safety documentation belongs to ARQ-28/DOCS-95. [E-SALE](../11-quality/testing-strategy.md#e-sale) |
| FR-006 | Automatically calculate the total amount of a sale | [HU-VEN-06](https://github.com/code-corhuila/synkro-docs/issues/228), [HU-VEN-07](https://github.com/code-corhuila/synkro-docs/issues/233), [HU-VEN-08](https://github.com/code-corhuila/synkro-docs/issues/235), [HU-VEN-09](https://github.com/code-corhuila/synkro-docs/issues/240), [HU-VEN-12](https://github.com/code-corhuila/synkro-docs/issues/246) | [synkro-sales-api.yaml](../07-api/contracts/openapi/synkro-sales-api.yaml) | [internal/domain/model/sale_test.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/domain/model/sale_test.go) → implementation: [internal/domain/model/sale.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/domain/model/sale.go) | `synkro-sales-api` | 🟡 Authoritative integer totals and frozen prices implemented; UI estimates are not authoritative. Data/contract drift remains in DOCS-95. [E-SALE](../11-quality/testing-strategy.md#e-sale) |
| FR-007 | Automatically deduct stock when a sale is registered | [HU-PRO-06](https://github.com/code-corhuila/synkro-docs/issues/193), [HU-PRO-09](https://github.com/code-corhuila/synkro-docs/issues/198), [HU-WKF-03](https://github.com/code-corhuila/synkro-docs/issues/243), [HU-VEN-12](https://github.com/code-corhuila/synkro-docs/issues/246) | [synkro-products-api.yaml](../07-api/contracts/openapi/synkro-products-api.yaml); [synkro-workflow.yaml](../07-api/contracts/openapi/synkro-workflow.yaml) | [workflow-core/src/test/java/co/edu/corhuila/synkro/workflow/RegisterSaleTest.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-core/src/test/java/co/edu/corhuila/synkro/workflow/RegisterSaleTest.java); [internal/application/usecase/stock_test.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/application/usecase/stock_test.go) → implementation: [workflow-core/src/main/java/co/edu/corhuila/synkro/workflow/application/usecase/RegisterSale.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-core/src/main/java/co/edu/corhuila/synkro/workflow/application/usecase/RegisterSale.java); [internal/application/usecase/stock.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/application/usecase/stock.go) | `synkro-workflow` → `synkro-products-api` | 🟡 Reservation/compensation effects have real reported evidence. Ambiguous outcomes and central contract changes retain their pending decision boundary. [E-STOCK](../11-quality/testing-strategy.md#e-stock) |
| FR-008 | Generate daily and monthly sales reports | [HU-VEN-10](https://github.com/code-corhuila/synkro-docs/issues/245) | [synkro-sales-api.yaml](../07-api/contracts/openapi/synkro-sales-api.yaml) | [internal/application/usecase/query_reports_test.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/application/usecase/query_reports_test.go) → implementation: [internal/application/usecase/query_reports.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/application/usecase/query_reports.go) | `synkro-sales-api` | 🟡 Daily/monthly aggregation API and tests delivered; reporting/dashboard UI remains pending. Do not describe this FR as having no implementation HU. [E-REPORTS](../11-quality/testing-strategy.md#e-reports) |
| FR-009 | Generate a report of the best-selling products | [HU-VEN-10](https://github.com/code-corhuila/synkro-docs/issues/245) | [synkro-sales-api.yaml](../07-api/contracts/openapi/synkro-sales-api.yaml) | [internal/application/usecase/query_reports_test.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/application/usecase/query_reports_test.go) → implementation: [internal/application/usecase/query_reports.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/application/usecase/query_reports.go) | `synkro-sales-api` | 🟡 Top-products aggregation API and tests delivered; reporting UI remains pending. [E-REPORTS](../11-quality/testing-strategy.md#e-reports) |
| FR-010 | Authenticate users and restrict by role | [HU-AUTH-03](https://github.com/code-corhuila/synkro-docs/issues/197), [HU-AUTH-07](https://github.com/code-corhuila/synkro-docs/issues/211), [HU-AUTH-08](https://github.com/code-corhuila/synkro-docs/issues/212), [HU-AUTH-09](https://github.com/code-corhuila/synkro-docs/issues/214), [HU-AUTH-10](https://github.com/code-corhuila/synkro-docs/issues/216), [HU-AUTH-11](https://github.com/code-corhuila/synkro-docs/issues/217), [HU-AUTH-13](https://github.com/code-corhuila/synkro-docs/issues/234), [HU-AUTH-14](https://github.com/code-corhuila/synkro-docs/issues/238), [HU-GTW-02](https://github.com/code-corhuila/synkro-docs/issues/231), [HU-FE-08](https://github.com/code-corhuila/synkro-docs/issues/248) | [synkro-auth-api.yaml](../07-api/contracts/openapi/synkro-auth-api.yaml) | [auth-core/src/test/java/co/edu/corhuila/synkro/auth/application/usecase/LoginUseCaseTest.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-core/src/test/java/co/edu/corhuila/synkro/auth/application/usecase/LoginUseCaseTest.java); [auth-app/src/test/java/co/edu/corhuila/synkro/auth/app/TokenAuthenticationHttpTest.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-app/src/test/java/co/edu/corhuila/synkro/auth/app/TokenAuthenticationHttpTest.java) → implementation: [auth-core/src/main/java/co/edu/corhuila/synkro/auth/application/usecase/LoginUseCase.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-core/src/main/java/co/edu/corhuila/synkro/auth/application/usecase/LoginUseCase.java); [auth-app/src/main/java/co/edu/corhuila/synkro/auth/app/AccessTokenAuthenticationFilter.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-app/src/main/java/co/edu/corhuila/synkro/auth/app/AccessTokenAuthenticationFilter.java) | `synkro-auth-api` | 🟡 Real RS256/Auth and Host-owned login exist. Token lifecycle ADRs, administrator recovery and administration UI are distinct pending work. [E-AUTH](../11-quality/testing-strategy.md#e-auth) |

**Note on the "Tests" column:** the rows now identify test files and their relevant implementation paths at frozen revisions. These links establish the source trace; they do not claim that suites were rerun for this documentation change. Pending integration, runtime acceptance and CI evidence remain explicit in each status and the shared acceptance boundary. The Corte 1 monolith is not treated as automated evidence.

**Note on the "Service" column:** the column names the owning delivered microservice or orchestration chain, rather than the Corte 1 monolith. FR-005 and FR-007 enter through synkro-workflow because the sale saga coordinates Products and Sales. A service's existence does not close its remaining acceptance conditions.

**Note on the "Contract" column:** each FR now traces to the OpenAPI contract file and endpoint tag that implements it (`07-api/contracts/openapi/`). This closes the FR → Contract traceability gap.

---

## NFR → Validation matrix

| NFR ID | Category | Key metric | How it is validated | Tool | Status |
|--------|----------|------------|---------------------|------|--------|
| NFR-001 | Performance | P95 < 300ms for critical endpoints | Timeouts/bounds exist; source is not load evidence; [evidence index](../11-quality/testing-strategy.md#executable-evidence-index) | k6 (proposed) | 🔴 No new P95/load result; retain canonical target and pending load verification |
| NFR-002 | Availability | Health checks per service | HTTP/store evidence in Customers/Auth/Sales/Workflow; Products liveness only (TD-017); gateway self-health; [evidence index](../11-quality/testing-strategy.md#executable-evidence-index) | HTTP/store tests; health probes | 🟡 Health exists, with Products own-store/503 gap TD-017. No production availability SLA or completed root acceptance is established |
| NFR-003 | Scalability | Stateless service instances | Domain boundaries and deployment design; [evidence index](../11-quality/testing-strategy.md#executable-evidence-index) | Code review | 🟡 Scalability/load validation remains aspirational; no horizontal-scaling certification |
| NFR-004 | Security | JWT RS256 validated locally + RBAC | E-AUTH, E-AUTH-SESSION, E-HOST, E-GATEWAY, service JWT/RBAC and application-permission tests; [evidence index](../11-quality/testing-strategy.md#executable-evidence-index) | JUnit / Go / Vitest / HTTP checks | 🟡 Stateless JWT revocation/renewal decisions and credential operations retain their actual limits |
| NFR-005 | Observability | Security events logged | Auth rejected/failed registration logs, common error/correlation tests, Workflow process traces; [evidence index](../11-quality/testing-strategy.md#executable-evidence-index) | Log/correlation assertions | 🟡 Some security/correlation behavior is automated; no claim that all services are fully instrumented or monitored |
| NFR-006 | Maintainability | One PR per service, no cross-service coupling | Unit/HTTP/integration suites, architecture checks, CI and preserved coverage gates; [evidence index](../11-quality/testing-strategy.md#executable-evidence-index) | CI / coverage / architecture checks | 🟡 Latest failing CI, review gaps and historical TDD limits remain explicit |
| NFR-007 | Portability | Reproducible Docker containers, two engines (PostgreSQL + MongoDB, ADR-010) | Domain runners/history isolation, Docker delivery and Mongo infrastructure; E-INFRA candidate tests; [evidence index](../11-quality/testing-strategy.md#executable-evidence-index) | Docker / migration integration | 🟡 Two engines exist; root main is not composed yet and HU-INF-02/#239 remains open |
| NFR-008 | Disaster Recovery | RTO/RPO defined | Reported component backup/recovery checks in infrastructure delivery; [evidence index](../11-quality/testing-strategy.md#executable-evidence-index) | Component recovery reports | 🔴 No new production RTO/RPO or disaster-recovery certification |
| NFR-009 | Data Integrity | Zero `DELETE` against business tables; zero document-removal calls in Sales' persistence adapter | Atomic registration/audit, idempotent sale insertion, validator/constraints, permissions and stock concurrency; [evidence index](../11-quality/testing-strategy.md#executable-evidence-index) | Real-engine / permission / concurrency tests | 🟡 SaleDetail.active drift and operational recovery decisions remain owned in DOCS-95/ARQ-28/DOCS-96 |

---

## ADR-014 design-to-verification traceability

Design documentation was merged through [PR #220](https://github.com/code-corhuila/synkro-docs/pull/220), which closed [HU-ARQ-27 — #219](https://github.com/code-corhuila/synkro-docs/issues/219).
[HU-DOCS-91 — #221](https://github.com/code-corhuila/synkro-docs/issues/221) reconciles the catalog, index and tracking statements; it does not deliver the design alignment a second time.

| Requirement | Decision / design delivery | Design artifact | Planned verification | Implementation status |
|---|---|---|---|---|
| FR-010, NFR-004 | ADR-014 Decision 1; [HU-ARQ-27](https://github.com/code-corhuila/synkro-docs/issues/219); [PR #220](https://github.com/code-corhuila/synkro-docs/pull/220) | Auth contract, authentication strategy, User creation rule | AUTH-REG-01/02/03; AUTH-UI-01 | HU-AUTH-13/#234: [E-AUTH](../11-quality/testing-strategy.md#e-auth); user-administration UI remains pending |
| NFR-005, NFR-009 | ADR-014 Decision 1; [HU-ARQ-27](https://github.com/code-corhuila/synkro-docs/issues/219); [PR #220](https://github.com/code-corhuila/synkro-docs/pull/220) | user_creation_audit specification, security policy, observability | AUTH-REG-04; AUTH-AUDIT-01 | HU-AUTH-12/#232 and HU-AUTH-13/#234: [E-AUTH-DB](../11-quality/testing-strategy.md#e-auth-db), [E-AUTH](../11-quality/testing-strategy.md#e-auth) |
| FR-010, NFR-004, NFR-009 | ADR-014 Decision 2; [HU-ARQ-27](https://github.com/code-corhuila/synkro-docs/issues/219); [PR #220](https://github.com/code-corhuila/synkro-docs/pull/220) | initialization runbook, reserved lock key, deployment sequence | AUTH-INIT-01/02/03 | HU-AUTH-14/#238: [E-AUTH-INIT](../11-quality/testing-strategy.md#e-auth-init); operational runbook reconciliation belongs to DOCS-96 |

Test IDs identify the designed verification scenarios. The evidence index now links their actual Auth/CLI test files and run references; a documentation merge alone does not prove execution or accept a pending policy. AUTH-UI-01 remains a separate missing administration screen.

## Frontend session design-to-verification traceability

| Requirements | Documentation HU | Design sources | Planned verification | Implementation evidence |
|---|---|---|---|---|
| FR-010, NFR-004 | [HU-DOCS-92 — #223](https://github.com/code-corhuila/synkro-docs/issues/223) | Browser session, frontend guide, navigation map and wireframes | FE-AUTH-01/02; FE-SESSION-01 through FE-SESSION-07 in `11-quality/testing-strategy.md` | HU-FE-08/#248 supplies real login implementation; [E-HOST](../11-quality/testing-strategy.md#e-host) and [E-AUTH-PORTAL](../11-quality/testing-strategy.md#e-auth-portal) link actual test families. TD-010/011 remain pending; not every scenario is fully real acceptance. |

Development-token scaffolding is distinct from delivered production login. Current evidence is linked above; implementation does not itself accept the renewal/session policy.

## Auth documentation reconciliation and remaining decision gaps

| Requirements | Documentation HU | Design / planned verification | Evidence boundary |
|---|---|---|---|
| FR-010, NFR-004, NFR-009 | [HU-DOCS-93 — #227](https://github.com/code-corhuila/synkro-docs/issues/227) | Auth schema/dictionary/guidelines; AUTH-REFRESH-01/02/03, AUTH-LOGOUT-01/02 and AUTH-SECRETS-01 in `testing-strategy.md` | HU-DOCS-93/#227 is closed through PR #229; later implementation evidence is [E-AUTH-SESSION](../11-quality/testing-strategy.md#e-auth-session). Documentation closure does not accept TD-010/011. |
| FR-010, NFR-004 | [HU-DOCS-93 — #227](https://github.com/code-corhuila/synkro-docs/issues/227) | [Renewal proposal](../07-api/authentication.md#renewal-policy-proposal-and-decision-status); FE-SESSION scenarios | Proposed guidance only; no standalone ADR or accepted-policy claim. TD-010 formalization and TD-011 revocation scope remain pending. |

## Inverse traceability: HU → FR

| HU | Title | FR(s) it implements | Sprint/Week |
|----|-------|---------------------|-------------|
| HU-ARQ-07 | Backend: monolithic MVP setup | FR-001 through FR-007 (Corte 1 monolith) | Week 5 |
| HU-FE-02 | Frontend: monolithic MVP setup | FR-010 (simulated) | Week 5 |
| [HU-INF-01 / #164](https://github.com/code-corhuila/synkro-docs/issues/164) | Common files in every code repository | NFR-006; [E-INFRA](../11-quality/testing-strategy.md#e-infra) | Week 9, issue closed |
| [HU-PRO-03 / #181](https://github.com/code-corhuila/synkro-docs/issues/181) | Base structure of the Products service | NFR-002/004/006; [E-PRODUCTS](../11-quality/testing-strategy.md#e-products) | Week 10 |
| [HU-GTW-01 / #182](https://github.com/code-corhuila/synkro-docs/issues/182) | Gateway routes and its own behavior | FR-010; NFR-002/004/005; [E-GATEWAY](../11-quality/testing-strategy.md#e-gateway) | Week 10 |
| [HU-FE-03 / #183](https://github.com/code-corhuila/synkro-docs/issues/183) | Host: single HTTP client, session and development sign-in | FR-010; NFR-004/005/006; [E-HOST](../11-quality/testing-strategy.md#e-host) | Week 10 |
| [HU-AUTH-01 / #184](https://github.com/code-corhuila/synkro-docs/issues/184) | Base structure of the Auth service | NFR-002/004/006; [E-AUTH](../11-quality/testing-strategy.md#e-auth) | Week 10-11 |
| [HU-WKF-01 / #185](https://github.com/code-corhuila/synkro-docs/issues/185) | Base structure of the workflow | NFR-002/004/006; [E-WORKFLOW](../11-quality/testing-strategy.md#e-workflow) | Week 10-11 |
| [HU-VEN-03 / #186](https://github.com/code-corhuila/synkro-docs/issues/186) | Base structure of the Sales service | NFR-002/006; [E-SALE](../11-quality/testing-strategy.md#e-sale) | Week 10-11 |
| [HU-CLI-01 / #187](https://github.com/code-corhuila/synkro-docs/issues/187) | Base structure of the Customers service | NFR-002/004/006; [E-CUSTOMERS](../11-quality/testing-strategy.md#e-customers) | Week 10-11 |
| [HU-FE-04 / #188](https://github.com/code-corhuila/synkro-docs/issues/188) | Host: portal registry, isolation, layout and not found | FR-010; NFR-004/005/006; [E-HOST](../11-quality/testing-strategy.md#e-host) | Week 10 |
| [HU-WRK-01 / #189](https://github.com/code-corhuila/synkro-docs/issues/189) | Base structure of the worker | NFR-005/006; FR-004 automation still pending; [E-WORKER](../11-quality/testing-strategy.md#e-worker) | Week 10-11 |
| [HU-WKF-02 / #190](https://github.com/code-corhuila/synkro-docs/issues/190) | Fix architecture-check guard and UTF-8 test coverage | NFR-006; [E-WORKFLOW](../11-quality/testing-strategy.md#e-workflow) | Per linked issue; no new week inferred |
| [HU-PRO-04 / #191](https://github.com/code-corhuila/synkro-docs/issues/191) | Base structure of the Products database repository | NFR-004/007/009; [E-PRODUCTS-DB](../11-quality/testing-strategy.md#e-products-db) | Week 10-11 |
| [HU-PRO-05 / #192](https://github.com/code-corhuila/synkro-docs/issues/192) | Category and Product tables | FR-002/003; NFR-009; [E-PRODUCTS-DB](../11-quality/testing-strategy.md#e-products-db) | Per linked issue; no new week inferred |
| [HU-PRO-06 / #193](https://github.com/code-corhuila/synkro-docs/issues/193) | Stock adjustment and reservation tables, plus the idempotency key table | FR-004/007; NFR-009; [E-PRODUCTS-DB](../11-quality/testing-strategy.md#e-products-db) | Per linked issue; no new week inferred |
| [HU-PRO-07 / #194](https://github.com/code-corhuila/synkro-docs/issues/194) | Stock alert table | FR-004; NFR-009; [E-PRODUCTS-DB](../11-quality/testing-strategy.md#e-products-db) | Per linked issue; no new week inferred |
| [HU-PRO-08 / #195](https://github.com/code-corhuila/synkro-docs/issues/195) | Categories and Products endpoints | FR-002/003; NFR-004/009; [E-PRODUCTS](../11-quality/testing-strategy.md#e-products) | Per linked issue; no new week inferred |
| [HU-AUTH-02 / #196](https://github.com/code-corhuila/synkro-docs/issues/196) | Base structure of the Auth database repository | NFR-004/007/009; [E-AUTH-DB](../11-quality/testing-strategy.md#e-auth-db) | Week 10-11 |
| [HU-AUTH-03 / #197](https://github.com/code-corhuila/synkro-docs/issues/197) | Auth issues real RS256-signed tokens | FR-010; NFR-004/005/006/009; [E-AUTH](../11-quality/testing-strategy.md#e-auth) | Per linked issue; no new week inferred |
| [HU-PRO-09 / #198](https://github.com/code-corhuila/synkro-docs/issues/198) | Stock adjustment and reservation endpoints | FR-004/007; NFR-009; [E-STOCK](../11-quality/testing-strategy.md#e-stock) | Per linked issue; no new week inferred |
| [HU-PRO-10 / #199](https://github.com/code-corhuila/synkro-docs/issues/199) | Stock-alert endpoints | FR-004; NFR-004/009; [E-ALERTS](../11-quality/testing-strategy.md#e-alerts) | Per linked issue; no new week inferred |
| [HU-PRO-11 / #200](https://github.com/code-corhuila/synkro-docs/issues/200) | Base structure of synkro-products-portal | NFR-004/006/007; [E-PRODUCTS-PORTAL](../11-quality/testing-strategy.md#e-products-portal) | Week 10 |
| [HU-FE-05 / #201](https://github.com/code-corhuila/synkro-docs/issues/201) | Host client: query parameters, idempotency key and caller cancellation | FR-010; NFR-004/005/006; [E-HOST](../11-quality/testing-strategy.md#e-host) | Per linked issue; no new week inferred |
| [HU-PRO-01 / #202](https://github.com/code-corhuila/synkro-docs/issues/202) | Products portal: list products | FR-002/004; NFR-006; [E-PRODUCTS-PORTAL](../11-quality/testing-strategy.md#e-products-portal) | Week 10-11 |
| [HU-FE-06 / #203](https://github.com/code-corhuila/synkro-docs/issues/203) | Host: design tokens and a ready `synkro-front` | FR-010; NFR-004/005/006; [E-HOST](../11-quality/testing-strategy.md#e-host) | Per linked issue; no new week inferred |
| [HU-PRO-13 / #204](https://github.com/code-corhuila/synkro-docs/issues/204) | Products portal: apply the design system to the list | FR-002; NFR-006; [E-PRODUCTS-PORTAL](../11-quality/testing-strategy.md#e-products-portal) | Per linked issue; no new week inferred |
| [HU-PRO-02 / #205](https://github.com/code-corhuila/synkro-docs/issues/205) | Products portal: register a product | FR-002/003; NFR-009; [E-PRODUCTS-PORTAL](../11-quality/testing-strategy.md#e-products-portal) | Week 10-11 |
| [HU-PRO-12 / #206](https://github.com/code-corhuila/synkro-docs/issues/206) | Products portal: complete the portal (catalogue management, categories, stock lookup and stock alerts) | FR-002/003/004; NFR-004/009; [E-PRODUCTS-PORTAL](../11-quality/testing-strategy.md#e-products-portal) | Week 10 |
| [HU-AUTH-04 / #208](https://github.com/code-corhuila/synkro-docs/issues/208) | `system_user` Table and `auth_reader` Role | FR-010; NFR-004/005/006/009; [E-AUTH-DB](../11-quality/testing-strategy.md#e-auth-db) | Per linked issue; no new week inferred |
| [HU-AUTH-05 / #209](https://github.com/code-corhuila/synkro-docs/issues/209) | `refresh_token` table, foreign key to `system_user` and its index | FR-010; NFR-004/005/006/009; [E-AUTH-DB](../11-quality/testing-strategy.md#e-auth-db) | Per linked issue; no new week inferred |
| [HU-AUTH-06 / #210](https://github.com/code-corhuila/synkro-docs/issues/210) | `service_token` and `idempotency_key` tables, with the service-token foreign key and index | FR-010; NFR-004/005/006/009; [E-AUTH-DB](../11-quality/testing-strategy.md#e-auth-db) | Per linked issue; no new week inferred |
| [HU-AUTH-07 / #211](https://github.com/code-corhuila/synkro-docs/issues/211) | Real token validation and role checks in `synkro-auth-api` | FR-010; NFR-004/005/006/009; [E-AUTH](../11-quality/testing-strategy.md#e-auth) | Per linked issue; no new week inferred |
| [HU-AUTH-08 / #212](https://github.com/code-corhuila/synkro-docs/issues/212) | PostgreSQL persistence for users and refresh tokens in `synkro-auth-api` | FR-010; NFR-004/005/006/009; [E-AUTH](../11-quality/testing-strategy.md#e-auth) | Per linked issue; no new week inferred |
| [HU-AUTH-09 / #214](https://github.com/code-corhuila/synkro-docs/issues/214) | User registration, user lookup and the common error envelope in `synkro-auth-api` | FR-010; NFR-004/005/006/009; [E-AUTH](../11-quality/testing-strategy.md#e-auth) | Per linked issue; no new week inferred |
| [HU-AUTH-10 / #216](https://github.com/code-corhuila/synkro-docs/issues/216) | Logout and refresh-token reuse detection in `synkro-auth-api` | FR-010; NFR-004/005/006/009; [E-AUTH-SESSION](../11-quality/testing-strategy.md#e-auth-session) | Per linked issue; no new week inferred |
| [HU-AUTH-11 / #217](https://github.com/code-corhuila/synkro-docs/issues/217) | Service tokens in `synkro-auth-api` | FR-010; NFR-004/005/006/009; [E-AUTH](../11-quality/testing-strategy.md#e-auth) | Per linked issue; no new week inferred |
| [HU-VEN-04 / #224](https://github.com/code-corhuila/synkro-docs/issues/224) | Sales migration skeleton — Liquibase runner over MongoDB | FR-005/006/007; NFR-004/009; [E-SALES-DB](../11-quality/testing-strategy.md#e-sales-db) | Per linked issue; no new week inferred |
| [HU-VEN-05 / #226](https://github.com/code-corhuila/synkro-docs/issues/226) | Sale collection, validator and permissions | FR-005/006/007; NFR-004/009; [E-SALES-DB](../11-quality/testing-strategy.md#e-sales-db) | Per linked issue; no new week inferred |
| [HU-VEN-06 / #228](https://github.com/code-corhuila/synkro-docs/issues/228) | Sale/SaleDetail domain aggregate | FR-005/006/007; NFR-004/009; [E-SALE](../11-quality/testing-strategy.md#e-sale) | Per linked issue; no new week inferred |
| [HU-VEN-16 / #230](https://github.com/code-corhuila/synkro-docs/issues/230) | Harden Sales MongoDB Migrations and CI Failure Detection | NFR-004/007/009; [E-SALES-DB](../11-quality/testing-strategy.md#e-sales-db) | Per linked issue; no new week inferred |
| [HU-GTW-02 / #231](https://github.com/code-corhuila/synkro-docs/issues/231) | Public login/refresh, protected register | FR-010; NFR-002/004/005; [E-GATEWAY](../11-quality/testing-strategy.md#e-gateway) | Per linked issue; no new week inferred |
| [HU-AUTH-12 / #232](https://github.com/code-corhuila/synkro-docs/issues/232) | Transactional User-Creation Audit Schema | FR-010; NFR-004/005/006/009; [E-AUTH-DB](../11-quality/testing-strategy.md#e-auth-db) | Per linked issue; no new week inferred |
| [HU-VEN-07 / #233](https://github.com/code-corhuila/synkro-docs/issues/233) | Idempotent Sale Registration Use Case and Ports | FR-005/006/007; NFR-004/009; [E-SALE](../11-quality/testing-strategy.md#e-sale) | Per linked issue; no new week inferred |
| [HU-AUTH-13 / #234](https://github.com/code-corhuila/synkro-docs/issues/234) | Admin-Only Registration with Transactional Audit | FR-010; NFR-004/005/006/009; [E-AUTH](../11-quality/testing-strategy.md#e-auth) | Per linked issue; no new week inferred |
| [HU-VEN-08 / #235](https://github.com/code-corhuila/synkro-docs/issues/235) | MongoDB Sale Persistence Adapter, BSON Mapping and Replica Set Integration | FR-005/006/007; NFR-004/009; [E-MONGO](../11-quality/testing-strategy.md#e-mongo) | Per linked issue; no new week inferred |
| [HU-INF-04 / #236](https://github.com/code-corhuila/synkro-docs/issues/236) | Persistent MongoDB Instance and Integration with Sales Migrations | NFR-004/006/007/009; [E-MONGO-INFRA](../11-quality/testing-strategy.md#e-mongo-infra) | Per linked issue; no new week inferred |
| [HU-VEN-17 / #237](https://github.com/code-corhuila/synkro-docs/issues/237) | Protect Credentials in the MongoDB Migration Runner | NFR-004/007; [E-SALES-DB](../11-quality/testing-strategy.md#e-sales-db) | Per linked issue; no new week inferred |
| [HU-AUTH-14 / #238](https://github.com/code-corhuila/synkro-docs/issues/238) | Audited Initial Administrator CLI | FR-010; NFR-004/005/006/009; [E-AUTH-INIT](../11-quality/testing-strategy.md#e-auth-init) | Per linked issue; no new week inferred |
| [HU-INF-02 / #239](https://github.com/code-corhuila/synkro-docs/issues/239) | Persistent PostgreSQL Infrastructure and SynkroTech Root Composition | NFR-004/006/007/009; [E-INFRA](../11-quality/testing-strategy.md#e-infra) | Week 10 |
| [HU-VEN-09 / #240](https://github.com/code-corhuila/synkro-docs/issues/240) | Sales HTTP Endpoints and Role-Based Authorization | FR-005/006/007; NFR-004/009; [E-SALE](../11-quality/testing-strategy.md#e-sale) | Per linked issue; no new week inferred |
| [HU-VEN-11 / #241](https://github.com/code-corhuila/synkro-docs/issues/241) | Sales Portal Remote Integration and Real Sales History | FR-005; NFR-004/006; [E-SALES-PORTAL](../11-quality/testing-strategy.md#e-sales-portal) | Per linked issue; no new week inferred |
| [HU-FE-07 / #242](https://github.com/code-corhuila/synkro-docs/issues/242) | Restore Remote Isolation When Switching Portals | FR-010; NFR-004/005/006; [E-HOST](../11-quality/testing-strategy.md#e-host) | Per linked issue; no new week inferred |
| [HU-WKF-03 / #243](https://github.com/code-corhuila/synkro-docs/issues/243) | Persistent and Recoverable Sales Registration Orchestration | FR-005/007; NFR-002/004/005/007/009; [E-WORKFLOW](../11-quality/testing-strategy.md#e-workflow) | Per linked issue; no new week inferred |
| [HU-CLI-02 / #244](https://github.com/code-corhuila/synkro-docs/issues/244) | Complete Customers schema and migration runner | FR-001; NFR-004/006/007/009; [E-CUSTOMERS-DB](../11-quality/testing-strategy.md#e-customers-db) | Week 10-11 |
| [HU-VEN-10 / #245](https://github.com/code-corhuila/synkro-docs/issues/245) | Daily, Monthly and Top-Products Sales Aggregation Reports | FR-008/009; NFR-004/009; [E-REPORTS](../11-quality/testing-strategy.md#e-reports) | Per linked issue; no new week inferred |
| [HU-VEN-12 / #246](https://github.com/code-corhuila/synkro-docs/issues/246) | Real Sale Registration Through Workflow and Composed Sale Detail | FR-005/006/007/010; NFR-005/006; [E-SALES-PORTAL](../11-quality/testing-strategy.md#e-sales-portal) | Per linked issue; no new week inferred |
| [HU-CLI-03 / #247](https://github.com/code-corhuila/synkro-docs/issues/247) | Functional Customers API with Persistence, Security, and Idempotent Registration | FR-001; NFR-004/006/007/009; [E-CUSTOMERS](../11-quality/testing-strategy.md#e-customers) | Per linked issue; no new week inferred |
| [HU-FE-08 / #248](https://github.com/code-corhuila/synkro-docs/issues/248) | Auth Portal and Production Authentication Through the Host | FR-010; NFR-004/005/006; [E-AUTH-PORTAL](../11-quality/testing-strategy.md#e-auth-portal) | Per linked issue; no new week inferred |
| [HU-CLI-04 / #249](https://github.com/code-corhuila/synkro-docs/issues/249) | Complete Customers Portal and Host Integration | FR-001; NFR-004/006/007/009; [E-CUSTOMERS-PORTAL](../11-quality/testing-strategy.md#e-customers-portal) | Per linked issue; no new week inferred |

Planned VEN-01/02 and INF-03 have no assigned issue and are reconciled in the [skip/substitution register](../00-governance/agile-conventions.md#skips-aliases-and-substitutions), rather than listed as delivered issues.

**Note:** documentation stories (HU-PDR-*, HU-ADR-*, HU-DOCS-*, HU-ARQ-*)
do not implement requirements in code and are not listed here. Code
stories use the `AUTH`, `CLI`, `PRO`, `VEN`, `FE`, `INF`, `GTW`, `WKF`
and `WRK` prefixes defined in `00-governance/agile-conventions.md`,
"Story identifiers".

---

## Status legend

| Status | Meaning |
|--------|---------|
| ✅ Done | Stated scope implemented, reviewed and supported by sufficient acceptance evidence for its identified delivery stage |
| 🟡 In progress | Implementation exists; part of the required behavior, acceptance, evidence or documentation remains open. This does not imply automated tests are absent |
| 🔴 Pending | No implementation evidence for the stated scope |
| ⏸ Blocked | Has an external blocker |
| ❌ Cancelled | Removed from scope |

---

## Identified gaps (requirements without coverage)

> Updated for Week 12 as part of HU-DOCS-94. Missing required behavior stays explicit; a closed issue, main commit or tag alone does not close the gap.

| Gap type | Description | Required action | Owner | When |
|----------|-------------|----------------|-------|------|
| Architecture / policy | Workflow replay/safe rejection/retry/advisory namespaces and Host session policy | Reviewed decision(s), next ADR-015, exact modified sections and actual acceptance | HU-ARQ-28/#254 — AsolanoT | Week 12 coordination; implementation per assigned issue |
| Domain / contracts | V017 receipt and health/error URL definitions reconciled; TD-012–018 keep unsupported lifecycle, safety, locks, resilience, retry and Products-store-health gaps explicit | Reviewed correction, validated contracts and explicit dependent decision; implementation closing evidence stays assigned | HU-DOCS-95/#253 — SergioAndres17; ARQ-28 owns decision-dependent sections | Week 12; prepared, not a completion claim |
| Root / operations | Root composition, revision pins, current CI and release/operation procedure | Applicable owner fixes, exact-root evidence, review and truthful release/control record | HU-INF-02/#239; HU-DOCS-96/#255 — AsolanoT / SantiagoPlazas2005 | Week 12 coordination; implementation per assigned issue |
| Portal verification | R-008 routing risk, portal flows and real catalog | Route/deep-link/back-forward evidence and consistent UX/diagram/service records | HU-DOCS-97/#256 — AsolanoT | Week 12 coordination; implementation per assigned issue |
| Required Worker behavior | Missing Worker low-stock job — FR-004 Must Have | Refine and assign a separate implementation story at Planning; real Products effect tests must cover the business job. No new issue or ID is assigned by this table | Proposed implementation planning owner: SantiagoPlazas2005, current WRK-01 assignee; DOCS-96/97 coordinate its visibility | Week 12 coordination; implementation per assigned issue |
| Required reporting UI | Reporting/dashboard UI — FR-008/009 Must Have | Confirm the remaining UI scope with the Product Owner, refine and assign implementation work; do not downgrade the canonical Must Have priority or treat API delivery as UI acceptance | Proposed UI planning owner: AsolanoT; JordanRG420 supplies the existing VEN-10 API evidence | Next applicable Planning |
| Administration scope | Auth administration UI | Explicit scope/contract decision and assigned implementation evidence; no new feature IDs invented here | Existing scope questions; proposed planning coordinator: AsolanoT | Next applicable Planning |
| Release evidence | Missing v2.0.0 tags: Worker and PostgreSQL Infra | Verify intended main content and required delivery gates before publishing a tag. PostgreSQL Infra main is still scaffold; do not tag it as composed delivery to conceal HU-INF-02's open scope | DOCS-96/#255 — SantiagoPlazas2005 records release evidence; affected repository coordination: SantiagoPlazas2005 / AsolanoT | Week 12 coordination; implementation per assigned issue |
| Acceptance evidence | Closed story with residual review, TDD or CI limitation | Preserve closure and limitation separately; no fabricated historical green or approval | Dated verification snapshot and responsible follow-up above | Next applicable Planning |
| NFR measurement | Load/SLA/full monitoring claims | Measured results and agreed scope; no inference from health/configuration | Existing NFR/control owners | Next applicable Planning |

---

## HU-02 (Discovery) → Repository Traceability

> This section responds to the professor's HU-02 ("Project Discovery"). It
> is not a new feature or a new file per task — it is the evidence that
> each of its 10 tasks is already resolved in the repo, so nothing gets
> duplicated across folders.

| HU-02 Block | Task | Where it's resolved | Status |
|---|---|---|---|
| Context Analysis | Describe the problem | `03-product/problem-framing.md`, §1 | ✅ Done |
| Context Analysis | Target users (persona) | `03-product/problem-framing.md`, §2 + `01-context/overview.md`, "Main Users" | ✅ Done |
| Context Analysis | Market research | `03-product/problem-framing.md`, §9 (Alegra, Loyverse) | ✅ Done |
| MVP Definition | Candidate features | `01-context/scope.md`, "MVP Scope" | ✅ Done |
| MVP Definition | Corte 1 scope (what's in) | `01-context/scope.md`, "MVP Scope" | ✅ Done |
| MVP Definition | What's out of scope | `01-context/scope.md`, "Explicitly Out of Scope" + `problem-framing.md`, §8 | ✅ Done |
| Initial Design | Wireframes/mockups | `12-ux-ui/wireframes.md` | ✅ Done |
| Initial Design | Initial data model | `02-domain/domain-map.md` + `entities-and-rules.md` | ✅ Done |
| Initial Design | Main user flows | `12-ux-ui/navigation-map.md`, "Main user flows" (Flow 1-5) | ✅ Done |
| Documentation | Consolidate everything in `docs` | This same table — each piece lives in its correct folder per that folder's own README | ✅ Done |

**Why this table lives here and not elsewhere:** `traceability-matrix.md`
is exactly the file the professor's own framework defines to connect an
external requirement to where its evidence lives in the repo — it is the
correct place, rather than creating a new document just for this.

---

### HU-DOCS-95 contract and representation checks

| Requirement / behavior | Implementation HU / evidence | Documentation / discrepancy | Acceptance boundary |
|---|---|---|---|
| FR-004 adjustment replay | HU-PRO-09; [internal/adapter/out/persistence/postgres_stock_integration_test.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/adapter/out/persistence/postgres_stock_integration_test.go) → [internal/adapter/out/persistence/postgres_stock.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/adapter/out/persistence/postgres_stock.go) | V017 stock_after / response currentStock; models and Products contract | Stored snapshot preserved for new rows; legacy NULL fallback remains explicit |
| FR-005/006 sale representation | HU-VEN-06/08/09; [internal/adapter/out/persistence/sale_document.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/out/persistence/sale_document.go) | Existing active flags retained; [TD-012](../15-project-control/technical-backlog.md#td-012--sale-and-detail-lifecycle-restoration) | Inactive root/detail restoration remains unsupported; no domain lifecycle invented |
| FR-005/007 saga replay/recovery | HU-WKF-03; [workflow-app/src/test/java/co/edu/corhuila/synkro/workflow/app/WorkflowHttpTest.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-app/src/test/java/co/edu/corhuila/synkro/workflow/app/WorkflowHttpTest.java); [workflow-adapters/src/test/java/co/edu/corhuila/synkro/workflow/adapter/out/http/HttpSaleParticipantsTest.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-adapters/src/test/java/co/edu/corhuila/synkro/workflow/adapter/out/http/HttpSaleParticipantsTest.java) | Workflow contract candidate; TD-013/014/015/016 | ARQ-28 decision must be accepted before dependent publication; ambiguity never proves safe release |
| FR-008/009 UTC/owner report aggregation | HU-VEN-10; [internal/adapter/out/persistence/reports.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/out/persistence/reports.go); [internal/adapter/in/httpapi/report_query.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/in/httpapi/report_query.go) | Domain report note and Sales contract | Integer/frozen values, ownership and group-before-page semantics; UI remains pending |
| NFR-002/004/006 HTTP health/errors | API health/controller/error-advice sources; testing-strategy evidence | Five operation-specific health servers; explicit 400/500; TD-017 | Schema/URL checks do not execute services or establish Products store health |

HU-DOCS-94 is merged through PR #257 and #252 is closed. HU-DOCS-95 remains prepared for review; seven tracked discrepancies are not accepted/deferred improvements or new HU assignments.

## How to maintain this matrix

1. When an HU is created: add the row in the FR → HU → Test → Service section
2. When a test is written: note the file in the "Tests that verify it" column
3. When implementation and acceptance evidence satisfy the stated scope: update the status. Keep issue closure, CI, review and incomplete acceptance separate
4. At each Sprint Planning: review gaps and assign actions

---

## Correlations

- User Stories → `04-requirements/user-stories.md`
- Non-Functional Requirements → `04-requirements/non-functional.md`
- Functional Requirements → `04-requirements/functional.md`
- Testing strategy → `11-quality/testing-strategy.md`
- DoD that determines when an HU is Done → `00-governance/definition-of-done.md`
- The professor's original user story → HU-02, delivered in the Weekly
- Detail of each HU-02 piece → `03-product/problem-framing.md`, `01-context/scope.md`, `02-domain/`, `12-ux-ui/`
