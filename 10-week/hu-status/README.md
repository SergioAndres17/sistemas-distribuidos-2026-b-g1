<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       10-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 10

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Sergio Andres Ordoñez Diaz
- GITHUB_USER: SergioAndres17
- TEAM: Group 10 - synkro-tech
- SPRINT_GOAL: Land the full Products and Auth PostgreSQL schemas end to end (tables, constraints, roles, audit), open the Sales MongoDB skeleton with its collection validator and roles, and harden the Liquibase migration runner so no credential or rebuild-check failure stays silent.
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
| HU-PRO-03  | Base structure of `synkro-products-api` (hexagonal Go + `/health` + default middleware) | done | https://github.com/code-corhuila/synkro-docs/issues/181 |
| HU-VEN-03  | Base structure of `synkro-sales-api` (hexagonal Go + `/health` + default middleware) | done | https://github.com/code-corhuila/synkro-docs/issues/186 |
| HU-PRO-04  | `synkro-products-db` Flyway skeleton, `products_writer` role, CI rebuild check (migrate → rollback → migrate), no tables yet | done | https://github.com/code-corhuila/synkro-docs/issues/191 |
| HU-PRO-05  | `category` and `product` tables (in that order, for the FK) | done | https://github.com/code-corhuila/synkro-docs/issues/192 |
| HU-PRO-06  | `stock_adjustment`, `stock_reservation`, `stock_reservation_line` tables | done | https://github.com/code-corhuila/synkro-docs/issues/193 |
| HU-PRO-07  | `stock_alert` table | done | https://github.com/code-corhuila/synkro-docs/issues/194 |
| HU-AUTH-04 | `system_user` table and the `auth_reader` role, with email-uniqueness and role-value checks | done | https://github.com/code-corhuila/synkro-docs/issues/208 |
| HU-AUTH-05 | `refresh_token` table, FK to `system_user`, its index | done | https://github.com/code-corhuila/synkro-docs/issues/209 |
| HU-AUTH-06 | `service_token` and `idempotency_key` tables, with the service-token FK and index | done | https://github.com/code-corhuila/synkro-docs/issues/210 |
| HU-AUTH-12 | User-creation audit: `auth_schema.user_creation_audit`, writer/reader grants, UPDATE/DELETE/TRUNCATE denied to `auth_app`, real privilege tests | done | branch: not specified — confirm before upload |
| HU-VEN-04  | Sales Liquibase skeleton: changelog-master, pinned extension+driver image, executor-only compose | done | branch: not specified — confirm before upload |
| HU-VEN-05  | `sale` collection with a strict validator, 4 indexes, `sales_reader`/`sales_writer` roles assigned to `sales_app` | done | branch: not specified — confirm before upload |
| HU-VEN-16  | Harden Liquibase preconditions, stop silently-swallowed migration errors, fix failure propagation in the MongoDB rebuild-check tests | done | branch: not specified — confirm before upload |
| HU-VEN-16  | Fix the MongoDB migration runner to stop leaking credentials via process args, logs, stdout/stderr, while keeping Liquibase, preflight/postflight and existing migrations compatible | done | branch: not specified — confirm before upload |

> Several rows have no PR link in the source table — replace the `branch:` placeholders with the real PR URL before upload.

## 2. My individual contribution

**Go base structures (HU-PRO-03, HU-VEN-03):**
- Stood up `synkro-products-api` and `synkro-sales-api` with the agreed hexagonal Go layout, a `/health` endpoint and the default middleware chain, matching the structure used by HU-CLI-01/AUTH-01/WKF-01's Java equivalents.

**Products PostgreSQL schema (HU-PRO-04 through HU-PRO-07):**
- Built the schema from the ground up: the Flyway skeleton and `products_writer` role first, with a CI check that migrates, rolls back and migrates again before any table exists; then `category` and `product` in FK order, the stock-adjustment/reservation tables, and finally `stock_alert` — each migration shipped independently and reviewable on its own.

