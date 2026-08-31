<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       04-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 04

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Sergio Andres Ordoñez Diaz
- GITHUB_USER: SergioAndres17
- TEAM: Group - synkro-tech
- SPRINT_GOAL: Formalize the domain-to-repository service catalog, fill the product-definition gap (problem framing + vision), and build a panoramic MVP monolith (Spring Boot + React) to validate business understanding, as requested by the instructor for Week 4.
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
| HU-ARQ-02 | MVP Monolith — Functional Backend | done | httd |

## 2. My individual contribution

**Context:** the instructor asked for 3 explicit deliverables this week: the domain catalog, continuing the `docs` repo, and a panoramic MVP mockup on any infrastructure (single native repo, monolith) so the business flow can be validated visually before the real distributed architecture is built. My HU covers the backend half of the MVP.

- I built a functional Spring Boot monolith exposing the core business flows: customer management, product/stock catalog, and sale registration with stock deduction — in a new, temporary repo (`mvp-demo`, `/backend`), explicitly separate from the 4 real backend repos from HU-ARQ-01.
- I enforced the same business invariants already documented in `entities-and-rules.md`, even in this throwaway prototype: `price > 0` and `stock` never negative for Product, `Sale` only accepted for an active customer, stock deducted on confirmation with rejection if requested quantity exceeds available stock, and `total` calculated as the sum of frozen `unitPrice * quantity` per line (the unit price does not change retroactively if the product's price changes later).
- I organized the code internally into separate packages/modules per domain (Auth/Customers/Products/Sales) to ease a future split, but deliberately did **not** implement full hexagonal ports-and-adapters here — that belongs to the real ADR-001 implementation, not to this spike.
- I used an embedded H2 database and a simple mocked role selector instead of real JWT/RS256 — Circuit Breaker, Saga, Outbox, and CQRS were explicitly kept out of scope, since those are ADR-001's target-architecture patterns, not part of this demo.
- I converted this HU into the Gherkin-scenario format required by `04-requirements/user-stories.md` and added it there.

## 3. Blockers and risks
- Since this monolith intentionally skips hexagonal architecture and the real single-logical-database setup, there's a risk that someone unfamiliar with the ADR-001 context might mistake this prototype for the real architecture — the README and HU both need to keep that distinction explicit.
- H2 in-memory data resets on restart; if the instructor expects persisted state across a live demo session, this could be a surprise — worth clarifying before the actual presentation.

## 4. Plan for next week
- Support HU-FE-01 (Jordan) if the frontend needs any backend adjustment to connect to the real API instead of its in-memory mock (currently decoupled by design).
- Start configuring the real single PostgreSQL instance with the 4 schemas and dedicated users, now that the domain catalog (HU-ARQ-01) is formalized — this was already planned from last week's blockers.

## 5. Compliance self-check
- [ ] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [x] Testable acceptance criteria
- [x] Tests added/updated (unit / integration)
- [ ] DDD / hexagonal boundaries respected (domain has no I/O) — intentionally out of scope for this throwaway MVP spike
- [x] No secrets; config via environment variables

## 6. Evidence links
- Backend monolith repo: [`mvp-demo/backend`](https://github.com/AsolanoT/mvp-demo/tree/main/backend)
- User stories backlog (HU-ARQ-02 entry): [`user-stories.md`](./docs/04-requirements/user-stories.md)
