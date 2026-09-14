<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       06-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 06

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Sergio Andres Ordoñez Diaz
- GITHUB_USER: SergioAndres17
- TEAM: Group - synkro-tech
- SPRINT_GOAL: Close the architecture documentation gap in `05-architecture/` by delivering HU-04 (ADR-001 publication, architectural overview, distributed-pattern evaluation, deployment topology, security threat model, and ADR-002 for sale-authorship traceability), and begin HU-05 (unify FR/NFR requirement identifiers and disconnect the repository from the external PDR).
<!-- CONFIG-END -->

## Docs Repository

| Board Name          | URL                                              |
| -------------------- | ------------------------------------------------ |
| synkro-docs Repository | https://github.com/code-corhuila/synkro-docs.git |

## Team Members

| Full Name                          | GitHub User                                                 |
| ----------------------------       | ------------------------------------------                  |
| Angel Gustavo Solano Trujillo      | https://github.com/AsolanoT                                 |
| Fredman Santiago Plazas Artunduaga | https://github.com/SantiagoPlazas2005                       |
| Jordan Ramirez Gallego             | https://github.com/JordanRG420                              |

## 1. User stories worked this week

| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-ARQ-10 | Write the architectural overview and cross-cutting concerns | done | `05-architecture/overview.md`, `05-architecture/cross-cutting.md` |
| HU-ARQ-11 | Evaluate distributed patterns against the MVP scope | done | `05-architecture/pattern-guide.md` |
| HU-ARQ-13 | Write ADR-002 — sale authorship traceability (joint with @AsolanoT) | done | `05-architecture/decisions/records/ADR-002-sale-authorship-traceability.md` |

## 2. My individual contribution

**Context:** my part of HU-04 was rebuilding the technical core of
`05-architecture/`, which was still the professor's unfilled template —
API Gateway, Kafka/RabbitMQ, and `Database per Service` principles that
directly contradict ADR-001. I rewrote it from what the team actually
decided, then evaluated the distributed patterns the course requires,
and closed the sprint co-authoring ADR-002 with Angel.

**HU-ARQ-10 — Architectural overview and cross-cutting concerns:**
- Rewrote `overview.md` end to end from ADR-001: C4 Level 1 (system
  context: ADMIN/SALESPERSON/INVENTORY as actors) and Level 2
  (containers: 4 services, 4 SPAs, 1 PostgreSQL instance) as Mermaid
  diagrams, both traceable to ADR-001 or `service-catalog.md`.
- Replaced the template's 4 generic principles with 5 specific to this
  project (one bounded context per service, schema isolation instead of
  database-per-service, no API Gateway, contract-first, soft deletion
  only), and added a section explicitly listing what the architecture
  does NOT have and why — so nobody assumes an API Gateway or Kafka
  exists by omission.
- Wrote `cross-cutting.md` from scratch: standard error format, JSON
  logging shared between Java and Go, `X-Trace-Id` correlation
  propagation without a distributed tracing system, `/health` contract,
  and the RS256 public-key distribution mechanism (env var, with JWKS
  recorded as a future candidate).

**HU-ARQ-11 — Distributed pattern evaluation:**
- Evaluated Circuit Breaker, Saga, Outbox, and CQRS against the actual
  MVP flow, not generically: Circuit Breaker adopted (simplified) for
  sales→customers/products calls; Saga, Outbox, and CQRS rejected with a
  specific technical reason each (a 2-step flow doesn't need Saga, no
  broker/events exist for Outbox, read and write models are identical for
  CQRS).
- Defined concrete timeout/retry values (3s reads, 5s writes, 1 retry on
  GET only, zero retries on the stock-deduction `PATCH` because it is not
  idempotent).
- Resolved the stock-concurrency question open since the PDR: a
  conditional `UPDATE ... WHERE stock >= :quantity` backed by the
  existing `CHECK (stock >= 0)` constraint, with the alternatives
  (optimistic locking, `SELECT FOR UPDATE`, Redis lock) documented and
  rejected.
- Documented the compensation path when stock deduction fails after a
  sale is created, including the partial-failure case across multiple
  line items — the piece HU-ARQ-13 later needed for context.

**HU-ARQ-13 — ADR-002 (joint with Angel):**
- Co-evaluated the two alternatives for sale-authorship traceability and
  helped adopt the `created_by` column over a full audit table, keeping
  the change scoped to what the MVP actually needs.
- Cross-checked that ADR-002's decision doesn't conflict with the CQRS
  rejection already written in `pattern-guide.md` §4 — both documents
  now agree that `sales_summary` is unpopulated by design, not by
  oversight.

## 3. Blockers and risks

- `overview.md` and `pattern-guide.md` both assume the 4 services run as
  separate processes; neither has been validated against a real
  docker-compose yet outside of what Angel documented in `deployment.md`
  — first integration test will be the real proof.
- The Circuit Breaker adoption names `sony/gobreaker` for Go but no Java
  equivalent decision was needed this week, since sales-service (the only
  service making outbound calls) is Go. If a future HU adds outbound
  calls from a Java service, Resilience4j is the candidate already noted
  in `pattern-guide.md`.

## 4. Plan for next week

- Start HU-05 jointly with Jordan and Santiago: help unify `RF/RNF` into
  `FR/NFR` and verify the pattern-guide's references stay consistent once
  `04-requirements/functional.md` exists.
- Review the whole team's understanding of the pattern decisions in the
  Weekly, since HU-ARQ-11 is the document most likely to get direct
  questions from the professor about resilience patterns.

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [x] Per-environment HU branch + PR to that environment — N/A: direct commit to `main` for `docs` per `documentation-rules.md` (no branches in this repo)
- [x] Testable acceptance criteria
- [x] Tests added/updated (unit / integration) — N/A, documentation-only HU
- [x] DDD / hexagonal boundaries respected (domain has no I/O) — N/A, no code touched this week
- [x] No secrets; config via environment variables — cross-cutting.md documents `JWT_PUBLIC_KEY` as an env var only, no real key committed

## 6. Evidence links
- Architectural overview: [`overview.md`](./05-architecture/overview.md)
- Cross-cutting concerns: [`cross-cutting.md`](./05-architecture/cross-cutting.md)
- Distributed pattern evaluation: [`pattern-guide.md`](./05-architecture/pattern-guide.md)
- ADR-002: [`ADR-002-sale-authorship-traceability.md`](./05-architecture/decisions/records/ADR-002-sale-authorship-traceability.md)

