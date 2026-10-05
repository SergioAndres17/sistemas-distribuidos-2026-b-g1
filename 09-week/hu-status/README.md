<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       09-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 09

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Sergio Andres Ordoñez Diaz
- GITHUB_USER: SergioAndres17
- TEAM: Group 10 - synkro-tech
- SPRINT_GOAL: Close HU-13 (contract completion and documentation readiness for the code phase), then deliver HU-14 in full — two database engines (ADR-010), the Angular Customers portal inside the React host (ADR-011), instance bootstrap and credentials (ADR-012), and the identity-as-cross-cutting-service proposal (ADR-013) — with every downstream document realigned, and open the code phase with HU-INF-01.
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
| HU-DOCS-73 | Rewrite the Customers API contract (HU-13)                            | done   | `synkro-customers-api.yaml` |
| HU-DOCS-75 | Align `domain-map.md` with the saga and the workflow (HU-13)          | done   | #135 |
| HU-DOCS-76 | Sweep stale references in context and governance documents (HU-13)    | done   | #142 |
| HU-ARQ-26  | ADR-013: the identity service as the cross-cutting security service (HU-14) | done | https://github.com/code-corhuila/synkro-docs/pull/163 |
| HU-DOCS-82 | Align overview, context, service catalog and C4 (HU-14)               | done   | https://github.com/code-corhuila/synkro-docs/pull/168 |
| HU-DOCS-83 | Align security documents and non-functional requirements (HU-14)      | done   | https://github.com/code-corhuila/synkro-docs/pull/169 |

## 2. My individual contribution

**HU-13 — contract and domain-map corrections (HU-DOCS-73, 75, 76):**
- HU-DOCS-73: rewrote `synkro-customers-api.yaml` end to end to match the final contract conventions fixed in `guidelines.md`.
- HU-DOCS-75: aligned `02-domain/domain-map.md` with the saga and the workflow — the register-sale flow's bounded-context boundaries weren't consistent with ADR-007's final shape.
- HU-DOCS-76: swept stale references across the context and governance documents left over from before ADR-009's database-topology correction.

**HU-14 — ADR-013 (HU-ARQ-26):**
- Proposed Option A: `synkro-auth-api` already is the system's cross-cutting security service — every other service depends on it, it depends on none — with no new repository. Resolved the repository-naming mismatch this implies (`synkro-auth-*`, not the course's `-security` pattern) explicitly in the Decision as an accepted, documented deviation, not left as an open question. Opened the issue asking the instructor to confirm before the ADR moves from `Proposed` to `Accepted`.
- Addressed three automated-review findings before merge: the PR title wasn't Conventional Commits (it had used the instructor-issue title instead), stray edits to ADR-001's and ADR-005's register rows had bled in from unmerged sibling branches and were removed, and the naming-mismatch resolution above was added in response to the bot flagging it as unresolved in an earlier draft.

**HU-14 — overview, catalog and C4 (HU-DOCS-82):**
- Split the C4 Level 2 diagram (both the Mermaid originals in `05-architecture/overview.md` and `01-context/overview.md`, and the `c4-02-containers.drawio` source) into two database subgraphs — PostgreSQL and MongoDB — and labeled the Customers portal box as Angular. Updated `service-catalog.md`, `dependency-map.md`, and both `_stacks/` guides for the infrastructure repository split and Sales' different persistence adapter.

**HU-14 — security and NFRs (HU-DOCS-83):**
- Rewrote the Least Privilege principle and Secret Management sections of `security-policy.md` for ADR-012's fixed-name service users, added a "Document Query Injection Prevention" rule to `security-rules.md` for Sales' MongoDB queries, extended `security-threat-model.md`'s Scope and two existing threats (T-3, D-3) for the two-engine split, and added a new threat, **T-7**, for document-query (NoSQL) injection — with a real mitigation already in the system's design, not a placeholder.

## 3. Blockers and risks

- **ADR-013 is `Proposed`, pending the instructor's answer.** Tracked as `open-questions.md` Q-005; does not block anything else in HU-14, since the story's own deliverable is the record and the question, not the acceptance.
- **The C4 `.drawio` source could not be safely regenerated from search fragments alone** — reconstructing a multi-hundred-line XML file from partial content risks corrupting it silently. Resolved by inventorying the real file's cell IDs and geometry first, then applying targeted edits instead of a full rewrite.
- **Review iteration on HU-ARQ-26** cost roughly a day: the bot's three findings all traced back to the same root cause (the branch had stacked on top of unmerged sibling branches instead of `main`), which only became visible after rebasing.

## 4. Plan for next week

- HU-GTW-01 (gateway routes and its own behavior) once HU-INF-02 lands.
- Revisit ADR-013's status as soon as the instructor answers; if Option A is confirmed, no further document changes are needed beyond flipping the ADR's status to `Accepted`.
- Help verify the C4 `.drawio` renders correctly once someone opens it in draw.io — it was edited programmatically and hasn't been visually confirmed yet.

## 5. Compliance self-check

- [x] Conventional Commits - `type(scope): summary`
- [x] Per-environment HU branch + PR to `main` — branches `docs/rewrite-customers-api-contract`, `docs/align-domain-map-with-saga`, `docs/sweep-stale-references`, `docs/add-adr-013-identity-as-cross-cutting-security`, `docs/align-overview-catalog-and-c4-with-two-engines`, `docs/align-security-and-nfr-with-two-engines`, merged via PR approved by `ariel5253`
- [x] Testable acceptance criteria
- [x] Tests added/updated — N/A, documentation-only HU
- [x] DDD / hexagonal boundaries respected — N/A, no code touched this week
- [x] No secrets; config via environment variables — ADR-013 changes no credential handling; it only proposes which repository owns an existing one

## 6. Evidence links

- ADR-013 (HU-ARQ-26): [`ADR-013-identity-as-cross-cutting-security.md`](./docs/ADR-013-identity-as-cross-cutting-security.md)
- Overview, catalog and C4 (HU-DOCS-82): [`overview.md`](./docs/05-architecture/overview.md), [`c4-02-containers.drawio`](./docs/08-uml/diagrams/source/c4-02-containers.drawio)
- Security documents and NFRs (HU-DOCS-83): [`security-threat-model.md`](./docs/05-architecture/security-threat-model.md), [`non-functional.md`](./docs/04-requirements/non-functional.md)
- Issue tracking ADR-013's pending answer: linked from `open-questions.md` Q-005
