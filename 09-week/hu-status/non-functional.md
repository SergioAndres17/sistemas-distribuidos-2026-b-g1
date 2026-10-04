# Non-Functional Requirements (NFR)

> NFRs define the **qualities of the system** — not what it does but how well it does it.
> The golden rule: every NFR must have a metric. "The system must be fast" is not an NFR.

**Legend used below:**
- ✅ **Already decided** — sourced from an existing document, cited inline
- 🔵 **Proposed default** — not yet ratified by the team; adopt or change it
- 🔮 **Aspirational (Cut 2/3)** — depends on infrastructure (Kubernetes, a real Production environment) that doesn't exist yet for this course project

---

## NFR-001: Performance

| Attribute | Metric | Status |
|-----------|--------|--------|
| P95 latency — critical endpoints | < 300ms under normal load | 🔵 Proposed default |
| P95 latency — non-critical endpoints | < 1000ms | 🔵 Proposed default |
| Minimum throughput / max RPS | Not yet defined | 🔵 No capacity planning done — academic project, no real traffic estimate exists |

**Critical endpoints (✅ justified by an existing decision, not arbitrary):**
- `GET /api/v1/products/{id}` — called on every single sale to check stock/price; this is the exact reason `synkro-products-api` was chosen to be built in Go ("high-frequency reads" — `01-context/overview.md`, Technology Stack)
- `POST /api/v1/sagas/register-sale` — the core revenue transaction; `synkro-workflow` orchestrates calls to `synkro-customers-api`, `synkro-products-api` and `synkro-sales-api` (ADR-007)

**Load testing tools:** k6 (🔵 proposed — not yet adopted by the team)

---

## NFR-002: Availability

| Environment | Status |
|-------------|--------|
| Local | ✅ Active (`01-context/scope.md`, Project Environments) |
| Development | ✅ Active |
| Staging | ✅ "Planned — pending a decision" (`01-context/scope.md`) — no SLO applies yet |
| Production | 🔮 Doesn't exist as a planned environment for this course project — Aspirational, Cut 2/3 |

**Health checks (🔵 proposed, standard practice, not yet built):**
- `GET /health` — liveness
- `GET /health/ready` — readiness (DB connected)

No SLO/error-budget numbers are defined here — they would only be meaningful once a Production environment is actually decided.

---

## NFR-003: Scalability

🔮 **Entirely aspirational — Cut 2/3.** Autoscaling, horizontal-scaling limits, and load-spike behavior all assume a Kubernetes deployment, which contradicts the current, established infrastructure decision: Docker / Docker Compose for the 8 deployable components + the shared PostgreSQL instance, local environment only (`01-context/overview.md`, Infrastructure row).

**Design principle that already applies today (✅):** each service instance should avoid storing state in memory, so it can be scaled later without a rewrite. State goes in PostgreSQL (the only datastore the team has actually adopted — no Redis has been decided anywhere in the project).

---

## NFR-004: Security

### Authentication and Authorization
- RBAC roles: `ADMIN`, `SALESPERSON`, `INVENTORY` ✅ (`01-context/overview.md`, Main Users; `00-governance/security-policy.md`)
- Roles are included in the JWT as the claim `roles: [...]` ✅ (`00-governance/security-policy.md`)
- Every service that receives requests validates the RS256 JWT locally with `synkro-auth-api`'s public key; no service trusts forwarded `X-User-Id`/`X-User-Roles` headers — identity comes only from the validated token. The gateway checks that a credential is present but does not validate it ✅ (ADR-006, supersedes ADR-003 §2)
- JWT/refresh token expiration times: 🔵 not yet specified by the team — template default is 1h access / 7d refresh

### Data transmission
- HTTPS mandatory in all environments except local; minimum TLS 1.2, TLS 1.3 recommended ✅ (`00-governance/security-policy.md`)

