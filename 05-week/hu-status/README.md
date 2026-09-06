<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       04-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 05

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Sergio Andres Ordoñez Diaz
- GITHUB_USER: SergioAndres17
- TEAM: Group - synkro-tech
- SPRINT_GOAL: Close the professor's S00/S06/S12 rubric feedback, formally answer the professor's HU-01 (Technology Stack Selection) and HU-02 (Project Discovery) by auditing existing documentation before writing anything new, and open the Corte 1 MVP build in the dedicated `synkro-tech` repository.
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
| HU-ARQ-07 | Backend: configuration and refinement of the monolithic MVP | todo | repo `synkro-tech` (backend) |

## 2. My individual contribution

**Context:** the professor's rubric marked `06-data` as 🔴 — `models.md`
was the framework scaffold, unmodified. Filling it in surfaced a real
naming inconsistency in the project that had gone unnoticed for weeks.

**HU-DOCS-15 — Data model, data dictionary, modeling conventions:**
- I filled `06-data/models.md` for all 4 schemas (`auth`, `customers`,
  `products`, `sales`) directly from ADR-001's "Data Model per Schema"
  section, adding the technical detail ADR-001 deliberately left open:
  column types, constraints, indexes, and an ER diagram per schema.
- I explicitly marked which relationships are real foreign keys within a
  schema (e.g. `sale_details.sale_id → sales.sale_id`) versus external
  references validated over HTTP across services (`sales.customer_id`,
  `sale_details.product_id`), per ADR-001's schema-isolation model.
- While writing the `role` column's `CHECK` constraint, I found a real
  inconsistency: ADR-001 fixes the role value as `SALES`, but
  `security-policy.md`, `non-functional.md`, and **both** real codebases
  (frontend `roles.js`, backend `Role.java` enum) already use
  `SALESPERSON`. Since `SALESPERSON` wins 4-to-2 and is what's actually
  implemented, I used `SALESPERSON` in the schema rather than `SALES` —
  flagged for the team, not resolved unilaterally with a silent guess.
- I also filled `data-dictionary.md` and `modeling-conventions.md`, two
  files `06-data/README.md` already listed as pending ⭐ that weren't part
  of the original rubric comment — I folded them into this same HU instead
  of opening a new one, since they're the same section.
- `modeling-conventions.md` documents, among other things, why there's no
  `created_by`/`updated_by` on any table: that authorship trail already
  lives in `security-policy.md`'s security logging, so duplicating it per
  table would just add cross-schema coupling for no real benefit.

**HU-DOCS-20 — Backend alternatives (Spring Boot vs. Quarkus, Go vs. Node.js):**
- For Java, I compared Spring Boot (already used for Auth/Customers)
  against Quarkus, concluding Spring Boot fits better because Auth/Customers
  run continuously — Quarkus's faster cold-start advantage doesn't apply to
  services that never restart on demand.
- For Go, I compared it against Node.js for Products/Sales, anchored on a
  real business rule (RF-07: stock must update automatically and safely
  under concurrent sales) rather than team preference, concluding Go's
  concurrency model fits that need better, and also satisfies the course's
  two-language backend requirement in a way Node.js (same language as the
  frontend) would not.

## 3. Blockers and risks

- The `SALES` vs. `SALESPERSON` inconsistency is now resolved in
  `06-data/models.md`, but ADR-001 itself still says `SALES` and is
  immutable — the team should decide whether to raise a short `ADR-002`
  that formally documents the correction, so the mismatch doesn't resurface
  elsewhere.
- HU-ARQ-07 hasn't started — it depends on the team confirming the final
  scope from HU-DOCS-17 before I configure the backend against a moving
  target.

## 4. Plan for next week

- Start HU-ARQ-07: project structure, database connection, environment
  variables, and README for `synkro-tech`'s backend.
- Coordinate with Angel (HU-FE-02) on the API shape the frontend expects,
  now that HU-DOCS-17's scope is confirmed.

## 5. Compliance self-check
- [ ] Conventional Commits - `type(scope): summary`
- [x] Per-environment HU branch + PR to that environment — not applicable to `docs` repo (no branches, direct commit to `main` per `documentation-rules.md`)
- [x] Testable acceptance criteria
- [ ] Tests added/updated (unit / integration) — not applicable, documentation-only HUs
- [ ] DDD / hexagonal boundaries respected (domain has no I/O) — not applicable, documentation-only HUs
- [x] No secrets; config via environment variables

## 6. Evidence links
- Data model: [`models.md`](./docs/06-data/models.md)
- Data dictionary: [`data-dictionary.md`](./docs/06-data/data-dictionary.md)
- Modeling conventions: [`modeling-conventions.md`](./docs/06-data/modeling-conventions.md)
- Backend alternatives: [`overview.md`](./docs/01-context/overview.md), "Alternatives Considered" → Backend subsections
