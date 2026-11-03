<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       13-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 13

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Sergio Andres Ordoñez Diaz
- GITHUB_USER: SergioAndres17
- TEAM: Group 10 - synkro-tech
- SPRINT_GOAL: Threat-model the synkro-api-gateway to synkro-products-api trust boundary with STRIDE, turn its highest risks into prioritized security stories on the team board, and propose security gates with a negative authorization test, keeping designed controls separate from what the code implements today.
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

| HU ID | Title | Status | Evidence (PR or commit URL) |
|-------|-------|--------|------------------------------|
| Workshop W13 | STRIDE of synkro-api-gateway -> synkro-products-api | doing (open for review) | [PR #270](https://github.com/code-corhuila/synkro-docs/pull/270); [claim issue #267](https://github.com/code-corhuila/synkro-docs/issues/267) |
| #271 | [security] synkro-products-api verifies the RS256 signature before changing catalog, prices or stock (E, risk 9, P0) | todo | [issue #271](https://github.com/code-corhuila/synkro-docs/issues/271) |
| #272 | [security] Stock adjustments are recorded only under the verified user (S, risk 6, P1) | todo | [issue #272](https://github.com/code-corhuila/synkro-docs/issues/272) |
| #273 | [security] Keep the author of every product price change (R, risk 4, P2) | todo | [issue #273](https://github.com/code-corhuila/synkro-docs/issues/273) |
| Workshop W13 | Security gates and negative authorization test (proposal) | done | [CI commit 70ddb4f](https://github.com/SergioAndres17/sistemas-distribuidos-2026-b-g1/commit/70ddb4f); [test commit 006f559](https://github.com/SergioAndres17/sistemas-distribuidos-2026-b-g1/commit/006f559); [run recorded 37503ff](https://github.com/SergioAndres17/sistemas-distribuidos-2026-b-g1/commit/37503ff) |

The PR is open: it was not merged by its author (workshop rule 8). The three issues are backlog items in the "Synkro Tech — Backlog" project; `todo` means they are not implemented.

## 2. My individual contribution

**Attack surface (CP1):**

- Drew a Mermaid data flow of every Synkro component taken from `deployment.md`, ADR-006, ADR-007 and ADR-008: users, synkro-front, synkro-api-gateway, auth, customers, products and sales APIs, synkro-workflow, synkro-worker, synkro-db, synkro-mongo and the observability stack. No broker appears because RabbitMQ is deferred (ADR-007 Decision 5).
- Marked six trust boundaries (TB1–TB6) and highlighted mine, TB2b. Listed six assets with their classification and every entry point of the boundary with how it authenticates today.

**STRIDE analysis (CP2, CP6):**

- Found in the current code of synkro-products-api that `requireAuth` only checks the JWT shape and `claimsFrom` decodes the payload **without verifying the RS256 signature** (`internal/adapter/in/httpapi/middleware.go`), while `canWriteCatalog` trusts those roles. The team threat model lists RS256 verification as *Designed*; the code does not implement it yet.
- Wrote seven STRIDE rows with probability, impact, existing control and missing mitigation. Ranked them: #6 Elevation of privilege (forged ADMIN role changes prices and stock) is P0 with risk 9.
- Documented the accepted residual risk (#4 catalog read, #5 flood in `develop`) and its review point before `qa`.
- Cited the physical library book in two rows [B] and in issue #271.

**Peer review (CP5, CP6):**

- No teammate opened a STRIDE pull request, so I self-reviewed my table as the guide allows ([review comment](https://github.com/code-corhuila/synkro-docs/pull/270/changes#r4408792964)).
- The review found a missed threat: `withCorrelation` reuses `X-Correlation-Id` without checking format or length and returns it as the `traceId` of every error body. It became row #7 (risk 4, P2) and a "Review response" section in the STRIDE file.

**Security backlog (CP3, CP7):**

- Turned the three highest risks into issues #271, #272 and #273, each with a user story, verifiable acceptance criteria (one is an automated test), STRIDE letter, risk value and link to PR #270.
- Added the `security` and `P0`/`P1`/`P2` labels and the team project, and rewrote a vague criterion of #272 into a concrete request and expected answer.

**Security gates and negative test (CP4, CP8):**

- Wrote `security.yml` for GitHub Actions on `pull_request`: gitleaks (secrets), govulncheck (dependencies) and the negative authorization test, which needs the other two jobs so a red step blocks the merge.
- Wrote a minimal Go module with the proposed fix (RS256 verification with `crypto/rsa`, only `alg: RS256`, expiry check, then `canWriteCatalog`) and a test that sends five `PUT /api/v1/products/{id}` requests: no token 401, edited payload with `alg: none` 401, ADMIN token signed with another key 401, SALESPERSON 403, INVENTORY 200.
- Ran it on my machine (Go 1.27.3, `PASS`) and validated the workflow with actionlint 1.7.7 (exit code 0). The real console output is in `HOW-TO-RUN.md`.

**Library book:** Pressman, Roger S., *Ingeniería del software: un enfoque práctico*, 7.ª ed., McGraw-Hill, 2010. Call number 005.3 P727a, copy 1. Consulted in person on 2026-10-28 at 19:00. Used: section 17.7.2 "Pruebas de seguridad" (p. 402) for threat #6 and issue #271, and section 17.7.3 "Pruebas de esfuerzo" (pp. 402-403) for threat #5.

## 3. Blockers and risks

- The RS256 signature check is still missing in synkro-products-api. Until #271 is implemented, any JWT-shaped token can change prices and stock; documenting the threat does not close it.
- The negative test runs on a minimal module that contains the proposed fix, not on synkro-products-api. Pointed at the current service, it would fail.
- `security.yml` is a proposal kept in my fork; it is not in the team pipeline.
- Probability and impact values are my estimates and must be reviewed by the team and the Tech Lead.
- The first commit of PR #270 contained a draft written for another boundary; the second commit replaced it. It is explained in the PR description and the history was not rewritten.
- Pushes were not fully on the expected rhythm: CP2 to CP5 were pushed after the class window, all final-delivery pushes were on Friday, and CP7 (17:43) and CP8 (17:51) were less than 15 minutes apart.

## 4. Plan for next week

- Propose #271 (P0) for the next sprint of synkro-products-api and move the negative test into `internal/adapter/in/httpapi` so it runs on every pull request.
- Follow with #272, which is mostly closed by #271, and then #273 (price-change history, documented in `06-data/models.md`).
- Ask the Tech Lead to review and merge PR #270, and to update the status of T-1, T-4 and E-2 in `security-threat-model.md` from what the code really does.

These are proposals, not completed work.

## 5. Compliance self-check

- [x] Conventional Commits - `type(scope): summary` — e.g. `docs(security): complete STRIDE and risk ranking for gateway-to-products-api`, `ci(w13): add security gates and negative authorization test`.
- [x] Documentation branch + PR to `main` — branch `security/stride-SergioAndres17`, PR #270, not merged by me.
- [x] Testable acceptance criteria — every issue has criteria with a request and an expected status; one per issue is an automated test.
- [x] Tests added/updated — negative authorization test in Go, run locally with `PASS`.
- [ ] DDD / hexagonal boundaries respected — N/A: no production code was changed; the proposal places the check in the HTTP input adapter, before the use case.
- [x] No secrets; config via environment variables — the test creates its RSA keys at run time; no key or token is committed.
- [ ] Every push on the expected rhythm — see section 3.

## 6. Evidence links

### Fork (`13-week/workshop`)

- [Workshop README (CONFIG)](../workshop/README.md)
- [01 — Attack surface and trust boundaries](../workshop/01-attack-surface.md)
- [04 — Security CI: HOW-TO-RUN with real output](../workshop/04-security-ci/HOW-TO-RUN.md)
- [04 — security.yml](../workshop/04-security-ci/security.yml)
- [04 — Proposed fix (authz.go)](../workshop/04-security-ci/products-authz/authz.go)
- [04 — Negative test (authz_test.go)](../workshop/04-security-ci/products-authz/authz_test.go)

### synkro-docs

- [Claim issue #267](https://github.com/code-corhuila/synkro-docs/issues/267)
- [PR #270 — Threat model: synkro-api-gateway -> synkro-products-api](https://github.com/code-corhuila/synkro-docs/pull/270)
- [Self-review comment](https://github.com/code-corhuila/synkro-docs/pull/270/changes#r4408792964)
- [Security issues #271](https://github.com/code-corhuila/synkro-docs/issues/271), [#272](https://github.com/code-corhuila/synkro-docs/issues/272), [#273](https://github.com/code-corhuila/synkro-docs/issues/273)

### Commits

| Checkpoint | Commit |
|------------|--------|
| CP0 | [e0b28fe](https://github.com/SergioAndres17/sistemas-distribuidos-2026-b-g1/commit/e0b28fe) |
| CP1 | [9397231](https://github.com/SergioAndres17/sistemas-distribuidos-2026-b-g1/commit/9397231) |
| CP2 | [a172df9](https://github.com/code-corhuila/synkro-docs/commit/a172df9), [9797d9f](https://github.com/code-corhuila/synkro-docs/commit/9797d9f) |
| CP3 | [7b267fc](https://github.com/SergioAndres17/sistemas-distribuidos-2026-b-g1/commit/7b267fc) |
| CP4 | [70ddb4f](https://github.com/SergioAndres17/sistemas-distribuidos-2026-b-g1/commit/70ddb4f), [006f559](https://github.com/SergioAndres17/sistemas-distribuidos-2026-b-g1/commit/006f559) |
| CP5 | [b0b407c](https://github.com/SergioAndres17/sistemas-distribuidos-2026-b-g1/commit/b0b407c) |
| CP6 | [23a7082](https://github.com/code-corhuila/synkro-docs/commit/23a7082) |
| CP7 | [fa24ae2](https://github.com/SergioAndres17/sistemas-distribuidos-2026-b-g1/commit/fa24ae2) |
| CP8 | [37503ff](https://github.com/SergioAndres17/sistemas-distribuidos-2026-b-g1/commit/37503ff) |
| CP9 | [a51843b](https://github.com/SergioAndres17/sistemas-distribuidos-2026-b-g1/commit/a51843b) |

### Personal reflection

- [W13 Reflection - Sergio Andrés Ordóñez Díaz](https://docs.google.com/document/d/1PfncCPQVoNdzMJ6WIADe8nQ2siG-SQ_-dEik3usE05k/edit?usp=sharing)
