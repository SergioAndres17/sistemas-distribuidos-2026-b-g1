<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       12-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 12

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Sergio Andres Ordoñez Diaz
- GITHUB_USER: SergioAndres17
- TEAM: Group 10 - synkro-tech
- SPRINT_GOAL: Reconcile delivered traceability and identifiers (HU-DOCS-94), domain/data/API contracts (HU-DOCS-95), saga/session/security decisions (HU-ARQ-28), deployment/release/project control (HU-DOCS-96), and the service catalog, portal flows and architecture views (HU-DOCS-97), with individual evidence and explicit remaining implementation and acceptance gaps.
<!-- CONFIG-END -->

## Docs Repository

| Board Name             | URL                                              |
|------------------------|--------------------------------------------------|
| synkro-docs Repository | https://github.com/code-corhuila/synkro-docs.git |

## Team Members

| Full Name                          | GitHub User                               |
|------------------------------------|-------------------------------------------|
| Sergio Andres Ordoñez Diaz         | https://github.com/SergioAndres17         |
| Fredman Santiago Plazas Artunduaga | https://github.com/SantiagoPlazas2005     |
| Jordan Ramirez Gallego             | https://github.com/JordanRG420            |
| Angel Gustavo Solano Trujillo      | https://github.com/AsolanoT               |

## 1. User stories worked this week