### Sensitive data
- Passwords: bcrypt ✅ (`00-governance/security-policy.md`) — exact cost factor 🔵 not specified, template default ≥ 12
- Secrets/keys: only in environment variables, never in source code, never in a committed `.env`, never in logs ✅ (`00-governance/security-policy.md`, Secret Management)
- Per-schema DB credentials (`<DOMAIN>_APP_USER` / `<DOMAIN>_APP_PASSWORD` for each of auth, customers, products, sales and workflow), injected via environment variables; each service connects only to its own schema in the shared instance ✅ (`00-governance/security-policy.md`, ADR-009, `deployment.md` §6)
- Data isolation by schema and GRANT permissions: each service's credentials grant access only to its own schema (`<domain>_writer`); no cross-schema queries are possible, even within the same PostgreSQL instance. CI rebuild checks verify GRANT correctness ✅ (ADR-009, ADR-005 Decision 3, `security-threat-model.md` T-3)

- DB password rotation: every 6 months, or immediately if compromise is suspected ✅ (`00-governance/security-policy.md`)

### OWASP Top 10 (✅ already has real, specific controls — not a generic checklist)

| Vulnerability | Implemented control |
|---------------|----------------------|
| A01: Broken Access Control | RBAC + permission validation in every service |
| A02: Cryptographic Failures | TLS 1.2+, bcrypt for passwords, RS256 for JWT |
| A03: Injection | Prepared SQL statements, schema validation |
| A05: Security Misconfiguration | Review of default values before every release |
| A07: Authentication Failures | JWT with rotation, minimum schema permissions per service |
| A09: Logging Failures | Logs without PII, security events recorded |

### Regulatory compliance
🔮 Not applicable — academic project, no real regulatory scope was ever defined (no GDPR/PCI-DSS/Habeas Data requirement in `01-context/scope.md`).

---

## NFR-005: Observability

**Already decided (✅) — security event logging** (`00-governance/security-policy.md`):

Events always logged: `auth.login.success`, `auth.login.failure`, `auth.token.revoked`, `auth.unauthorized_access_attempt`, `admin.role.changed`
Required fields: `userId` (or `ANONYMOUS`), `action`, `resource`, `result`, `timestamp`

**Everything else is 🔮 aspirational (Cut 2/3):** structured JSON logging beyond security events, correlation IDs, Prometheus/Grafana metrics, distributed tracing, and alerting all assume operational infrastructure the team hasn't built or decided on yet.

---

## NFR-006: Maintainability

**Already decided (✅):** each of the 4 bounded contexts must evolve independently, without forcing changes on the others — this is the explicit justification for choosing Hexagonal Architecture (`01-context/overview.md`, Internal Architecture row; this is what was informally referenced elsewhere as "NFR-03").
**Metric:** a PR that changes one service's code never also needs to change another service's code.

| Metric | Status |
|--------|--------|
| Test coverage | 🔵 Not yet decided — TDD (red→green→refactor) is Pillar 1 of the whole project, but no % threshold has been set |
| Onboarding time | 🔵 Planned to be documented in `10-devops/local-setup.md` (not created yet) |

---

## NFR-007: Portability

