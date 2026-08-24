<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       03-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 03

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Sergio Andres Ordoñez Diaz
- GITHUB_USER: SergioAndres17
- TEAM: Group - synkro-tech
- SPRINT_GOAL: Correct the PDR and ADR-001 to align with the actual course requirements (business-only PDR, single logical database, explicit context map) before starting the docs repository.
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

## 2. My individual contribution

**Context for the correction:** the business PDR mixed technical architecture content into what should be a preliminary business discovery document — the PDR should only contain business context, needs, expected processes, open questions and a business glossary. In parallel, before formalizing any architecture decision, the team's decision was to first document, in business language, which parts of SynkroTech's operation are genuinely independent from each other, instead of jumping straight into a services list.

- I corrected section **02 — Procesos actuales / Flujo esperado** of the business PDR, describing the current manual sales process (stock checked by hand, sales recorded in isolated files, no consolidated reports) and the expected flow once the system is in place (login, client lookup or registration, real-time stock validation, automatic total calculation and stock deduction, traceable transaction, report generation).
- I built a flow diagram for the expected process (login → role validation → client search/registration → product selection → stock validation → sale confirmation → total calculation → stock deduction → traceable registration → report availability), matching each step described in the PDR text.
- I also documented the MVP scope boundaries in this same section (what's included vs. explicitly out of scope for this first version, such as multiple branches or payment gateway integration).
- Together with Jordan, I worked on the **context-map** document. My main contribution was analyzing the **Products and Inventory** and **Sales** bounded contexts specifically: justifying why Products needs to be independent (high query frequency for stock checks, concurrency control needs) and why Sales is the context that orchestrates the other three (Auth, Clients, Products) without any of them needing to know Sales exists.

## 3. Blockers and risks
- The expected-flow diagram assumes stock validation happens synchronously and instantly; the open question about what happens if stock reaches zero right before confirming a sale (already listed in PDR section 03) still needs a technical answer before this flow can be implemented as-is.
- The context-map's justification for Products' independence is based on expected query frequency, not real usage data yet — this should be revisited once the system is live.

## 4. Plan for next week
- Start drafting the base hexagonal folder template documentation for the Products service (Go), since it is the bounded context I focused on in the context map.
- Follow up on the open question about stock concurrency (PDR section 03, question #3) so it can be resolved before backend implementation starts.

## 5. Compliance self-check
- [ ] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [x] Testable acceptance criteria
- [ ] Tests added/updated (unit / integration)
- [ ] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

## 6. Evidence links
- Corrected business PDR: [`01_PDR_negocio_v2.md`](./docs/01_PDR_negocio_v2.md)
- Context map: [`01_context-map_v1.md`](./docs/01_context-map_v1.md)
