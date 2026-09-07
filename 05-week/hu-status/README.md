<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       04-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 05

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Sergio Andres Ordoñez Diaz
- GITHUB_USER: SergioAndres17
- TEAM: Group - synkro-tech
- SPRINT_GOAL: Close the professor's S00/S06/S12 rubric feedback, formally answer the professor's HU-01 (Technology Stack Selection) and HU-02 (Project Discovery) by auditing existing documentation before writing anything new, and deliver the Corte 1 MVP in the dedicated `synkro-tech` repository.
<!-- CONFIG-END -->

## Docs Repository

| Board Name          | URL                                              |
| -------------------- | ------------------------------------------------ |
| synkro-docs Repository | https://github.com/code-corhuila/synkro-docs.git |

## Team Members

| Full Name                          | GitHub User                                                 |
| ----------------------------       | ------------------------------------------                  |
| Sergio Andres Ordoñez Diaz         | https://github.com/SergioAndres17                           |
| Fredman Santiago Plazas Artunduaga | https://github.com/SantiagoPlazas2005                       |
| Jordan Ramirez Gallego             | https://github.com/JordanRG420                              |
| Angel Gustavo Solano Trujillo      |  https://github.com/AsolanoT                                |

## 1. User stories worked this week

| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-DOCS-15 | Data model per microservice (S06) + data dictionary + modeling conventions | done | `06-data/models.md`, `data-dictionary.md`, `modeling-conventions.md` |
| HU-DOCS-20 | `overview.md` refinement: Backend alternatives (Java and Go) | done | `01-context/overview.md`, "Alternatives Considered" |
| HU-ARQ-07 | Backend: configuration and refinement of the monolithic MVP | done | repo `synkro-tech` (backend) |
| HU-ARQ-08 | QA and merge of the Corte 1 MVP to `main` (with @AsolanoT) | done | repo `synkro-tech` (`main`) + `docs/12-ux-ui/mvp-synkro-tech/` |

## 2. My individual contribution

**Context:** the professor's rubric marked `06-data` as 🔴 — `models.md`
was the framework scaffold, unmodified. Filling it in surfaced a real
naming inconsistency in the project that had gone unnoticed for weeks, and
later in the sprint I moved into the actual Corte 1 backend build.

**HU-DOCS-15 — Data model, data dictionary, modeling conventions:**
- I filled `06-data/models.md` for all 4 schemas (`auth`, `customers`,
  `products`, `sales`) directly from ADR-001's "Data Model per Schema"
  section, adding the technical detail ADR-001 deliberately left open:
  column types, constraints, indexes, and an ER diagram per schema.
- While writing the `role` column's `CHECK` constraint, I found a real
  inconsistency: ADR-001 fixes the role value as `SALES`, but
  `security-policy.md`, `non-functional.md`, and **both** real codebases
  already used `SALESPERSON`. I used `SALESPERSON` in the schema, flagged
  for the team rather than resolved unilaterally.
- I also filled `data-dictionary.md` and `modeling-conventions.md`, two
  files `06-data/README.md` already listed as pending ⭐ — folded into this
  same HU instead of opening a new one.

**HU-DOCS-20 — Backend alternatives (Spring Boot vs. Quarkus, Go vs. Node.js):**
- Compared Spring Boot against Quarkus for Auth/Customers, concluding
  Spring Boot fits better because those services run continuously —
  Quarkus's cold-start advantage doesn't apply to services that never
  restart on demand.
- Compared Go against Node.js for Products/Sales, anchored on RF-07 (stock
  must update automatically and safely under concurrent sales) rather than
  team preference, concluding Go's concurrency model fits better and
  satisfies the course's two-language backend requirement.

**HU-ARQ-07 — Backend configuration (Corte 1 MVP):**
- Moved database credentials to environment variables with a `.env.example`
  committed, so no secret is hardcoded.
- Renamed `tax_id` → `identity_document` across the entire Customers module
  (entity, DTOs, repository, service) to align the code with what
  `06-data/models.md` already specified.
- Added the `registration_date` field to `Customer`, matching the data
  model spec.
- Rewrote the backend `README.md` with real setup instructions,
  prerequisites, and an environment-variable reference table.

**HU-ARQ-08 — QA and merge to `main` (joint with Angel):**
- Validated `develop → qa → main` end to end: customer, product, category,
  and sales CRUD; sale registration with stock validation and automatic
  deduction; soft delete on every entity.
- After merging to `main`, synced the final backend + frontend content
  into `docs/12-ux-ui/mvp-synkro-tech/`, replacing the older copy that
  still had `tax_id` and the pre-Crimson-Circuit palette.
- One known, non-blocking defect carried into this delivery: the sidebar
  and login logo render inside the old text-box CSS rule instead of a
  proper image-sized one (squished/boxed logo). Filed as a quick follow-up
  fix, not treated as a merge blocker.

## 3. Blockers and risks

- None blocking. The logo CSS issue noted above is cosmetic and scheduled
  as an immediate follow-up, not a functional defect.
- The `SALES` vs. `SALESPERSON` naming gap is resolved in code and in
  `06-data/models.md`, but `ADR-001` itself still says `SALES` — the team
  should decide whether a short `ADR-002` is worth raising to formally
  record the correction.

## 4. Plan for next week

- Apply the small logo CSS fix in `synkro-tech` and re-sync
  `12-ux-ui/mvp-synkro-tech/`.
- Support the team in deciding whether `ADR-002` is needed for the role
  naming correction.
- Start scoping backend work for Corte 2 once the professor confirms next
  milestones (real JWT auth, sales reports population).

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [x] Per-environment HU branch + PR to that environment — `develop → qa → main` followed in `synkro-tech`; direct commit to `main` for `docs` per `documentation-rules.md`
- [x] Testable acceptance criteria
- [x] Tests added/updated (unit / integration)
- [x] DDD / hexagonal boundaries respected (domain has no I/O) — not applicable to this Corte 1 monolith spike; hexagonal boundaries are ADR-001's target-architecture scope
- [x] No secrets; config via environment variables

## 6. Evidence links
- Data model: [`models.md`](./docs/06-data/models.md)
- Data dictionary: [`data-dictionary.md`](./docs/06-data/data-dictionary.md)
- Modeling conventions: [`modeling-conventions.md`](./docs/06-data/modeling-conventions.md)
- Backend alternatives: [`overview.md`](./docs/01-context/overview.md), "Alternatives Considered" → Backend subsections
- Corte 1 MVP backend: [`synkro-tech`](https://github.com/code-corhuila/synkro-tech) (`main`)
- Delivery copy in docs: [`12-ux-ui/mvp-synkro-tech/`](./docs/12-ux-ui/mvp-synkro-tech/)

