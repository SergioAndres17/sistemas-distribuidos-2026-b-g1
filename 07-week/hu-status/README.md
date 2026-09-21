<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       07-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 07

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Sergio Andres Ordoñez Diaz
- GITHUB_USER: SergioAndres17
- TEAM: Group - synkro-tech
- SPRINT_GOAL: Propagate the ADR-003 architectural decisions to service-catalog.md and models.md, align git-conventions.md with branching-policy.md, and deliver the hexagonal architecture guide.
<!-- CONFIG-END -->

## Docs Repository

| Board Name             | URL                                              |
|------------------------|--------------------------------------------------|
| synkro-docs Repository | https://github.com/code-corhuila/synkro-docs.git |

## Team Members

| Full Name                          | GitHub User                               |
|------------------------------------|-------------------------------------------|
| Angel Gustavo Solano Trujillo      | https://github.com/AsolanoT               |
| Fredman Santiago Plazas Artunduaga | https://github.com/SantiagoPlazas2005     |
| Jordan Ramirez Gallego             | https://github.com/JordanRG420            |

## 1. User stories worked this week

| HU ID      | Title                                                              | Status | Evidence (PR or commit URL)                                                                       |
|------------|--------------------------------------------------------------------|--------|---------------------------------------------------------------------------------------------------|
| HU-ARQ-14  | Add ADR-003 — downstream corrections to service-catalog and models | done   | `09-microservices/service-catalog.md`, `06-data/models.md`                                        |
| HU-DOCS-28 | Align git-conventions.md with branching-policy.md                 | done   | `00-governance/git-conventions.md`                                                                |
| HU-ARQ-15  | Add hexagonal architecture guide                                   | done   | `05-architecture/hexagonal-architecture.md`                                                       |

## 2. My individual contribution

**HU-ARQ-14 — Downstream corrections (service-catalog and models):**
- Updated `09-microservices/service-catalog.md` to reflect ADR-003's
  architectural change: added `api-gateway`, `synkro-workflow`, and
  `synkro-worker` to the service registry and gave each a full detail
  section; corrected the Service Communication Matrix (removed rows
  describing `sales-service` calling `customers-service`/`products-service`
  directly at sale creation, which was the pre-ADR-003 design); corrected
  the Data Ownership Matrix and the JWT validation note; and updated the
  frontend/shared repositories table to the professor's actual repository
  names.
- Updated `06-data/models.md`: added the `sales.outbox` table with its
  modeling decisions (why it lives in the `sales` schema, why
  `synkro-workflow` has no database); corrected the `customer_id` and
  `product_id` external-reference notes, which still attributed
  validation to `sales-service` directly instead of `synkro-workflow`
  (Saga steps 1 and 2).

**HU-DOCS-28 — Align git-conventions.md:**
- Corrected `00-governance/git-conventions.md` to match
  `branching-policy.md` on all 7 items flagged by the professor: added
  `dev`/`develop` equivalence note; clarified that no permanent branch
  accepts a direct commit; added the missing `qa/` branch type; fixed
  `hotfix/` to branch from `main`; replaced "Merge policy" (which
  contradicted the cherry-pick promotion rule) with "Promotion by
  re-application"; added the "Release branches" section; and corrected
  the `docs` repository exception (the old text said "no PR is possible",
  which was wrong under the new policy).

**HU-ARQ-15 — Hexagonal architecture guide:**
- Authored `05-architecture/hexagonal-architecture.md`, the missing
  reference document for Pillar 7. Defined the 3 layers (Domain,
  Application, Infrastructure) and the inward-only dependency rule;
  showed ports and adapters as working code in both Java and Go; walked
  a complete example (`RegisterSaleUseCase`) across all 3 layers using
  the post-ADR-003 shape of `sales-service`'s responsibility; showed how
  the port/adapter split enables TDD (a use-case unit test using a fake
  in-memory repository); and documented the folder structure convention
  per language and a table of common mistakes that violate the
  architecture.

## 3. Blockers and risks

- `hexagonal-architecture.md` uses `RegisterSaleUseCase` as its example
  (renamed from `CreateSaleUseCase` per the HU) because after ADR-003,
  "creating" a sale is `synkro-workflow`'s job — `sales-service` only
  registers a pre-validated sale. This naming decision is documented
  explicitly in the file.
- The git-conventions.md correction was a two-commit delivery: the first
  covered the professor's 7-item checklist, the second fixed an
  additional stale description found in `documentation-rules.md` during
  review — kept separate to preserve traceability.

## 4. Plan for next week

- Follow up on professor's feedback on the merged PRs and address any
  requested corrections.
- Begin folder `07-api/` (OpenAPI contracts) once the technology decision
  for `synkro-workflow` and `synkro-worker` is confirmed.
- Coordinate with the team on the first coding sprint kickoff (TDD cycle,
  hexagonal structure per `hexagonal-architecture.md`).

## 5. Compliance self-check

- [x] Conventional Commits - `type(scope): summary`
- [x] Per-environment HU branch + PR to `main` — branches `docs/add-adr-003-gateway-saga-async`, `docs/align-git-conventions`, and `docs/add-hexagonal-architecture-guide`, all merged via PR approved by `ariel5253`
- [x] Testable acceptance criteria
- [x] Tests added/updated — N/A, documentation-only HU
- [x] DDD / hexagonal boundaries respected — N/A, no code touched this week
- [x] No secrets; config via environment variables — N/A

## 6. Evidence links

- Service catalog (HU-ARQ-14): [`service-catalog.md`](https://github.com/code-corhuila/synkro-docs/blob/main/09-microservices/service-catalog.md)
- Data models (HU-ARQ-14): [`models.md`](https://github.com/code-corhuila/synkro-docs/blob/main/06-data/models.md)
- Git conventions (HU-DOCS-28): [`git-conventions.md`](https://github.com/code-corhuila/synkro-docs/blob/main/00-governance/git-conventions.md)
- Hexagonal architecture (HU-ARQ-15): [`hexagonal-architecture.md`](https://github.com/code-corhuila/synkro-docs/blob/main/05-architecture/hexagonal-architecture.md)