**Auth PostgreSQL schema (HU-AUTH-04, HU-AUTH-05, HU-AUTH-06, HU-AUTH-12):**
- HU-AUTH-04: `system_user` with a unique-email constraint, a role check restricted to `ADMIN`/`SALESPERSON`/`INVENTORY` (never `SERVICE`), and length checks on name and email, with CI tests proving an invalid role and a duplicate email are both rejected.
- HU-AUTH-05: `refresh_token`, storing only a hash of the token (unique), with an `ON DELETE RESTRICT` foreign key to `system_user` added in a later `04_alter` migration, plus its index.
- HU-AUTH-06: `service_token` (restricted to `WORKFLOW`/`WORKER`, with an expiry check and an indexed `issued_by` FK) and the polymorphic `idempotency_key` table (`resource_type` plus an un-keyed `resource_id`, since it spans more than one table).
- HU-AUTH-12: added `user_creation_audit` with the accepted design's fields, constraints, keys and indexes; granted SELECT/INSERT to the writer and SELECT to the reader; explicitly denied UPDATE/DELETE/TRUNCATE and structural changes to `auth_app`; fixed permissions the table had inherited incorrectly; and tested the privilege boundaries against a real database rather than asserting on the migration file alone.

**Sales MongoDB foundations and runner hardening (HU-VEN-04, HU-VEN-05, HU-VEN-16 ×2):**
- HU-VEN-04: set up the Liquibase skeleton for `synkro-sales-db` — changelog-master, an image with the Mongo extension and driver versions pinned, and a compose file that runs only the migration executor, with no standalone Mongo service of its own.
- HU-VEN-05: created the `sale` collection with a strict schema validator, four indexes, and the `sales_reader`/`sales_writer` roles assigned to the `sales_app` connection user.
- HU-VEN-16 (first pass): tightened Liquibase preconditions so a missing precondition no longer passes silently, and fixed the rebuild-check tests so a MongoDB migration failure actually fails the test instead of being swallowed.
- HU-VEN-16 (second pass): rewrote the migration runner so credentials never appear in process arguments, logs, stdout or stderr, while keeping it compatible with Liquibase, the preflight/postflight checks, the existing security validations and every migration already applied.

## 3. Blockers and risks

- **HU-AUTH-12's audit table is a hard dependency for HU-AUTH-13/14** (Jordan) — any further permission fix on `user_creation_audit` has to be re-verified against both use cases before they can rely on it.
- **The Sales MongoDB rebuild-check (HU-VEN-16) only became trustworthy after the second hardening pass** — before that, a silently swallowed failure could have hidden a broken migration; worth double-checking no earlier sprint's green CI run was a false positive.
- **Three of this week's HUs (HU-AUTH-12, HU-VEN-04, HU-VEN-05, both HU-VEN-16 entries) have no recorded PR link on the sprint board** — needs confirming before this file is uploaded.
- **Products and Sales schemas are now both fully migrated**, so any further column or constraint change from here on is an `_alter` migration, never an edit to an already-merged one.

## 4. Plan for next week

- Supply the missing PR links for HU-AUTH-12, HU-VEN-04, HU-VEN-05 and the two HU-VEN-16 entries.
- Pair with Jordan to confirm the Sales API's integration tests pass cleanly against the hardened migration runner.
- Start the Customers PostgreSQL schema skeleton once HU-CLI-01's base structure (Angel) is confirmed stable.
- Review Santiago's HU-AUTH-07/08 JDBC adapters against the `system_user`/`refresh_token` schema for any column-name mismatch.

## 5. Compliance self-check

- [x] Conventional Commits - `type(scope): summary`
- [x] Per-environment HU branch + PR to `main`, approved by `ariel5253`
- [x] Testable acceptance criteria
- [x] Tests added/updated — CI rebuild checks (migrate → rollback → migrate) on every `-db` HU, role-rejection and duplicate-email tests (HU-AUTH-04), real privilege tests (HU-AUTH-12)
- [x] DDD / hexagonal boundaries respected — migrations live only in the `-db` repositories, never in `-api`; `-api` base structures (HU-PRO-03, HU-VEN-03) keep domain free of framework imports
- [x] No secrets; config via environment variables — HU-VEN-16's runner fix removes the last place credentials could leak into logs or process args

## 6. Evidence links

- Products API base structure: `synkro-products-api`
- Sales API base structure: `synkro-sales-api`
- Products schema migrations: `synkro-products-db`
- Auth schema migrations: `synkro-auth-db`
- Sales MongoDB skeleton and migration runner: `synkro-sales-db`