| HU ID      | Title                                                                 | Status | Evidence (PR or commit URL) |
|------------|------------------------------------------------------------------------|--------|------------------------------|
| HU-DOCS-95 | Reconcile domain, data and public API contracts | done | [PR #258](https://github.com/code-corhuila/synkro-docs/pull/258); [issue #253](https://github.com/code-corhuila/synkro-docs/issues/253) |

## 2. My individual contribution

**HU-DOCS-95 — domain, data and public API reconciliation:**

- Compared the domain entities/context boundaries with the data model, dictionary, delivered migrations and five public OpenAPI contracts, preserving business ownership and accepted migration history.
- Documented the Products V017 `stock_adjustment.stock_after` receipt and replay behavior. Corrected migration paths, SQL examples, grants and constraint descriptions against the owning repositories rather than claiming missing constraints already exist.
- Reconciled Sales reporting with UTC ranges, frozen integer amounts, token-derived ownership and stable pagination. Documented reversed-date and offset-overflow 400 responses, effective unversioned health URLs and generic persistence/unexpected 500 responses.
- Made the Sale/SaleDetail inactive-restoration discrepancy explicit; kept it as TD-012 instead of silently adding a domain attribute to match storage. Tracked lock overlap, absent Circuit Breaker, retry/jitter differences, Products health and physical-schema discrepancies through TD-014–018.
- Reconciled cross-cutting, pattern and threat references, including unique STRIDE identifiers and the two-engine scope. Centralized the saga decision-dependency guidance and kept observed behavior distinct from architectural adoption.
- Updated the bounded requirement/traceability references, testing strategy and documentation-verification record without changing business scope.

**Evidence:** [issue #253](https://github.com/code-corhuila/synkro-docs/issues/253), [merged PR #258](https://github.com/code-corhuila/synkro-docs/pull/258), [domain rules](./docs/02-domain/entities-and-rules.md), [data model](./docs/06-data/models.md), [contract guidelines](./docs/07-api/guidelines.md) and [verification record](./docs/15-project-control/documentation-verification.md).

**Attribution:** #253 assigns this work to SergioAndres17 and its content commit is authored by SergioAndres17; the integration PR was opened by AsolanoT. The PR author's account is not used to reassign the individual contribution.

**Validation recorded in the delivery:** five YAML contracts were checked for parsing/duplicate keys, local/shared references, response ordering and documented behavior; source/test/migration comparisons and internal-link checks are recorded in PR #258 and the verification entry. These are document/contract checks, not newly executed backend or database acceptance. The PR description leaves its CI-green checkbox unchecked.

## 3. Blockers and risks

- TD-012/014/015/016/017/018 remain implementation discrepancies. Updating the specification does not repair inactive aggregate restoration, isolate runtime locks, add a Circuit Breaker, fix jitter, probe the Products store or change its schema.
- The DOCS-95 snapshot precedes the later HU-ARQ-28 acceptance. Its Proposed/pending statements describe that historical cut; ADR-015/016/017 were subsequently recorded as Accepted project decisions in PR #259.
- Infrastructure/source-access checks and CI outcomes must be read from their own records; the merge does not establish a successful platform run.

## 4. Plan for next week

- Coordinate the reviewed resolution of Sale/SaleDetail restoration and Products physical-schema discrepancies with the responsible owners.
- Support regression cases for replay authorization, ambiguous outcomes, report ranges and health failures before implementation changes.
- Reconcile contract/model references after runtime corrections and preserve applied migrations and Accepted ADRs.

These are follow-up proposals, not completed work or newly assigned HU identifiers.

## 5. Compliance self-check

- [x] Conventional Commits - `type(scope): summary` — content commit: `docs(contracts): reconcile domain, data and public API contracts`.
- [x] Documentation branch + PR to `main` — `docs/reconcile-domain-data-and-api-contracts`; PR #258 is merged.
- [x] Testable acceptance criteria — valid sales, replay errors, ambiguous outcomes and unresolved model drift are explicit in HU-DOCS-95.
- [x] Tests added/updated — N/A for production code; contract validation and existing source/test references were documented.
- [x] DDD / hexagonal boundaries respected — the domain was reviewed before storage/contracts; unsupported storage fields were not silently promoted into domain behavior.
- [x] No secrets; config via environment variables — documentation and OpenAPI snapshots contain no operational credential files.
- [ ] Successful PR CI independently established — the PR description does not mark CI green; no passing result is inferred here.

## 6. Evidence links

The 20 changed documentation/contract files below are exported from the merged PR #258 revision. One additional unchanged file, `_shared.yaml`, is included as contract-reference context. This subset preserves the original delivery; use the complete pinned repository for other cross-document links.

### Merged delivery references

- [PR #258 — docs(contracts): reconcile domain, data and public API contracts](https://github.com/code-corhuila/synkro-docs/pull/258); [merge commit 7b0341c](https://github.com/code-corhuila/synkro-docs/commit/7b0341ccac6c9fec08ce7389bde0ef5771bcc0de).

### Local document evidence


**00-governance**

- [00-governance/agile-conventions.md](./docs/00-governance/agile-conventions.md)

**02-domain**

- [02-domain/domain-map.md](./docs/02-domain/domain-map.md)
- [02-domain/entities-and-rules.md](./docs/02-domain/entities-and-rules.md)

**04-requirements**

- [04-requirements/functional.md](./docs/04-requirements/functional.md)
- [04-requirements/traceability-matrix.md](./docs/04-requirements/traceability-matrix.md)
- [04-requirements/user-stories.md](./docs/04-requirements/user-stories.md)

**05-architecture**

- [05-architecture/cross-cutting.md](./docs/05-architecture/cross-cutting.md)
- [05-architecture/pattern-guide.md](./docs/05-architecture/pattern-guide.md)
- [05-architecture/security-threat-model.md](./docs/05-architecture/security-threat-model.md)

**06-data**

- [06-data/data-dictionary.md](./docs/06-data/data-dictionary.md)
- [06-data/models.md](./docs/06-data/models.md)

**07-api**

- [07-api/contracts/openapi/_shared.yaml](./docs/07-api/contracts/openapi/_shared.yaml)
- [07-api/contracts/openapi/synkro-auth-api.yaml](./docs/07-api/contracts/openapi/synkro-auth-api.yaml)
- [07-api/contracts/openapi/synkro-customers-api.yaml](./docs/07-api/contracts/openapi/synkro-customers-api.yaml)
- [07-api/contracts/openapi/synkro-products-api.yaml](./docs/07-api/contracts/openapi/synkro-products-api.yaml)
- [07-api/contracts/openapi/synkro-sales-api.yaml](./docs/07-api/contracts/openapi/synkro-sales-api.yaml)
- [07-api/contracts/openapi/synkro-workflow.yaml](./docs/07-api/contracts/openapi/synkro-workflow.yaml)
- [07-api/guidelines.md](./docs/07-api/guidelines.md)

**11-quality**

- [11-quality/testing-strategy.md](./docs/11-quality/testing-strategy.md)

**15-project-control**

- [15-project-control/documentation-verification.md](./docs/15-project-control/documentation-verification.md)
- [15-project-control/technical-backlog.md](./docs/15-project-control/technical-backlog.md)
