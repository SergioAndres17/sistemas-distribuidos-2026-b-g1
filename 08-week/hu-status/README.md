<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       08-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 08

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Sergio Andres Ordoñez Diaz
- GITHUB_USER: SergioAndres17
- TEAM: Group 10 - synkro-tech
- SPRINT_GOAL: Close HU-08 (professor review feedback) and HU-09 (first version of the API contracts), and replace the architecture decisions that no longer held — shared database instance, gateway-only token validation, stateless saga, broker without a real consumer, undecided cross-cutting stack — through ADR-005 to ADR-008, rewriting every dependent document (HU-10).
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
| Angel Gustavo Solano Trujillo      |  https://github.com/AsolanoT              |

## 1. User stories worked this week

| HU ID      | Title                                                        | Status | Evidence (PR or commit URL)                                                                       |
|------------|---------------------------------------------------------------|--------|---------------------------------------------------------------------------------------------------|
| HU-DOCS-41 | Pre-ADR-003 JWT and CORS model in `security-policy.md` and `cross-cutting.md` (HU-08, with Santiago) | done   | https://github.com/code-corhuila/synkro-docs/pull/56 |
| HU-DOCS-40 | OpenAPI contract: `synkro-workflow` (HU-09)                   | done   | https://github.com/code-corhuila/synkro-docs/pull/50 |
| HU-ARQ-20  | ADR-007: persistent saga execution and scheduled work (HU-10) | done   | `05-architecture/decisions/records/ADR-007-persistent-saga-and-scheduled-work.md` |
| HU-DOCS-43 | Domain model: entities, domain events and glossary (HU-10)    | done   | `02-domain/entities-and-rules.md`, `02-domain/domain-events.md`, `01-context/glossary.md` |
| HU-DOCS-49 | Security threat model (HU-10)                                 | done   | `05-architecture/security-threat-model.md` |
| HU-DOCS-51 | Architecture overview (HU-10)                                 | done   | `05-architecture/overview.md` |
| HU-DOCS-52 | C4 container diagram in Mermaid and draw.io (HU-10)           | done   | `05-architecture/overview.md` §3, `08-uml/diagrams/source/c4-02-containers.drawio` |

## 2. My individual contribution

**HU-DOCS-41 — pre-ADR-003 security model (HU-08, with Santiago):**
- Aligned `security-policy.md` and `cross-cutting.md` with the gateway model of ADR-003, which at the time was the accepted decision. HU-10 later superseded it with per-service validation (ADR-006), as recorded in the HU-08 closing comment.

**HU-DOCS-40 — workflow contract (HU-09):**
- Wrote the first OpenAPI contract for `synkro-workflow`: the sale-registration entry point and the saga's outcomes.

**HU-ARQ-20 — ADR-007: persistent saga execution and scheduled work (HU-10):**
- Authored ADR-007 with 6 decisions, each with options, dominant criterion and accepted cost:
  1. Saga state persisted after every step in `workflow-db`, the workflow's own instance.
  2. The sale as a saga resource with idempotent steps (`Idempotency-Key: <sagaId>:<step>`).
  3. Stock reservation as a resource of the products service.
  4. `SaleFailed` removed: the saga record holds the failure.
  5. RabbitMQ, the outbox and `SaleCompleted` deferred until an event has a consumer a requirement needs.
  6. A scheduled low-stock alert job for the worker.
- Answers the PR #56 finding about the workflow's health-check dependencies.

**HU-DOCS-43 — domain model (HU-10):**
- Updated `entities-and-rules.md`:
  - Money in minor units.
  - `identityDocument` unique across all customers.
  - Stock changed only through reservations and adjustments.
  - New stock adjustment, stock reservation and stock alert entities.
- Updated the glossary with the new concepts, and `domain-events.md`: the MVP publishes no events, and `SaleCompleted` remains only as a future candidate.

**HU-DOCS-49 — security threat model (HU-10):**
- Rewrote the STRIDE analysis for the gateway, per-service validation and the saga, adding 6 threats with their mitigations: forged identity headers, leaked service token, algorithm confusion, replayed creations, saga-store tampering, and a salesperson calling reservations directly.
- Result: 25 threats, 24 designed, 1 accepted.

**HU-DOCS-51 and HU-DOCS-52 — architecture overview and C4 diagram (HU-10):**
- Updated `overview.md`: principles, service catalog, pattern table, synchronous-only communication, and the technical debt (AT-001 to AT-003 closed).
- Updated the C4 Level 2 diagram in both Mermaid and draw.io in the same PR: one database per domain, the saga store, the published ports, and dashed arrows for internal calls with service tokens.

## 3. Blockers and risks

- **HU-DOCS-43 exceeded the 400-line limit (~445 lines).** Kept as one PR by team decision, because its three files describe one indivisible domain change; documented as a reasoned exception in the HU-10 closing comment.
- **Two splits to stay within 400 lines.** `pattern-guide.md` moved from HU-DOCS-51 to HU-DOCS-72, and the sale-registration BPMN moved from HU-DOCS-52 to HU-DOCS-73.
- **Manual SVG exports.** The draw.io diagrams must be exported to SVG by hand in the same commit, or the Mermaid and draw.io versions drift apart.

## 4. Plan for next week

- HU-10 (carried forward): HU-DOCS-72 (`pattern-guide.md`) and HU-DOCS-73 (sale-registration BPMN diagram).
- HU-11 — HU-DOCS-60: align the `synkro-workflow` contract with the persisted saga (`POST /api/v1/sagas/register-sale`, `GET /api/v1/sagas/{id}`).
- HU-12:
  - HU-DOCS-67: testing strategy for Java and Go, with coverage thresholds.
  - HU-DOCS-69: diagram index and UX flows aligned with the saga and the stock alerts.

## 5. Compliance self-check

- [x] Conventional Commits - `type(scope): summary`
- [x] Per-environment HU branch + PR to `main` — branches `docs/fix-pre-adr003-jwt-and-cors-model`, `docs/add-workflow-openapi-contract`, `docs/add-adr-007-persistent-saga-and-scheduled-work`, `docs/update-domain-model-for-reservations-and-alerts`, `docs/update-threat-model-for-per-service-validation`, `docs/update-architecture-overview` and `docs/update-c4-containers-diagram`, merged via PR approved by `ariel5253`
- [x] Testable acceptance criteria
- [x] Tests added/updated — N/A, documentation-only HU
- [x] DDD / hexagonal boundaries respected — N/A, no code touched this week; the domain model was updated before any contract used the new concepts
- [x] No secrets; config via environment variables — N/A, no configuration touched

## 6. Evidence links

- ADR-007 (HU-ARQ-20): [`ADR-007-persistent-saga-and-scheduled-work.md`](./docs/ADR-007-persistent-saga-and-scheduled-work.md)
- Entities and rules (HU-DOCS-43): [`entities-and-rules.md`](./docs/entities-and-rules.md)
- Domain events (HU-DOCS-43): [`domain-events.md`](./docs/domain-events.md)
- Glossary (HU-DOCS-43): [`glossary.md`](./docs/glossary.md)
- Threat model (HU-DOCS-49): [`security-threat-model.md`](./docs/security-threat-model.md)
- Architecture overview (HU-DOCS-51, HU-DOCS-52): [`overview.md`](./docs/overview.md)
- C4 containers in draw.io (HU-DOCS-52): [`c4-02-containers.drawio`](./docs/c4-02-containers.drawio)
