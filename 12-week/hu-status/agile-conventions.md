# Agile Team Conventions

> Defines how the team works through its development cycles. Agreed on with the
> entire team. Update when the team decides to change something.

---

## Sprint structure

| Field | Value |
|-------|-------|
| Duration | 1 week |
| Sprint start | Monday |
| Sprint end | Sunday |
| Current sprint | Sprint 12 — Week 12 of the project (16 weeks total) |
| Estimated capacity | Point estimation starts in Sprint 10 |

> **On "Estimated capacity":** point estimation started with the first real
> product HUs in Sprint 10. The current sprint is 12; the starting point
> for estimation has not changed. Documentation HUs are tracked without
> inventing story-point estimates.

> **Note:** The team uses **HU (User Story) as the only work item type**, without distinguishing Task/Spike/HU — this includes both product features and documentation/research tasks (PDR, ADR, context map, governance, etc.). Point estimation (see scale below) starts once real product feature HUs (code) exist; it does not apply to documentation HUs.

---

## Ceremonies

### Sprint Planning
- **When:** first day of the sprint (Monday)
- **Duration:** maximum 1 hour
- **Who:** entire team
- **Goal:** select and commit to the sprint's tasks/HUs, break them down into technical tasks
- **Output artifact:** GitHub Project updated with the sprint's items

### Daily Stand-up
- **When:** asynchronous, via each member's weekly README (`NN-week/hu-status/README.md`)
- **Format:**
  1. What did I do?
  2. What will I do?
  3. Is anything blocking me?
- **Rule:** technical discussions are resolved outside this report, not inside it

### Sprint Review
- **When:** last day of the sprint (Sunday)
- **Duration:** maximum 30 minutes
- **Who:** team only (no Product Owner)
- **Goal:** internally show what was built/documented that week before presenting it to the Product Owner

### Weekly (with Product Owner)
- **When:** Wednesday of each week, reviewing the previous week's work (e.g. Wednesday of week 4 reviews what was done in week 3)
- **Duration:** maximum 1 hour
- **Who:** team + Product Owner (the course instructor)
- **Goal:** present what was built/documented the previous week, collect feedback from the Product Owner, and use the same session to refine and detail the following week's tasks/HUs (merges the function of a PO-facing Sprint Review and Backlog Refinement into a single ceremony, since both depend on the instructor's weekly availability)
- **Exit criterion:** the following week's task/HU meets the Definition of Ready (DoR)

### Sprint Retrospective
- **When:** last day of the sprint (Sunday), after the internal Sprint Review
- **Duration:** maximum 30 minutes
- **Who:** team only (no Product Owner)
- **Format:** What went well / What to improve / Action commitments
- **Rule:** each retro produces at least 1 improvement action with an owner

---

## Estimation

### Scale
| Points | Meaning |
|--------|---------|
| 1 | Trivial — done in hours |
| 2 | Small — done in one day |
| 3 | Medium — takes 2–3 days |
| 5 | Large — takes almost a full sprint |
| 8 | Very large — should be split |
| 13 | Epic — MUST be split before the sprint |

**Technique:** Informal Planning Poker (team discussion, no dedicated tool)
**Applies from:** the first real product HUs (implementation Sprint 1)

### Estimation rule
- If there is disagreement of 2+ levels, discuss before voting again.
- If a story is estimated at 8 or 13, it must be split into smaller sub-tasks.

---

## Story identifiers

Every story is `HU-<PREFIX>-NN`. Each prefix has one sequence shared by the whole team; it never restarts by person, sprint or cut.

| Prefix | Scope | Next number |
|--------|-------|-------------|
| `DOCS` | Documents of `synkro-docs` | 98 |
| `ARQ` | Architecture decisions (ADR) and their spikes | 29 |
| `PDR`, `ADR`, `DOM` | Discovery stories of Cut 1; closed, no new numbers | — |
| `AUTH` | Auth domain: `synkro-auth-db`, `synkro-auth-api`, `synkro-auth-portal` | 15 |
| `CLI` | Customers domain: its database repository, service and portal | 05 |
| `PRO` | Products domain: its database repository, service and portal | 14 |
| `VEN` | Sales domain: its database repository, service and portal | 18 |
| `FE` | `synkro-front`, the frontend host | 09 |
| `INF` | `synkro-infra-postgres`, `synkro-infra-mongo` and work that touches every repository | 05 |
| `GTW` | `synkro-api-gateway` | 03 |
| `WKF` | `synkro-workflow` | 04 |
| `WRK` | `synkro-worker` | 02 |