- All services (4 domain APIs, synkro-workflow, synkro-worker, synkro-api-gateway, synkro-front) run as Docker containers via Docker Compose, reproducible in the Local environment ✅ (`01-context/overview.md`, Infrastructure; `deployment.md` §9)
- One PostgreSQL instance per environment with schema-per-domain isolation; each service connects only to its own schema, so it can be deployed independently without sharing database state ✅ (ADR-009, `deployment.md` §4)
- Kubernetes 1.28+ compatibility 🔮 Aspirational — Cut 2/3, not the current infrastructure decision
- Environment variables are the only source of environment-specific configuration ✅ (consistent with `00-governance/security-policy.md`'s Secret Management rule)

---

## NFR-008: Disaster Recovery (DR)

🔮 **Entirely aspirational — Cut 2/3.** RTO/RPO targets, database failover, and region-level recovery all assume a Production environment and infrastructure maturity that don't exist for this course project (see NFR-002). Nothing here is defined today.

---

## NFR-009: Data Integrity & Traceability *(added — doesn't fit the 8 standard categories above)*

**Already decided (✅):** no business record is ever physically deleted — deactivation only, via the `active` boolean flag, preserving operational history (`01-context/scope.md`, MVP Scope item 8; this is what was informally referenced elsewhere as "NFR-004").

**Metric:** zero `DELETE` SQL statements against business tables in application code.
**How measured:** code review / static grep for `DELETE FROM` outside of test fixtures.

---

## NFR priority matrix

| NFR | Priority | Validated today? | Owner |
|-----|----------|--------------------|-------|
| NFR-004 Security | P1 | Partially — controls are defined, no automated scanning (SAST/DAST) set up yet | Whole team |
| NFR-006 Maintainability | P1 | Partially — TDD is a course pillar, but no coverage threshold is enforced in CI yet | Whole team |
| NFR-009 Data Integrity | P1 | Not automated — currently a manual code-review check | Whole team |
| NFR-001 Performance | P2 | No — no load-testing infra exists | Unassigned |
| NFR-007 Portability (Docker + schema isolation) | P2 | Partially — Docker Compose runs locally; schema isolation designed (ADR-009) but not yet built | Whole team |
| NFR-005 Observability (security logging part) | P2 | Not automated yet | Unassigned |
| NFR-002 Availability | P3 | N/A — no Production environment exists | Unassigned |
| NFR-003 Scalability | P3 — Aspirational (Cut 2/3) | No | Unassigned |
| NFR-005 Observability (full stack) | P3 — Aspirational (Cut 2/3) | No | Unassigned |
| NFR-007 Portability (Kubernetes part) | P3 — Aspirational (Cut 2/3) | No | Unassigned |
| NFR-008 Disaster Recovery | P3 — Aspirational (Cut 2/3) | No | Unassigned |

---

## NFR Reclassification Record

During the identifier unification (HU-DOCS-25), five NFR citations
across the repository were found to point to the **wrong category**, not
just the wrong digit format. These were not editorial corrections — they
were substantive reclassifications that changed which quality attribute
a document was referencing.

The root cause was the same in all five cases: the original citations
used the PDR's external numbering (`RNF-01` through `RNF-07`), which
mapped to different categories than the repository's own `NFR-001`
through `NFR-009`. When the PDR was disconnected, those stale numbers
were carried forward without verifying that they pointed to the correct
content.

| Document | Old citation | Pointed to (wrong) | Corrected to | Correct category |
|----------|-------------|---------------------|-------------|------------------|
| `01-context/scope.md` (soft-deletion row) | `NFR-004` | Security | `NFR-009` | Data Integrity & Traceability |
| `05-architecture/overview.md` (P5) | `NFR-004` | Security | `NFR-009` | Data Integrity & Traceability |
| `06-data/data-dictionary.md` (`active` row) | `NFR-004` | Security | `NFR-009` | Data Integrity & Traceability |
| `01-context/overview.md` (Communication row) | `NFR-007` | Portability | `NFR-004` | Security |
| `01-context/overview.md` (Internal Architecture row) | `NFR-003` | Scalability | `NFR-006` | Maintainability |

**Approved by:** the development team as part of HU-DOCS-25, reviewed and merged by the Product Owner (professor) in [PR #17 — "docs(requirements): unify FR/NFR identifiers and remove PDR dependency"](https://github.com/code-corhuila/synkro-docs/pull/17). The reclassifications were documented in the PR description but not in a standalone record — this section corrects that gap per the professor's feedback.

---

## Correlations

- Security checklist → `00-governance/security-policy.md`
- Technology and infrastructure decisions → `01-context/overview.md`, ADR-001
- Traced to test cases (once they exist) → `04-requirements/traceability-matrix.md`
- Pipeline that would validate these NFRs → `10-devops/README.md` (not yet created)
