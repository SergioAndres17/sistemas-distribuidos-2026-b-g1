<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       03-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 03

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Sergio Andres Ordoñez Diaz
- GITHUB_USER: SergioAndres17
- TEAM: Group - synkro-tech
- SPRINT_GOAL: Correct the PDR and ADR-001 to align with the actual course requirements (business-only PDR, single logical database, explicit context map), then populate the docs repository's 00-governance, 01-context, and 02-domain sections with SynkroTech-specific content.
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
| HU-PDR-07 | Correct section 02 - Current processes / Expected flow (including the flow diagram) in the business PDR | done | https://github.com/SergioAndres17/sistemas-distribuidos-2026-b-g1/blob/main/03-week/hu-status/01_PDR_negocio_v2.md |
| HU-ADR-05 (A) | Build the context map: identify SynkroTech's bounded contexts and justify which ones become independent services | done | https://github.com/SergioAndres17/sistemas-distribuidos-2026-b-g1/blob/main/03-week/hu-status/01_context-map_v1.md |
| HU-DOM-01 | Map the bounded contexts and their relationships (formal domain-map) | done | http |
| HU-DOCS-05 | Consolidate the business, technical, and methodology glossary | done | https: |
| HU-DOCS-06 | Define branch strategy (main/qa/dev), commits, and PR policy | done | https |
| HU-DOCS-07 | Adopt the team's Definition of Done (DoD) | done | https: |

## 2. My individual contribution

**Context for the correction:** the business PDR mixed technical architecture content into what should be a preliminary business discovery document — the PDR should only contain business context, needs, expected processes, open questions and a business glossary. In parallel, before formalizing any architecture decision, the team's decision was to first document, in business language, which parts of SynkroTech's operation are genuinely independent from each other, instead of jumping straight into a services list.

- I corrected section **02 — Procesos actuales / Flujo esperado** of the business PDR, describing the current manual sales process (stock checked by hand, sales recorded in isolated files, no consolidated reports) and the expected flow once the system is in place (login, client lookup or registration, real-time stock validation, automatic total calculation and stock deduction, traceable transaction, report generation).
- I built a flow diagram for the expected process (login → role validation → client search/registration → product selection → stock validation → sale confirmation → total calculation → stock deduction → traceable registration → report availability), matching each step described in the PDR text.
- I also documented the MVP scope boundaries in this same section (what's included vs. explicitly out of scope for this first version, such as multiple branches or payment gateway integration).
- Together with Jordan, I worked on the **context-map** document. My main contribution was analyzing the **Products and Inventory** and **Sales** bounded contexts specifically: justifying why Products needs to be independent (high query frequency for stock checks, concurrency control needs) and why Sales is the context that orchestrates the other three (Auth, Clients, Products) without any of them needing to know Sales exists.

**Continuing this week, once the ADR correction closed, I moved on to populating the actual `docs` repository scaffold provided by the instructor:**

- I upgraded the context-map draft into the formal `domain-map.md` required by the `02-domain` section: the 4 bounded contexts (Auth, Customers, Products, Sales), an ASCII relationship diagram showing Auth as the only pure upstream context, a relationship table (upstream/downstream), and a new Ubiquitous Language table clarifying, for example, that "Client" never means "system user".
- I consolidated `glossary.md`, merging the business glossary from the PDR with the technical terms from ADR-001 (microservice, bounded context, port, adapter, schema) and adding a new methodology section (ADR, Spike, Task, HU, DoR, DoD) that didn't exist as a single reference before.
- I defined `git-conventions.md`: the `main/qa/dev` branch flow (correcting an earlier gap where the `qa` branch, already required by the instructor's original rules, was missing), Conventional Commits format, PR size/review policy, and merge policy per branch tier.
- I adopted `definition-of-done.md`: the full checklist (code, tests, integration, deployment, documentation) plus the adapted version that applies specifically to Sprint 0 documentation tasks like this week's.

## 3. Blockers and risks
- The expected-flow diagram assumes stock validation happens synchronously and instantly; the open question about what happens if stock reaches zero right before confirming a sale (already listed in PDR section 03) still needs a technical answer before this flow can be implemented as-is.
- The context-map's justification for Products' independence is based on expected query frequency, not real usage data yet — this should be revisited once the system is live.
- `domain-map.md`'s bounded-context boundaries are still a design hypothesis; they haven't been stress-tested against real implementation work yet (e.g. whether Sales really only needs read-only calls to Customers/Products, or something more).

## 4. Plan for next week
- Follow up on the open question about stock concurrency (PDR section 03, question #3) so it can be resolved before backend implementation starts.
- Support completing `05-architecture` and `06-data`, since those are the next milestone before starting code implementation.
- Start drafting the base hexagonal folder template documentation for the Products service (Go), since it is the bounded context I've focused on since the context-map work.

## 5. Compliance self-check
- [ ] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [x] Testable acceptance criteria
- [ ] Tests added/updated (unit / integration)
- [ ] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

## 6. Evidence links
- Corrected business PDR: [`01_PDR_negocio_v2.md`](./01_PDR_negocio_v2.md)
- Context map (draft): [`01_context-map_v1.md`](./01_context-map_v1.md)
- Domain map (formal): [`domain-map.md`](./docs/02-domain/domain-map.md)
- Glossary: [`glossary.md`](./docs/01-context/glossary.md)
- Git conventions: [`git-conventions.md`](./docs/00-governance/git-conventions.md)
- Definition of Done: [`definition-of-done.md`](./docs/00-governance/definition-of-done.md)