### Identifier reconciliation after HU-DOCS-92 creation

This is the single repository register for the reconciled identifiers below. The verification record references this register rather than maintaining a second ledger.

| Assigned ID | Issue | Work |
|---|---|---|
| HU-INF-01 | [#164](https://github.com/code-corhuila/synkro-docs/issues/164) | Common files in every code repository |
| HU-PRO-03 | [#181](https://github.com/code-corhuila/synkro-docs/issues/181) | Base structure of the Products service |
| HU-GTW-01 | [#182](https://github.com/code-corhuila/synkro-docs/issues/182) | Gateway routes and its own behavior |
| HU-FE-03 | [#183](https://github.com/code-corhuila/synkro-docs/issues/183) | Host: single HTTP client, session and development sign-in |
| HU-AUTH-01 | [#184](https://github.com/code-corhuila/synkro-docs/issues/184) | Base structure of the Auth service |
| HU-WKF-01 | [#185](https://github.com/code-corhuila/synkro-docs/issues/185) | Base structure of the workflow |
| HU-VEN-03 | [#186](https://github.com/code-corhuila/synkro-docs/issues/186) | Base structure of the Sales service |
| HU-CLI-01 | [#187](https://github.com/code-corhuila/synkro-docs/issues/187) | Base structure of the Customers service |
| HU-FE-04 | [#188](https://github.com/code-corhuila/synkro-docs/issues/188) | Host: portal registry, isolation, layout and not found |
| HU-WRK-01 | [#189](https://github.com/code-corhuila/synkro-docs/issues/189) | Base structure of the worker |
| HU-WKF-02 | [#190](https://github.com/code-corhuila/synkro-docs/issues/190) | Fix architecture-check guard and UTF-8 test coverage |
| HU-PRO-04 | [#191](https://github.com/code-corhuila/synkro-docs/issues/191) | Base structure of the Products database repository |
| HU-PRO-05 | [#192](https://github.com/code-corhuila/synkro-docs/issues/192) | Category and Product tables |
| HU-PRO-06 | [#193](https://github.com/code-corhuila/synkro-docs/issues/193) | Stock adjustment and reservation tables, plus the idempotency key table |
| HU-PRO-07 | [#194](https://github.com/code-corhuila/synkro-docs/issues/194) | Stock alert table |
| HU-PRO-08 | [#195](https://github.com/code-corhuila/synkro-docs/issues/195) | Categories and Products endpoints |
| HU-AUTH-02 | [#196](https://github.com/code-corhuila/synkro-docs/issues/196) | Base structure of the Auth database repository |
| HU-AUTH-03 | [#197](https://github.com/code-corhuila/synkro-docs/issues/197) | Auth issues real RS256-signed tokens |
| HU-PRO-09 | [#198](https://github.com/code-corhuila/synkro-docs/issues/198) | Stock adjustment and reservation endpoints |
| HU-PRO-10 | [#199](https://github.com/code-corhuila/synkro-docs/issues/199) | Stock-alert endpoints |
| HU-PRO-11 | [#200](https://github.com/code-corhuila/synkro-docs/issues/200) | Base structure of synkro-products-portal |
| HU-FE-05 | [#201](https://github.com/code-corhuila/synkro-docs/issues/201) | Host client: query parameters, idempotency key and caller cancellation |
| HU-PRO-01 | [#202](https://github.com/code-corhuila/synkro-docs/issues/202) | Products portal: list products |
| HU-FE-06 | [#203](https://github.com/code-corhuila/synkro-docs/issues/203) | Host: design tokens and a ready `synkro-front` |
| HU-PRO-13 | [#204](https://github.com/code-corhuila/synkro-docs/issues/204) | Products portal: apply the design system to the list |
| HU-PRO-02 | [#205](https://github.com/code-corhuila/synkro-docs/issues/205) | Products portal: register a product |
| HU-PRO-12 | [#206](https://github.com/code-corhuila/synkro-docs/issues/206) | Products portal: complete the portal (catalogue management, categories, stock lookup and stock alerts) |
| HU-DOCS-89 | [#207](https://github.com/code-corhuila/synkro-docs/issues/207) | Add the Products Portal Stories to the Traceability Matrix |
| HU-AUTH-04 | [#208](https://github.com/code-corhuila/synkro-docs/issues/208) | `system_user` Table and `auth_reader` Role |
| HU-AUTH-05 | [#209](https://github.com/code-corhuila/synkro-docs/issues/209) | `refresh_token` table, foreign key to `system_user` and its index |
| HU-AUTH-06 | [#210](https://github.com/code-corhuila/synkro-docs/issues/210) | `service_token` and `idempotency_key` tables, with the service-token foreign key and index |
| HU-AUTH-07 | [#211](https://github.com/code-corhuila/synkro-docs/issues/211) | Real token validation and role checks in `synkro-auth-api` |
| HU-AUTH-08 | [#212](https://github.com/code-corhuila/synkro-docs/issues/212) | PostgreSQL persistence for users and refresh tokens in `synkro-auth-api` |
| HU-AUTH-09 | [#214](https://github.com/code-corhuila/synkro-docs/issues/214) | User registration, user lookup and the common error envelope in `synkro-auth-api` |
| HU-DOCS-90 | [#215](https://github.com/code-corhuila/synkro-docs/issues/215) | Clarify Modal and Button Tokens, Fix Sale Amount Validation Wording |
| HU-AUTH-10 | [#216](https://github.com/code-corhuila/synkro-docs/issues/216) | Logout and refresh-token reuse detection in `synkro-auth-api` |
| HU-AUTH-11 | [#217](https://github.com/code-corhuila/synkro-docs/issues/217) | Service tokens in `synkro-auth-api` |
| HU-ARQ-27 | [#219](https://github.com/code-corhuila/synkro-docs/issues/219) | ADR-014: Admin-Only User Registration and Initial Administrator Creation |
| HU-DOCS-91 | [#221](https://github.com/code-corhuila/synkro-docs/issues/221) | Reconcile ADR-014 Documentation Tracking and User Story Catalog |
| HU-DOCS-92 | [#223](https://github.com/code-corhuila/synkro-docs/issues/223) | Align Login Ownership and Frontend Session Lifecycle Documentation |
| HU-VEN-04 | [#224](https://github.com/code-corhuila/synkro-docs/issues/224) | Sales migration skeleton — Liquibase runner over MongoDB |
| HU-VEN-05 | [#226](https://github.com/code-corhuila/synkro-docs/issues/226) | Sale collection, validator and permissions |
| HU-DOCS-93 | [#227](https://github.com/code-corhuila/synkro-docs/issues/227) | Reconcile Auth Documentation and Session Decision Traceability |
| HU-VEN-06 | [#228](https://github.com/code-corhuila/synkro-docs/issues/228) | Sale/SaleDetail domain aggregate |
| HU-VEN-16 | [#230](https://github.com/code-corhuila/synkro-docs/issues/230) | Harden Sales MongoDB Migrations and CI Failure Detection |
| HU-GTW-02 | [#231](https://github.com/code-corhuila/synkro-docs/issues/231) | Public login/refresh, protected register |
| HU-AUTH-12 | [#232](https://github.com/code-corhuila/synkro-docs/issues/232) | Transactional User-Creation Audit Schema |
| HU-VEN-07 | [#233](https://github.com/code-corhuila/synkro-docs/issues/233) | Idempotent Sale Registration Use Case and Ports |
| HU-AUTH-13 | [#234](https://github.com/code-corhuila/synkro-docs/issues/234) | Admin-Only Registration with Transactional Audit |
| HU-VEN-08 | [#235](https://github.com/code-corhuila/synkro-docs/issues/235) | MongoDB Sale Persistence Adapter, BSON Mapping and Replica Set Integration |
| HU-INF-04 | [#236](https://github.com/code-corhuila/synkro-docs/issues/236) | Persistent MongoDB Instance and Integration with Sales Migrations |
| HU-VEN-17 | [#237](https://github.com/code-corhuila/synkro-docs/issues/237) | Protect Credentials in the MongoDB Migration Runner |
| HU-AUTH-14 | [#238](https://github.com/code-corhuila/synkro-docs/issues/238) | Audited Initial Administrator CLI |
| HU-INF-02 | [#239](https://github.com/code-corhuila/synkro-docs/issues/239) | Persistent PostgreSQL Infrastructure and SynkroTech Root Composition |
| HU-VEN-09 | [#240](https://github.com/code-corhuila/synkro-docs/issues/240) | Sales HTTP Endpoints and Role-Based Authorization |
| HU-VEN-11 | [#241](https://github.com/code-corhuila/synkro-docs/issues/241) | Sales Portal Remote Integration and Real Sales History |
| HU-FE-07 | [#242](https://github.com/code-corhuila/synkro-docs/issues/242) | Restore Remote Isolation When Switching Portals |
| HU-WKF-03 | [#243](https://github.com/code-corhuila/synkro-docs/issues/243) | Persistent and Recoverable Sales Registration Orchestration |
| HU-CLI-02 | [#244](https://github.com/code-corhuila/synkro-docs/issues/244) | Customers schema and migration runner |
| HU-VEN-10 | [#245](https://github.com/code-corhuila/synkro-docs/issues/245) | Daily, Monthly and Top-Products Sales Aggregation Reports |
| HU-VEN-12 | [#246](https://github.com/code-corhuila/synkro-docs/issues/246) | Real Sale Registration Through Workflow and Composed Sale Detail |
| HU-CLI-03 | [#247](https://github.com/code-corhuila/synkro-docs/issues/247) | Functional Customers API with Persistence, Security, and Idempotent Registration |
| HU-FE-08 | [#248](https://github.com/code-corhuila/synkro-docs/issues/248) | Auth Portal and Production Authentication Through the Host |
| HU-CLI-04 | [#249](https://github.com/code-corhuila/synkro-docs/issues/249) | Complete Customers Portal and Host Integration |
| HU-DOCS-94 | [#252](https://github.com/code-corhuila/synkro-docs/issues/252) | Reconcile delivery traceability and the identifier register |
| HU-DOCS-95 | [#253](https://github.com/code-corhuila/synkro-docs/issues/253) | Reconcile domain, data and public API contracts |
| HU-ARQ-28 | [#254](https://github.com/code-corhuila/synkro-docs/issues/254) | Formalize saga safety, session lifecycle and integration decision boundaries |
| HU-DOCS-96 | [#255](https://github.com/code-corhuila/synkro-docs/issues/255) | Document real deployment, release evidence and project control |
| HU-DOCS-97 | [#256](https://github.com/code-corhuila/synkro-docs/issues/256) | Update the service catalog, portal flows and architecture views |

**Assignment and next-number rules:** a proposal is a candidate story without a created issue assigning its internal ID. An ID is assigned (consumed) when such an issue exists, regardless of whether its work is open, in progress, closed or cancelled; assigned IDs are never reused. `Next number` is the next unassigned number in that prefix sequence, not the next completed story.

The five Week 12 stories HU-DOCS-94/#252, HU-DOCS-95/#253, HU-ARQ-28/#254, HU-DOCS-96/#255 and HU-DOCS-97/#256 have been created and their IDs are consumed. The next DOCS number is 98 and the next ARQ number is 29. ADR numbering is separate: the next new decision record is ADR-015. Recheck the issue inventory before assigning another ID.

**Lifecycle authority:** the linked GitHub issue is authoritative for its current open/closed state. A PR's `Closes #223` is an instruction to close the issue when that PR merges into main; it does not declare completion before merge. This register tracks assignment, not a duplicate lifecycle status. The catalog and board must follow actual review and merge evidence.

**Why the prefixes mix languages:** `AUTH`, `CLI`, `PRO` and `VEN` are retained for continuity with assigned issues and existing traces. `CLI`, `PRO` and `VEN` abbreviate the Spanish domain names. New prefixes use English names, consistent with `documentation-rules.md`. Existing assigned IDs are not renamed.

A story belongs to the prefix of the repository it changes. A code pull request references its story as `code-corhuila/synkro-docs#<issue>`; a story that needs changes in two domains is split into one story per domain.


### Skips, aliases and substitutions

| Reference | Reconciliation | Treatment |
|---|---|---|
| HU-VEN-16 / #230 | Migration hardening and CI error detection | Keep this ID and its history unchanged. |
| Old HU-VEN-16 references linked to #237 | #237 is now HU-VEN-17, credential-safe migration runner | Resolve by issue link. Do not globally replace HU-VEN-16 because #230 remains valid. |
| HU-SALES-12 linked to #246 | Historical alias of HU-VEN-12 | Use HU-VEN-12 for current references to that scope. |
| Planned HU-VEN-01 | Initial simulated registration plan | No assigned issue found in this repository; delivered registration scope is HU-VEN-12/#246. Preserve the plan, do not claim delivery under VEN-01. |
| Planned HU-VEN-02 | Initial simulated history plan | No assigned issue found here; delivered history is HU-VEN-11/#241. |
| HU-VEN-13–15 | No assigned issues found in the complete checked inventory | Keep the gaps visible. Their origin is not established; do not invent cancelled stories or completed deliveries. |
| Planned HU-INF-03 | Contract-simulated service plan | No assigned issue found here; real-service evidence supersedes the need to describe Sales as simulated. No cancellation decision is inferred. |
| HU-DOCS-55/56 | Their original contract scope was reassigned while the IDs delivered topology corrections | HU-DOCS-55's original Auth scope is HU-DOCS-72/#137; HU-DOCS-56's original Customers scope is HU-DOCS-73/#138. HU-DOCS-71/#136 is the related schema-prefix correction. Preserve these IDs. |
| HU-DOCS-89–93 | Existing assigned issues #207/#215/#221/#223/#227 | Keep them assigned; DOCS-93 was merged by PR #229. |
| HU-INF-01's 17 repositories | Historical common-files delivery before the current two-infrastructure inventory | Keep its original scope; the current inventory has 18 code repositories, including INF-04's Mongo repository. |

The inherited AUTH/CLI/PRO/VEN prefixes are retained for continuity with assigned IDs and existing traces. New prefixes use English names. The historical FE-01/02 and early discovery work are not renumbered because later implementation stories start at FE-03.

### Responsibility and lifecycle

| Story | Responsible person | Review coordination |
|---|---|---|
| HU-DOCS-94 / #252 | Jordan Ramirez Gallego / JordanRG420 | SergioAndres17 |
| HU-DOCS-95 / #253 | Sergio Andrés Ordóñez Díaz / SergioAndres17 | JordanRG420 |
| HU-ARQ-28 / #254 | Angel Gustavo Solano Trujillo / AsolanoT | Saga/security/operation reviewers as specified by the issue |
| HU-DOCS-96 / #255 | Fredman Santiago Plazas Artunduaga / SantiagoPlazas2005 | AsolanoT |
| HU-DOCS-97 / #256 | Angel Gustavo Solano Trujillo / AsolanoT | Peer review independent of the author |

All five stories above belong to Sprint 12 — Week 12. These are story responsibilities, not permanent service ownership. Follow [DoR](definition-of-ready.md), [DoD](definition-of-done.md) and [Git conventions](git-conventions.md).

Issues #186/#231/#234/#235/#243/#244/#246 were closed on 2026-10-24. Their closure does not convert unexecuted tests, absent reviews or historical TDD gaps into passed criteria. HU-INF-02/#239 remains open. The catalog and evidence index preserve the remaining boundaries.

The board's Status and Environment fields must be reconciled to actual issue, PR and revision evidence. This preparation did not inspect those fields and makes no claim to have updated them.

---

## Backlog tool

**Tool:** GitHub Projects
**Board URL:** [Synkro Tech — Backlog](https://github.com/orgs/code-corhuila/projects/24)

### Board columns
| Column | Meaning |
|--------|---------|
| What did I do? | Record of what was completed in the period |
| Up Next | Next planned tasks |
| Blockers | Impediments or dependencies |
| In Review | Under review by another team member |
| Done | Meets the Definition of Done (DoD) and is closed |

---

## Team velocity

| Sprint | Items completed | Notes |
|--------|----------------------|-------|
| Sprint 0 (weeks 1-2) | 8 HU | Discovery documentation (PDR, ADR-001) |
| Sprint 3 (week 3) | 22 HU | PDR/ADR correction + context map + populating `00-governance`, `01-context`, and `02-domain` |
| Sprint 4 (week 4) | 5 HU | Service catalog, user-stories.md/NFRs formalization, panoramic MVP (Cut 2) |
| Sprint 5-6 (weeks 5-6) | 3 HU | ADR-001 publication, deployment + threat model, ADR-002 sale authorship (HU-04) |
| Sprint 7 (week 7) | 10 HU | FR/NFR unification and traceability matrix (HU-DOCS-25, 26, 27), git-conventions alignment (HU-DOCS-28), ADR-003 + downstream and hexagonal architecture guide (HU-ARQ-14, 15), domain events and UML diagrams (HU-DOCS-29, 30), and the first two review corrections — PR #17 citation and C4 sync note (HU-DOCS-31, 33) |
| Sprint 8 (week 8) | 26 HU | HU-08 remaining corrections (HU-DOCS-32, 34, 41 and HU-ARQ-16); HU-09 first-pass contracts (7 HU); HU-10 architecture decisions — ADR-005 to ADR-008 and downstream documents (15 HU) |
| Sprint 9 (week 9) | 19 HU | HU-11 DB topology correction (ADR-009) and contract rewrites (9 HU); HU-12 governance, context, requirements and architecture alignment (10 HU). HU-13 (9 HU: Auth and Customers contracts, DDL prefixes, domain map, context sweep, backlog closure, stack guides, ⭐ documents, developer documents) confirmed Done against the board |
| Sprint 10 (week 10) | — | Two database engines and the Angular Customers portal (ADR-010 to ADR-012, HU-14: 13 HU). First code sprint starts in parallel: common repository files across all 17 code repositories (HU-INF-01, done), the base structure of `synkro-products-api` and the gateway/host pieces that depend on no pending ADR. Implementation delivery is reconciled in the requirement catalog; weekly point totals have not been independently reconciled |
| Sprint 11 (week 11) | — | Code-delivery evidence is reconciled in the story catalog; no weekly completion or point total is inferred from the aggregate |
| Sprint 12 (week 12) | 1 HU closed: HU-DOCS-94 (#252, PR #257) | Five assigned stories: HU-DOCS-94, HU-DOCS-95, HU-ARQ-28, HU-DOCS-96 and HU-DOCS-97; documentation has no invented point estimate |
| Average | — | Calculate from recorded completed implementation points; no measured velocity is claimed here |

> **Note:** the current sprint is Sprint 12 — Week 12. The five documentation
> and architecture stories listed above are assigned to that week. Older
> weekly totals are not recalculated from issue creation dates or releases.

> **Cuts and sprints:** `04-requirements/user-stories.md` groups HUs by cut, not by
> calendar week, so its totals differ from the rows above. They reconcile: Cut 4 (52) =
> Sprint 8 (26) − 2 HUs that stay in Cut 3 (HU-DOCS-32, HU-ARQ-16) + Sprint 9 (19 done
> + 9 of HU-13, confirmed Done against the board). HU-14 (13 HU: ADR-010 to ADR-012 and
> their downstream documents) is a new cut, Cut 5 below, counted separately since it
> belongs to the two-engine/Angular correction, not the original Cut 4 themes.
> Cut 6 holds the code-phase stories, counted in story points from Sprint 10.

---

## Related documents

- Definition of Ready → `00-governance/definition-of-ready.md`
- Definition of Done → `00-governance/definition-of-done.md`
- Risk management → `15-project-control/risks.md`
- Technical debt backlog → `15-project-control/technical-backlog.md`
