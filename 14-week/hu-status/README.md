<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       14-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 14

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Sergio Andres Ordoñez Diaz
- GITHUB_USER: SergioAndres17
- TEAM: Group 10 - synkro-tech
- SPRINT_GOAL: Deliver HU-VEN-20 (clickable sales catalogue, yearly commercial sale numbers, customer search and a preserved cart) as small, reviewable PRs verified with a real-database suite and a real-browser acceptance, after closing last week's threat model and Sales DB release-reference fix, and report separately what GitHub Actions could not run (account locked for billing).
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
| Last week - W13 | STRIDE threat model of synkro-api-gateway -> synkro-products-api | done (submitted; merge decision belongs to the Tech Lead) | [PR #270](https://github.com/code-corhuila/synkro-docs/pull/270); commits [a172df9](https://github.com/code-corhuila/synkro-docs/commit/a172df9), [9797d9f](https://github.com/code-corhuila/synkro-docs/commit/9797d9f), [23a7082](https://github.com/code-corhuila/synkro-docs/commit/23a7082) |
| Last week - W13 | Security backlog from the highest risks | todo (backlog items) | issues [#271](https://github.com/code-corhuila/synkro-docs/issues/271), [#272](https://github.com/code-corhuila/synkro-docs/issues/272), [#273](https://github.com/code-corhuila/synkro-docs/issues/273) |
| HU-INF-05 (#263) | synkro-infra-mongo: align the Sales DB release reference | done (re-applied to develop, qa and main) | [PR #13](https://github.com/code-corhuila/synkro-infra-mongo/pull/13) (develop, [a9af7f2](https://github.com/code-corhuila/synkro-infra-mongo/commit/a9af7f2)), [PR #14](https://github.com/code-corhuila/synkro-infra-mongo/pull/14) (qa, [f772bf4](https://github.com/code-corhuila/synkro-infra-mongo/commit/f772bf4)), [PR #15](https://github.com/code-corhuila/synkro-infra-mongo/pull/15) (main, [16d25c5](https://github.com/code-corhuila/synkro-infra-mongo/commit/16d25c5)) |
| HU-VEN-20 (#305) | Sales DB: yearly commercial number structures | doing (PR open for review, not merged) | [PR #18](https://github.com/code-corhuila/synkro-sales-db/pull/18); [commit e00e0d3](https://github.com/code-corhuila/synkro-sales-db/commit/e00e0d3fd27cc012e25d8d4d82a41f3449b7ca72) |
| HU-VEN-20 (#305) | Sales API: stable commercial numbers; exhausted year answered as 422 | doing (PR open for review, not merged) | [PR #35](https://github.com/code-corhuila/synkro-sales-api/pull/35); commits [6ef3f16](https://github.com/code-corhuila/synkro-sales-api/commit/6ef3f16), [5361c75](https://github.com/code-corhuila/synkro-sales-api/commit/5361c75e687315906e67bc61517c0a0eb4b214a8), [14fd0b0](https://github.com/code-corhuila/synkro-sales-api/commit/14fd0b07771379d70aa4d72d9f41b2f9a4569e73) |
| HU-VEN-20 (#305) | Sales Portal: catalogue + cart, customer search, sale in progress, history/receipts/issuer (5 stacked PRs) | doing (PRs open for review, not merged) | [#63](https://github.com/code-corhuila/synkro-sales-portal/pull/63), [#65](https://github.com/code-corhuila/synkro-sales-portal/pull/65), [#64](https://github.com/code-corhuila/synkro-sales-portal/pull/64), [#66](https://github.com/code-corhuila/synkro-sales-portal/pull/66), [#67](https://github.com/code-corhuila/synkro-sales-portal/pull/67) |
| HU-VEN-20 (#305) | Products Portal: searchable category selector | doing (PR open for review, not merged) | [PR #33](https://github.com/code-corhuila/synkro-products-portal/pull/33); [commit 7e9855b](https://github.com/code-corhuila/synkro-products-portal/commit/7e9855b1db197f7a7844c02adf604e0efa0453d3) |
| HU-VEN-20 (#305) | Replaced Sales Portal PRs (size policy) | closed as superseded, not merged | [#61](https://github.com/code-corhuila/synkro-sales-portal/pull/61), [#62](https://github.com/code-corhuila/synkro-sales-portal/pull/62) |
| HU-CLI-05 (#304) | Consumed Jordan's customer-search contract; requested his confirmation | doing (contract still "proposed") | [issue #304](https://github.com/code-corhuila/synkro-docs/issues/304) |

The HU-VEN-20 PRs target `develop` (the Sales Portal ones are stacked). Nothing of HU-VEN-20 was merged or promoted, and both HUs stay open.

## 2. My individual contribution

**Last week:**

- STRIDE of the gateway-to-products-api boundary in synkro-docs: found that the Products API decodes the JWT payload without verifying the RS256 signature, ranked the risk, answered a self-review and turned the top risks into issues #271, #272 and #273.
- HU-INF-05 (#263): changed the Sales DB release reference in synkro-infra-mongo to the published `v2.0.0` commit (`344d917`), updated the scripts, CI and docs, added a test of the revision pin and a verification note, and re-applied the same fix to `qa` and `main`.

**This week - Sales API and database (commercial number):**

- Verified the yearly number `VEN-YYYY-NNNNNN` against a real MongoDB 7.0 replica set: year rollover (Colombia time), the same-key concurrent case, a gap when an insert fails, and the 999999 ceiling. The mandatory real-database scenarios went from 37 to 42, all passing.
- Found that an exhausted year was answered as `500`, which the Workflow (ADR-015) does not accept as proof that registration did not commit, so stock would have stayed reserved. It is now `422 BUSINESS_RULE_VIOLATION` (closed error catalog, no new code). Three unit tests failed first; the real saga now ends `COMPENSATED` with the stock released.
- Ran `sale-check.sh` of sales-db as a whole: `ALL SALE CHECKS PASSED` (apply, validators, permissions, full rollback and re-apply).

**This week - Sales Portal:**

- Customer autocomplete using Jordan's proposed `POST /customers/search` contract: body-only text, 200 ms debounce, five results, "more matches" notice, error with retry, keyboard support.
- The cart and the customer survive leaving the screen and reloading, using only identifiers and quantities in `sessionStorage`. The sale is rebuilt from the services, and a sent but unsettled attempt is shown read-only and retried only with its original idempotency key.
- Commercial number, current customer data and issuer data in the history, detail, PDF and XML (schema 1.2).
- Split the 994-line PR into 4 stacked PRs plus a replacement for #62 to meet the 400-line policy (392 / 174 / 190 / 256 / 115 added lines excluding tests). The final tree is identical to the replaced PRs (`git diff` empty). No force-push.

**This week - Verification:**

- Real-browser acceptance with Playwright against a disposable composition built from exact commits (Host, Gateway, Auth, Customers, Products, Sales, Workflow, PostgreSQL, MongoDB): 14/14 scenarios on the final revisions. It found two CSS defects that unit tests could not see (a hidden suggestion list that stayed visible, and a squeezed customer column); both are fixed with tests.
- Investigated an intermittent test in synkro-products-portal: it fails on untouched `develop` too (5 of 5 runs), so it is pre-existing and was not hidden.
- GitHub Actions ended "not started: account locked due to a billing issue" on these PRs. I report them as not executed, never as passed.

## 3. Blockers and risks

- No CI has run on the HU-VEN-20 PRs and none has a human review yet.
- The customer-search contract is a proposal from Customers; my verification shows technical compatibility, not an approval. No latency target is agreed.
- The "no backfill" decision has not been checked against the real target database.
- One build supports one Host origin: `localhost` and `127.0.0.1` need different builds.
- Commercial numbers can have gaps (the counter and the insert are not one transaction).
- Stacked PRs must be integrated strictly in order.
- The RS256 signature check in synkro-products-api (issue #271) is still not implemented.

## 4. Plan for next week

- Get the PRs reviewed and integrated in order: sales-db #18 → sales-api #35 → Sales Portal #63 → #65 → #64 → #66 → #67, and Products Portal #33.
- Refresh the pinned `DB_REF` of the Sales API launcher after #18 and rebuild the portal images.
- Run the target-environment inventory of the Sales database.
- Ask Jordan to confirm the search contract on #304.

These are proposals, not completed work.

## 5. Compliance self-check

- [x] Conventional Commits - `type(scope): summary` — e.g. `fix(sales): answer an exhausted yearly sequence as a 422 business rule violation`, `fix(infra-mongo): align the sales database release reference`.
- [x] Per-environment HU branch + PR to that environment — infra-mongo fix re-applied to develop, qa and main; HU-VEN-20 `feat/...` branches with PRs to `develop`, nothing promoted.
- [x] Testable acceptance criteria
- [x] Tests added/updated (unit / integration)
- [x] DDD / hexagonal boundaries respected (domain has no I/O) — the number rules are in the domain; the counter is in the persistence adapter; the HTTP error mapping is in the input adapter.
- [x] No secrets; config via environment variables — the acceptance environment generates disposable credentials at run time.

## 6. Evidence links

- Last week: [synkro-docs PR #270](https://github.com/code-corhuila/synkro-docs/pull/270), issues [#271](https://github.com/code-corhuila/synkro-docs/issues/271), [#272](https://github.com/code-corhuila/synkro-docs/issues/272), [#273](https://github.com/code-corhuila/synkro-docs/issues/273); [infra-mongo PR #13](https://github.com/code-corhuila/synkro-infra-mongo/pull/13), [#14](https://github.com/code-corhuila/synkro-infra-mongo/pull/14), [#15](https://github.com/code-corhuila/synkro-infra-mongo/pull/15)
- This week, pull requests: [sales-db #18](https://github.com/code-corhuila/synkro-sales-db/pull/18), [sales-api #35](https://github.com/code-corhuila/synkro-sales-api/pull/35), [sales-portal #63](https://github.com/code-corhuila/synkro-sales-portal/pull/63), [#65](https://github.com/code-corhuila/synkro-sales-portal/pull/65), [#64](https://github.com/code-corhuila/synkro-sales-portal/pull/64), [#66](https://github.com/code-corhuila/synkro-sales-portal/pull/66), [#67](https://github.com/code-corhuila/synkro-sales-portal/pull/67), [products-portal #33](https://github.com/code-corhuila/synkro-products-portal/pull/33)
- This week, issues: [HU-VEN-20 #305](https://github.com/code-corhuila/synkro-docs/issues/305), [HU-CLI-05 #304](https://github.com/code-corhuila/synkro-docs/issues/304)
- Key commits: [14fd0b0 (422 on exhausted year)](https://github.com/code-corhuila/synkro-sales-api/commit/14fd0b07771379d70aa4d72d9f41b2f9a4569e73), [f6724b9 (final Sales Portal head)](https://github.com/code-corhuila/synkro-sales-portal/commit/f6724b9a66ca00a962b798b1e6c74311b9907bc3)
