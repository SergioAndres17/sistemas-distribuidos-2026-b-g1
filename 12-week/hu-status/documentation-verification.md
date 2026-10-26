# Documentation Verification

## Scope and maintenance

This file stores bounded, dated documentation-verification entries, not a running PR-discussion log. Each entry names its story, baseline commit, inspected scope, evidence and pending boundaries. Keep general review conversation in the linked GitHub PR.

Preserve completed entries as historical evidence. Add later checks as a new dated entry; never replace the original observation silently. A correction must name the earlier entry and distinguish its original finding from the newer result. Do not infer current issue, board or decision status from an old snapshot.

The original sections from **Purpose and baseline** through **References** below are the 2026-10-11 HU-DOCS-92 preparation snapshot. Their completion-boundary wording describes that time, not the present merge state. HU-DOCS-93 records the subsequent merge evidence and corrections in its own entry below.

The governance identifier register is the sole identifier ledger. An entry may describe its checked snapshot, but must not create another live register. This file has no independent authority to accept architectural proposals.

## Purpose and baseline

This record links documentation checks to the repository changes they inspect. Verification date: 2026-10-11. HU-DOCS-92 base: `92a8eba1b230d183e24a05201923b911e6ffa87c` on main.
[HU-DOCS-92 — #223](https://github.com/code-corhuila/synkro-docs/issues/223) includes the follow-up evidence corrections for [merged PR #222](https://github.com/code-corhuila/synkro-docs/pull/222).

## Retrospective review of PR #222

| Observation | Verified finding and correction |
|---|---|
| DOCS 89→92 is not explained by the catalog diff | Correct traceability concern. DOCS-89 and DOCS-90 were already closed issues, not new work delivered by PR #222. Their catalog entries and the identifier reconciliation table now link the missing records. The arithmetic itself was correct. |
| `docs(requirements)` understates the scope | Cross-cutting scope clarification is warranted. The review also explicitly recognizes the title as conventional; this is not evidence of a format violation. The corrected description identifies governance, requirements, architecture-index and project-control changes. No commit history is rewritten. |
| The preparation report is mentioned but not linked | Confirmed in the PR description and its five-file diff. This record is retrospective evidence supplied by HU-DOCS-92, not an attachment claimed to have existed in PR #222. The revised descriptions link this file. |
| DOCS and ARQ need the same accounting check | Confirmed against the authoritative identifier register linked below. Next number follows assignment, not closure or the number of stories delivered by a particular PR. |

The [identifier register and assignment/lifecycle rules](../00-governance/agile-conventions.md#identifier-reconciliation-after-hu-docs-92-creation) are the single source for identifier reconciliation. This record does not repeat its issue table or maintain live issue statuses.

PR #222 had one observed APPROVED review by `ariel5253`. Its description checks every completion item, but main still retained the earlier in-progress catalog entry and PR #220 still described downstream alignment as deferred at verification time. HU-DOCS-92 records the merged delivery and supplies accurate PR descriptions; it does not invent teammate or board evidence.

## HU-DOCS-92 preparation checks

- Catalog headings, existing structure, two or more scenarios, issue links and DOCS order were checked; historical cut totals and unrelated catalog rules were preserved.
- DOCS maxima and ARQ maxima were reconciled against the issue history: 92 and 27; next identifiers are 93 and 28. Assignment and current lifecycle authority follow the linked governance rules; a PR closing keyword does not mean the issue is already closed.
- Production login ownership, host-only credentials/session handling, the 60-second renewal margin, operation-specific errors, coordinated renewal and stale-response invalidation were inspected across the changed design sections.
- FE-AUTH-01/02 and FE-SESSION-01 through 07 exist as unique planned scenarios and are traced to FR-010/NFR-004; no test-file or execution claim was added.
- New Markdown links were checked against repository paths and the fetched issue/PR inventory. The patch and ZIP were checked against the exact baseline and replacement contents.
- The upload set contains no accepted ADR, API YAML or domain-model changes. The complete diff remains below 400 changed lines.

## Review corrections for PR #225

- The identifier register distinguishes issue assignment from completion. `Closes #223` takes effect on merge; the next-number counter advances on issue creation.
- The duplicated five-row issue table was removed from this record; identifier links and accounting are maintained only in the governance register.
- During review, this file is available in [PR #225's file changes](https://github.com/code-corhuila/synkro-docs/pull/225/files). After merge, descriptions reference [the record on main](https://github.com/code-corhuila/synkro-docs/blob/main/15-project-control/documentation-verification.md), which survives deletion of the working branch.
- The governance register defines proposal, assigned/consumed ID and next number. It has no independent live-status column.

## Completion boundary

These are documentation preparation checks, not execution of frontend tests, Auth migrations or initialization. PR #220/#222 metadata edits, teammate review, approval of the new PR, live checks, merge and board closure still require their actual external evidence. HU-DOCS-92/#223 is now closed through merged PR #225, as recorded in the HU-DOCS-93 entry below; the preparation itself did not supply implementation execution evidence.

## References

- Session policy → [Authentication](../07-api/authentication.md#browser-session)
- Planned frontend scenarios → [Testing strategy](../11-quality/testing-strategy.md#planned-production-login-and-session-scenarios)
- Identifier accounting → [Agile conventions](../00-governance/agile-conventions.md#identifier-reconciliation-after-hu-docs-92-creation)
- Story records → [User stories](../04-requirements/user-stories.md)

## HU-DOCS-93 verification entry — 2026-10-11

**Story:** [#227](https://github.com/code-corhuila/synkro-docs/issues/227). **Baseline:** `395f60191dc39d153145069f2479c9e1a5e37fc3` on main. **Scope:** existing Auth and tracking Markdown files only; no new Markdown path, ADR record, API contract or migration is included.

**Correction relationship:** extends the HU-DOCS-92 snapshot above and addresses PR #225 follow-up observations. [PR #225](https://github.com/code-corhuila/synkro-docs/pull/225) is now merged and #223 is closed; earlier preparation wording is retained as dated evidence, not a current-state assertion.

| Preparation check | Result / boundary |
|---|---|
| Paths and immutable artifacts | Every replacement path already exists; accepted ADR files, Auth YAML and SQL statement structure are preserved. |
| Policy traceability | Renewal is explicitly Proposed guidance with options/criterion/costs; existing ADRs are cited only for their actual constraints. TD-010 formalization remains pending. |
| Auth references | auth_schema, hash/lifecycle meanings and effective URL composition agree with the existing model/contract. |
| Revocation claims | Logout does not claim immediate access-JWT revocation; TD-011 records the broader reuse scope before unsupported tests are implemented. |
| Planned scenarios | AUTH-REFRESH-01/02/03, AUTH-LOGOUT-01/02 and AUTH-SECRETS-01 are unique planned backend scenarios, separate from frontend scenarios. |
| Evidence and tracking | New links/anchors, catalog structure, existing historical content and identifier accounting were checked. DOCS follows issue assignment; no parallel ledger is added. |
| Package | Complete replacement files and patch application were checked against the baseline; the diff stays below 400 changed lines. |

These are documentation preparation results. No backend/frontend implementation tests, migrations, new ADR acceptance or production initialization were executed. Issue-scope adjustment, teammate review, new-PR approval, live checks, merge and board closure require actual external evidence.

**Open boundaries:** [TD-010 and TD-011](technical-backlog.md#td-010--formalize-the-browser-renewal-policy) remain pending. Closing this documentation reconciliation does not close those architectural decisions. The new PR should link this record through its PR file view during review and the canonical main path after merge.

---

## HU-DOCS-94 verification entry — 2026-10-24

**Issue:** [#252](https://github.com/code-corhuila/synkro-docs/issues/252). **Responsible person:** JordanRG420. **Sprint / Week:** Sprint 12 — Week 12. **Requested peer reviewer:** SergioAndres17. **Documentation baseline:** main at 61056c93e6e0783a9199bf56778e9494739bc458 (merged PR #229).

This entry extends the DOCS-92/93 snapshots above. It records the implemented system after those design/documentation deliveries and the seven issue closures on 2026-10-24. It does not replace historical observations or create another live ID ledger.

### Scope, method and limits

Authenticated read-only GitHub inspection covered all 18 code repositories, main/develop trees, qa refs, annotated tag objects and peeled commits, current issue inventory, existing evidence and CI metadata. The complete docs issue inventory was paginated across three pages (256 issue/PR records, 167 actual issues); pull requests were excluded from HU accounting.

The seven closures were checked for the delivery snapshot. Previous reconciliation inspected 49 related PRs and 84 acceptance criteria, their key reviews, CI jobs and check-run annotations. Local execution counts remain attributed to the linked reports; no backend/frontend suite, migration, browser flow or deployment was rerun by HU-DOCS-94.

Project Status/Environment fields were not inspected or changed. Main code or a version tag does not imply an actually provisioned production environment.

### Repository revision snapshot

The table is a dated evidence snapshot, not a rolling deployment lock. Tag column contains the annotated object; where v2.0.0 exists, its peeled commit equals the full main SHA in that row.

| Repository | main commit | qa commit | develop commit | v2.0.0 tag object |
|---|---|---|---|---|
| [synkro-products-api](https://github.com/code-corhuila/synkro-products-api/tree/8c04934ae5b51417a0b88506cc59734ef7913e64) | 8c04934ae5b51417a0b88506cc59734ef7913e64 | 22245634d2f10a5667899a24d5c1511d4f3a0891 | af06bb056fedb7b9f83d518436c3370a0f1373d1 | 8225a6b1049271be31a1c85a0cc18888d0d665ac |
| [synkro-products-portal](https://github.com/code-corhuila/synkro-products-portal/tree/b407475b363b9ac7fcbe3512852ba4146a96dadf) | b407475b363b9ac7fcbe3512852ba4146a96dadf | e5b03cf438ecae4cf189460abe65dc07d9912c8c | 31f1dc235e50c69030c60f5d5faca5d2c1786270 | 0d18bd9706aa60a074ea66c6292fca3a93ba4172 |
| [synkro-products-db](https://github.com/code-corhuila/synkro-products-db/tree/043e485d16018fc4fb4cd60694c2a521c143ddbd) | 043e485d16018fc4fb4cd60694c2a521c143ddbd | 5f15eb094a7214962a4aae7e7d768e9c250440b9 | 0beae79920d9af83865bdebaf6b316a2b2a57592 | d480c88e41659396670bfd7e1a593c3f7e4ab611 |
| [synkro-sales-db](https://github.com/code-corhuila/synkro-sales-db/tree/344d917264bbb5ad17c40d2e3a33f217a7a94866) | 344d917264bbb5ad17c40d2e3a33f217a7a94866 | 218919d0b671f9607b3dd909e85296e86aebc109 | 9cfbde7db4bea7bacad70370c60f91f8eb917c04 | 5bbad891ba71e55ce37607f998c47da6c97e658e |
| [synkro-sales-api](https://github.com/code-corhuila/synkro-sales-api/tree/f163eb60552e72f2f5716dd91cb227ae23aa8683) | f163eb60552e72f2f5716dd91cb227ae23aa8683 | c1e3de3f8904f934a8aaa60b52187d947dfdeb39 | 3459794f4f98a4cee3a283f3278d88b7837f4798 | 0a70e42c90aefe3859c90dcd1572a99301da59e5 |
| [synkro-sales-portal](https://github.com/code-corhuila/synkro-sales-portal/tree/179dcf5a3bdd2b5fa09d4602cacf61c3a41e761e) | 179dcf5a3bdd2b5fa09d4602cacf61c3a41e761e | b76bb332cd61b83c19d23aa1ad117f01a4874651 | d691eec160ba50ed417e5acbbe5d02496f2dea8d | 3efec7412eae027c3092d1a95c62686b5281b997 |
| [synkro-workflow](https://github.com/code-corhuila/synkro-workflow/tree/09a193866f05f3714a0bd1ad6d7559a9f00b3611) | 09a193866f05f3714a0bd1ad6d7559a9f00b3611 | 569a9cb1194e0b76c94c9993f5fe32fc7899f7b8 | 2aa2f25f4411f0c2bbdbf7cbfb57701fcee7c7d7 | 757b64c36be9eef8a180b57467fbb7e282d4f3b1 |
| [synkro-worker](https://github.com/code-corhuila/synkro-worker/tree/8ccef730f849807809c5b11c135bc7cae3ce6342) | 8ccef730f849807809c5b11c135bc7cae3ce6342 | 208dc4a45bef08cdc3c867a7a43fe6823c3bce88 | ea0bb32488f6db09c26877fad4dd27c9b2ef147f | Not present |
| [synkro-api-gateway](https://github.com/code-corhuila/synkro-api-gateway/tree/5e5c3d5bdd67ae8c20eba2eaf2241d43a67adcde) | 5e5c3d5bdd67ae8c20eba2eaf2241d43a67adcde | 1a25e02fa7f5073467e855c5e06b9e6147b9b61e | 24a4ab157bb000e152e5e4567f16deb3ebadcf05 | 4718c3526aede5e9bae82d5f022044a6c4275c42 |
| [synkro-front](https://github.com/code-corhuila/synkro-front/tree/c43d024790cfc6345ddd266c04e6967a8d035702) | c43d024790cfc6345ddd266c04e6967a8d035702 | b4d9c6c498135b5700b645ed864bbdcd29e6c4dd | 70a1b87b380e2ab5a9896fd5a9c13e8096b918c8 | eee0c207d6a994756f6ce33d3f9d16b05197e336 |
| [synkro-infra-mongo](https://github.com/code-corhuila/synkro-infra-mongo/tree/89a1a80bfd28c3c3d5287e3be2e52a7ac57e167f) | 89a1a80bfd28c3c3d5287e3be2e52a7ac57e167f | aaf3335d950774597d439246ff52522466f29740 | aa30b1b96e649ee6d2ff63e337add8f5164fff06 | 2967cbbd5f0fa5b7f04cf6d1429bda33a33e3387 |
| [synkro-infra-postgres](https://github.com/code-corhuila/synkro-infra-postgres/tree/649b811c5ad8711dd1033f2a08f35269ae20c924) | 649b811c5ad8711dd1033f2a08f35269ae20c924 | 649b811c5ad8711dd1033f2a08f35269ae20c924 | f49117420038f35aedbb3aba3538b82af9078812 | Not present |
| [synkro-auth-db](https://github.com/code-corhuila/synkro-auth-db/tree/6c8270372adca0bee7a6e62a8f14c33a9d8977e5) | 6c8270372adca0bee7a6e62a8f14c33a9d8977e5 | 08587e7377b1daa38b460117b97f188b08457c6c | 8a8deb658f0336cc4ad2aaaa7654d7bfd5012a6b | 99c3e8c6f730b80b377cf99f5c5f5d64f7b7596e |
| [synkro-auth-api](https://github.com/code-corhuila/synkro-auth-api/tree/e3ea374fbfb428e2583b5a776295e60b0e229696) | e3ea374fbfb428e2583b5a776295e60b0e229696 | 7a188055b2ad1187ac9d39d67e8f19be88b6259d | 38ed0312b0bfb32eef4bb0f4b393b9acd343004d | 5e031387310539c9ee15450f54d1e2c728afd871 |
| [synkro-auth-portal](https://github.com/code-corhuila/synkro-auth-portal/tree/15f7334d33822aaace24ec25fa1a1097a43dbad0) | 15f7334d33822aaace24ec25fa1a1097a43dbad0 | 446d065922dd454f27133183674b4ad2f3fd9a2b | 02e71fcab39a98aa14a082c68957e750c35347c0 | 9eae08ab88aeca7907f54502bff987bcacc71387 |
| [synkro-customers-api](https://github.com/code-corhuila/synkro-customers-api/tree/28caff4f2d4c5e562416db7400061675e4b6766b) | 28caff4f2d4c5e562416db7400061675e4b6766b | 8f54aa424cd08fbed75dafcfa1e1dbab3b6dabc4 | 58a0ef9f6fc05b788b2e65002468cd9c14783540 | f606594c44b56ff18968e09e0377e517b8b24f86 |
| [synkro-customers-portal](https://github.com/code-corhuila/synkro-customers-portal/tree/4208839dad3ccfb8eeb4feff12352c8e4cd73695) | 4208839dad3ccfb8eeb4feff12352c8e4cd73695 | 39e03908c9391f671b4808b6e95b943e1f6f9292 | 1f322bbf19764c074dbb3950f118742a71a669d2 | 02b079fccd1d9ad7388c572da54a281ceaf8d78f |
| [synkro-customers-db](https://github.com/code-corhuila/synkro-customers-db/tree/4c24733e812adec24658fbce9d8518eda52adda4) | 4c24733e812adec24658fbce9d8518eda52adda4 | 49367c1b1d2c1bd2233102a43c51aed19026a72b | a23ba04f94ff0f150c5bafd9801cc0c6d9e98bd7 | 8116db4d9d8368e8a7776d0c7532d52be59bdd90 |

Sixteen repositories have v2.0.0; Worker and PostgreSQL Infra do not. Source blobs match between main/develop in 15 repositories, despite different promotion SHAs. Content differences:

| Repository | main versus develop | Consequence |
|---|---|---|
| synkro-products-portal | Seven files: CI, README, Dockerfile, Compose, Vite, deploy test and root-deploy test | Root remote-delivery corrections in develop are not attributed to its tag |
| synkro-sales-api | Six files: .env.example, CI, README, Compose and two root deployment tests | Do not attribute develop's root integration to the released source |
| synkro-infra-postgres | 169 changed paths; main/qa have two original files | Operational root exists in develop/candidates, not in main |

[Root PR #13](https://github.com/code-corhuila/synkro-infra-postgres/pull/13) and [stacked browser PR #14](https://github.com/code-corhuila/synkro-infra-postgres/pull/14) remain candidate evidence. Their heads at the inspected cut were 8139a43e786f3ab7adf6367065a6165d8d83c15e and d23470ddcaf248cfc6c6da92dfb40b31c85694ed. They do not become delivered main content through a link in this document.

### Latest CI observations

Each row identifies the actual run branch and head; this is not a claim that every failure has the same cause or that every earlier successful run is invalid.

| Repository | Run / result | Run branch | Run head |
|---|---|---|---|
| synkro-products-api | [39467903051: failure](https://github.com/code-corhuila/synkro-products-api/actions/runs/39467903051) | main | 8c04934ae5b51417a0b88506cc59734ef7913e64 |
| synkro-products-portal | [38479489257: success](https://github.com/code-corhuila/synkro-products-portal/actions/runs/38479489257) | main | b407475b363b9ac7fcbe3512852ba4146a96dadf |
| synkro-products-db | [39162836250: failure](https://github.com/code-corhuila/synkro-products-db/actions/runs/39162836250) | main | 043e485d16018fc4fb4cd60694c2a521c143ddbd |
| synkro-sales-db | [38392130380: success](https://github.com/code-corhuila/synkro-sales-db/actions/runs/38392130380) | main | 344d917264bbb5ad17c40d2e3a33f217a7a94866 |
| synkro-sales-api | [39467971335: failure](https://github.com/code-corhuila/synkro-sales-api/actions/runs/39467971335) | main | f163eb60552e72f2f5716dd91cb227ae23aa8683 |
| synkro-sales-portal | [39851609595: failure](https://github.com/code-corhuila/synkro-sales-portal/actions/runs/39851609595) | main | 179dcf5a3bdd2b5fa09d4602cacf61c3a41e761e |
| synkro-workflow | [39467995641: failure](https://github.com/code-corhuila/synkro-workflow/actions/runs/39467995641) | main | 09a193866f05f3714a0bd1ad6d7559a9f00b3611 |
| synkro-worker | [39849478414: failure](https://github.com/code-corhuila/synkro-worker/actions/runs/39849478414) | main | 8ccef730f849807809c5b11c135bc7cae3ce6342 |
| synkro-api-gateway | [38400685293: success](https://github.com/code-corhuila/synkro-api-gateway/actions/runs/38400685293) | main | 5e5c3d5bdd67ae8c20eba2eaf2241d43a67adcde |
| synkro-front | [39468072408: failure](https://github.com/code-corhuila/synkro-front/actions/runs/39468072408) | main | c43d024790cfc6345ddd266c04e6967a8d035702 |
| synkro-infra-mongo | [38458170071: success](https://github.com/code-corhuila/synkro-infra-mongo/actions/runs/38458170071) | main | 89a1a80bfd28c3c3d5287e3be2e52a7ac57e167f |
| synkro-infra-postgres | [39951913663: startup_failure](https://github.com/code-corhuila/synkro-infra-postgres/actions/runs/39951913663) | feat/delivered-composition-lock | 8139a43e786f3ab7adf6367065a6165d8d83c15e |
| synkro-auth-db | [39467874277: failure](https://github.com/code-corhuila/synkro-auth-db/actions/runs/39467874277) | main | 6c8270372adca0bee7a6e62a8f14c33a9d8977e5 |
| synkro-auth-api | [38480964935: success](https://github.com/code-corhuila/synkro-auth-api/actions/runs/38480964935) | main | e3ea374fbfb428e2583b5a776295e60b0e229696 |
| synkro-auth-portal | [38473109917: success](https://github.com/code-corhuila/synkro-auth-portal/actions/runs/38473109917) | main | 15f7334d33822aaace24ec25fa1a1097a43dbad0 |
| synkro-customers-api | [38449900586: success](https://github.com/code-corhuila/synkro-customers-api/actions/runs/38449900586) | main | 28caff4f2d4c5e562416db7400061675e4b6766b |
| synkro-customers-portal | [38475193648: success](https://github.com/code-corhuila/synkro-customers-portal/actions/runs/38475193648) | main | 4208839dad3ccfb8eeb4feff12352c8e4cd73695 |
| synkro-customers-db | [38449392314: success](https://github.com/code-corhuila/synkro-customers-db/actions/runs/38449392314) | main | 4c24733e812adec24658fbce9d8518eda52adda4 |

Earlier exact-head successes and their test scope remain in [testing-strategy.md](../11-quality/testing-strategy.md#execution-references-checked-during-reconciliation). The Sales API, Workflow and Sales Portal release failures there have directly checked annotations reporting billing lock before any step. That cause is not generalized to every failure above.

### Closed issues and preserved boundaries

| Issue / owner | Closure observed | Delivery evidence | Retained limitation |
|---|---|---|---|
| [#186](https://github.com/code-corhuila/synkro-docs/issues/186) / SergioAndres17 | 2026-10-24 | Sales API #2 → #10 → #17 → #23; exact original peer approval; later QA CI success | Initial shape-only JWT gate did not meet the well-formed-invalid-token criterion; real RS256 arrived later in HU-VEN-09 |
| [#231](https://github.com/code-corhuila/synkro-docs/issues/231) / AsolanoT | 2026-10-24 | Gateway #3 → #6 → #9 → #10; CI and final teacher approval | Controlled contract checks and real Auth acceptance are distinct; full-platform acceptance is not this HU's scope |
| [#234](https://github.com/code-corhuila/synkro-docs/issues/234) / JordanRG420 | 2026-10-24 | Auth #9 → #19 → #33 → #37; real PostgreSQL CI; later Gateway acceptance; final release approval | Original peer approval preceded a CI-only pin correction; no additional domain implementation is invented |
| [#235](https://github.com/code-corhuila/synkro-docs/issues/235) / JordanRG420 | 2026-10-24 | Sales API #5 → #13 → #20 → #23; real MongoDB CI on the exact approved implementation head | Inactive Sale/Detail restoration and central model drift remain DOCS-95 scope; root pin conflict belongs to INF-02 |
| [#243](https://github.com/code-corhuila/synkro-docs/issues/243) / AsolanoT | 2026-10-24 | Workflow #4 → #8 → #12 → #13; original process CI; later real acceptance and root S4 report | Old Customers 501 is historical. Review/contract/decision follow-up and unsuccessful release CI are not erased |
| [#244](https://github.com/code-corhuila/synkro-docs/issues/244) / SergioAndres17 | 2026-10-24 | Replacement Customers DB #3 → #5 → #7 → #8; CI and final approval; CLI-DB-01…14 report | PR #2 closed unmerged. Initial functional SQL red was blocked by Docker; later test-first corrections do not alter the past |
| [#246](https://github.com/code-corhuila/synkro-docs/issues/246) / AsolanoT | 2026-10-24 | Sales Portal increments/QA/release #43; 242 tests and 18/18 real-service browser cases reported | Current-head peer/codeowner review and QA/Pact limitations remain recorded; exact release CI did not execute |
| [#239](https://github.com/code-corhuila/synkro-docs/issues/239) / AsolanoT | OPEN | PostgreSQL foundation plus root candidate reports | Sales DB pin agreement, root validation/reproduction, promotion and full acceptance remain open |

Closure records reflect issue lifecycle, not automatic satisfaction of every quality gate. This reconciliation does not reopen the closed issues, re-close them, rewrite their history or create replacement implementation stories.

### Review and integration decisions

Recorded owner instructions authorized normal integration with local evidence during the billing impediment:
- [Sales API #23 decision](https://github.com/code-corhuila/synkro-sales-api/pull/23#issuecomment-6297205003).
- [Workflow #13 decision](https://github.com/code-corhuila/synkro-workflow/pull/13#issuecomment-6297208715).
- [Sales Portal #43 decision](https://github.com/code-corhuila/synkro-sales-portal/pull/43#issuecomment-6350923886).

Those decisions explain the merges. They do not constitute ariel5253 approval, peer review, Pact acceptance or green remote checks. Sales API #23's earlier teacher review is DISMISSED; Workflow #13 and Sales Portal #43 have no formal teacher approval in the checked review lists. Auth #37, Gateway #10 and Customers DB #8 do have final-head teacher approvals.

Preserve these differences; the current DoD and Git conventions are not weakened to make historical deliveries appear uniform.

### Corrections and assignment authority

The central [identifier register](../00-governance/agile-conventions.md#identifier-reconciliation-after-hu-docs-92-creation) now reflects consumed DOCS-94–97 and ARQ-28, next DOCS-98/ARQ-29, #230 VEN-16 and #237 VEN-17. It explains gaps and substitutions by source without inventing missing issues. This entry does not repeat the assignment ledger.

The next ADR is ADR-015; no ADR is created or accepted by DOCS-94. HU-DOCS-97/#256 belongs to AsolanoT. DOCS-93/#227 is closed and merged through PR #229, correcting its older preparation wording.

### File and acceptance checks

| File | Current addition / correction |
|---|---|
| 00-governance/agile-conventions.md | Counters, assigned-ID register, gaps/aliases and story responsibility |
| 00-governance/definition-of-ready.md | Current dependencies, branch/environment/CI readiness and documentation checks |
| 00-governance/definition-of-done.md | Evidence levels, unchanged gates and closure versus acceptance |
| 00-governance/git-conventions.md | qa- rationale and exact flow; valid release/ naming and GitHub issue references |
| 00-governance/documentation-rules.md | Current 18-repository names, canonical sources and dated-history maintenance |
| 04-requirements/functional.md | Current-status pointer and explicit historical labeling of the Corte 1 status table; FR definitions unchanged |
| 04-requirements/non-functional.md | Current-status pointer and historical labeling of earlier validation observations; metrics and priorities unchanged |
| 04-requirements/traceability-matrix.md | Ten FR chains, nine NFR validation boundaries and 58 inverse implementation rows |
| 04-requirements/user-stories.md | Current Cut 6/7 reconciliation, 39 additional Cut 8 stories and five approved documentation stories |
| 11-quality/testing-strategy.md | Scenario-to-code mapping, exact test/source links and execution evidence scopes |
| 15-project-control/README.md | Navigation entry for the dated documentation-verification record |
| 15-project-control/documentation-verification.md | This bounded revision/CI/closure snapshot |

HU-DOCS-94's success case is traceable reports: FR-008/009 → HU-VEN-10/#245 → report tests → query_reports.go and the Mongo/report evidence. Its error cases remain visible: closed issue with incomplete evidence, historical alias collision, unassigned gap and failed/unexecuted check. No cell is promoted to fully accepted merely from issue closure.

### Completion and follow-up boundary

This delivery prepares twelve complete Markdown replacements and their link/path/counter consistency checks. Source paths were checked against the frozen trees. The evidence index was additionally corrected by reading test contents and checking their actual subjects: an existing path alone does not establish the right test-to-implementation relationship. Cross-component tests list their relevant implementations explicitly, rather than being presented as unit tests of an unrelated file. No accepted ADR, requirement definition/metric/priority, domain/data model, YAML contract, applied migration, CODEOWNERS or production source is modified. Existing Markdown sections, tables and comments are updated in place; no full prior version or collapsible archive is embedded. Source history remains in Git.

HU-DOCS-94 is now closed. [PR #257](https://github.com/code-corhuila/synkro-docs/pull/257) merged to main as [2e07223829a51573574b0c6fc134b4d321a118da](https://github.com/code-corhuila/synkro-docs/commit/2e07223829a51573574b0c6fc134b4d321a118da). The GitHub PR record confirms merged=true and merged_at=2026-10-25T01:29:51Z (2026-10-24 in America/Bogota). That merge is evidence of integration; individual review/check outcomes must be read from their own records rather than inferred from the merge.

Later DOCS-95, ARQ-28, DOCS-96 and DOCS-97 deliveries add a new dated verification entry and update affected canonical sections when their real evidence exists. This reconciliation is complete for the inspected cut; it does not mark those follow-up stories complete or pre-approve their outcomes.

---

## HU-DOCS-95 verification entry — 2026-10-24

**Issue:** [#253](https://github.com/code-corhuila/synkro-docs/issues/253). **Responsible person:** SergioAndres17. **Peer reviewer:** JordanRG420. **Sprint / Week:** Sprint 12 — Week 12. **Documentation baseline:** main 2e07223829a51573574b0c6fc134b4d321a118da, merged [DOCS-94 PR #257](https://github.com/code-corhuila/synkro-docs/pull/257); #252 is closed.

### Scope and inspected evidence

Domain entities/context boundaries were read before physical models, dictionary and contracts. Nine owning API/database/Workflow main revisions were checked against the prior delivery snapshot and remain the same. Exact source/test/migration links below freeze their commits; historical handoff lifecycle states are not reused as current issue or acceptance state.

| Topic | Inspected source / evidence | Result and boundary |
|---|---|---|
| Adjustment receipt | [01_ddl/04_alter/V017__add_stock_after_to_stock_adjustment.sql](https://github.com/code-corhuila/synkro-products-db/blob/043e485d16018fc4fb4cd60694c2a521c143ddbd/01_ddl/04_alter/V017__add_stock_after_to_stock_adjustment.sql); [internal/adapter/out/persistence/postgres_stock.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/adapter/out/persistence/postgres_stock.go); [internal/adapter/out/persistence/postgres_stock_integration_test.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/adapter/out/persistence/postgres_stock_integration_test.go) | New rows persist an immutable resulting stock in the same transaction; legacy NULL receipts have an explicit current-product fallback |
| Sale/Detail lifecycle | [internal/adapter/out/persistence/sale_document.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/out/persistence/sale_document.go); [internal/adapter/out/persistence/reports.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/out/persistence/reports.go) | Inactive roots/details cannot be restored by the domain mapper; report filtering does not close TD-012 |
| UTC and ownership | [internal/adapter/in/httpapi/report_query.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/in/httpapi/report_query.go); [internal/adapter/out/persistence/reports.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/out/persistence/reports.go) | Inclusive UTC days via exclusive next midnight; token-derived owner; grouped stable pagination; frozen integer amounts |
| Replay / rejection | [workflow-app/src/test/java/co/edu/corhuila/synkro/workflow/app/WorkflowHttpTest.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-app/src/test/java/co/edu/corhuila/synkro/workflow/app/WorkflowHttpTest.java); [workflow-adapters/src/test/java/co/edu/corhuila/synkro/workflow/adapter/out/http/HttpSaleParticipantsTest.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-adapters/src/test/java/co/edu/corhuila/synkro/workflow/adapter/out/http/HttpSaleParticipantsTest.java); [workflow-core/src/main/java/co/edu/corhuila/synkro/workflow/application/usecase/RegisterSale.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-core/src/main/java/co/edu/corhuila/synkro/workflow/application/usecase/RegisterSale.java) | Source/tests exist; adoption of changed normative safety behavior depends on the still-open ARQ-28 decision |
| Durable recovery | [workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/out/persistence/JdbcSagaStore.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/out/persistence/JdbcSagaStore.java); [workflow-app/src/main/java/co/edu/corhuila/synkro/workflow/app/WorkflowComposition.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-app/src/main/java/co/edu/corhuila/synkro/workflow/app/WorkflowComposition.java) | Private persisted correlation/pending/attempt fields and unresolved suspension; no new public state or instance |
| Lock / retry / resilience | Auth stores, [workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/out/persistence/JdbcSagaStore.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/out/persistence/JdbcSagaStore.java), [workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/out/http/HttpSaleParticipants.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/out/http/HttpSaleParticipants.java) and [workflow-app/src/main/java/co/edu/corhuila/synkro/workflow/app/WorkflowComposition.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-app/src/main/java/co/edu/corhuila/synkro/workflow/app/WorkflowComposition.java) | TD-014/015/016 remain open; no invented complete namespace isolation, Circuit Breaker or retry compliance |
| Health | [internal/adapter/in/httpapi/handler.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/adapter/in/httpapi/handler.go) and the other owning controllers | Corrected the prior DOCS-94 aggregate health wording: Products has liveness only; store-sensitive 503 remains TD-017 |

### Decision and publication gate

HU-ARQ-28/#254 remains open; the inspected register has no Accepted ADR-015. The Workflow candidate marks its dependency explicitly. The cross-cutting, pattern, threat, domain and data clauses distinguish observed implementation from accepted design. Do not integrate decision-dependent normative changes as already approved; complete ARQ-28 review/acceptance and reconcile final references first. Accepted ADR-007 and all other accepted records remain byte-for-byte untouched.

The seven new TD records are unresolved delivery discrepancies/decision dependencies, with owners and explicit closing evidence. They do not mean defects were fixed, accepted or deferred outside required MVP scope. No new HU/issue, migration, infrastructure instance, public saga field or status is created.

### Feedback reconciliation

Review findings were checked against source, not accepted solely from the review text. Forty-six versioned PostgreSQL migration files were inspected at the frozen Auth/Customers/Products/Workflow revisions. Models now reproduce their actual SQL and paths; Auth audit V014–V017 is delivered, Products V009 creates stock_adjustment, and TD-018 retains physical-schema differences. No environment application is inferred and no migration source was changed.

Removed the obsolete empty-tests/monolith notes, reconciled STRIDE IDs/categories/counts and MongoDB scope, centralized the saga adoption boundary in [REST Guidelines](../07-api/guidelines.md#saga-decision-status), and clarified that a review draft does not replace the accepted decision. Sorted all response codes and normalized OpenAPI string quoting. Sales list/report 400 descriptions and safe examples now explicitly cover reversed dates and signed-64-bit offset overflow, matching report_query.go and reports_test.go. Workflow keeps closed replay-error schemas: a permissive shared Error reference alone would weaken the no-private-fields check.

### Files and verification method

| Path | Bounded correction |
|---|---|
| 00-governance/agile-conventions.md | One Week 12 completion update for DOCS-94; existing sprint table, ceremonies and counters unchanged |
| 02-domain/entities-and-rules.md | Receipt/replay meaning, report projections and explicit lifecycle/recovery discrepancy |
| 02-domain/domain-map.md | Existing context ownership with a bounded integration decision note |
| 06-data/models.md | Exact delivered migration paths/SQL, Auth audit V014–V017, real grants/constraint names, V017 receipt, private metadata and advisory usage/collision registry |
| 06-data/data-dictionary.md | stock_after, active/decoder gap, private execution fields and query boundaries |
| 05-architecture/cross-cutting.md | Health URLs, safe errors, actual retry/configuration evidence and pending safety decision |
| 05-architecture/pattern-guide.md | Real ordered stock locks, Mongo report model, missing Circuit Breaker and evidence-sensitive failure paths |
| 05-architecture/security-threat-model.md | Unique STRIDE IDs, 33-threat summary with three pending, two-engine scope and saga/replay/lock boundaries |
| 07-api/guidelines.md | Unversioned health server override, 400/500 and permitted replay boundaries |
| 07-api/contracts/openapi/synkro-auth-api.yaml | Health URL and generic persistence/unexpected 500 responses |
| 07-api/contracts/openapi/synkro-customers-api.yaml | Health URL, malformed UUID 400 and generic 500 responses |
| 07-api/contracts/openapi/synkro-products-api.yaml | Health URL/gap, response omissions and adjustment receipt replay |
| 07-api/contracts/openapi/synkro-sales-api.yaml | Health URL, detail 400/500, report query/range semantics and lifecycle gap |
| 07-api/contracts/openapi/synkro-workflow.yaml | Explicitly gated replay/recovery candidate, unchanged status/projection and health/errors |
| 04-requirements/functional.md | Correct FR-006 technical reference and FR-007 contract chain; business wording/priorities unchanged |
| 04-requirements/traceability-matrix.md | Bounded contract/evidence mapping and correction of Products health overclaim |
| 04-requirements/user-stories.md | DOCS-94 actual merge and DOCS-95 complete Week 12 specification/preparation state |
| 11-quality/testing-strategy.md | Current health limitation and document-versus-runtime verification boundaries |
| 15-project-control/technical-backlog.md | TD-012–018 unresolved discrepancies, owners and closing evidence |
| 15-project-control/documentation-verification.md | This bounded entry |

The five affected contracts retain the OpenAPI 3.0.3 structure and use patch document version 2.1.1, separately from a service release tag. The required checks cover YAML duplicate/syntax errors, local reference resolution, OpenAPI schema validity, effective health URLs, response/example schemas, unchanged business requirement scope, Markdown links/anchors, known test-to-code references and preserved migration/ADR sources. Outdated SQL examples in models.md were corrected to literal inspected migration excerpts; preservation applies to the source migrations themselves, not to inaccurate former examples. Outdated SQL examples in models.md were corrected to literal inspected migration excerpts; preservation applies to the source migrations themselves, not to inaccurate former examples.

These are static document/contract checks. No backend suite, database migration, participant process, browser flow or deployment was rerun. Test-file presence and schema-valid examples are not newly executed acceptance. No peer approval, successful CI, merged DOCS-95 PR or issue closure is invented.

### Completion boundary

Independent peer/domain review, the relevant accepted ARQ-28 decision, required main approval and applicable PR checks remain external evidence to supply. HU-DOCS-95 is prepared for review and remains open. Existing document headings, tables and introductory comments are preserved; corrections are integrated in place without embedding previous complete versions.
