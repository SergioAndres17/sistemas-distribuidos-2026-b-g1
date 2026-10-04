# Risk Register

> Unmitigated risks become problems. This register allows the team to
> anticipate, not just react.
> Review and update at each retrospective or when there is a significant project change.

---

## Probability × impact matrix

```
IMPACT
  │
  │ HIGH  │  [Mitigate]    │  [Avoid]       │
  │       │  Low           │  High          │
  │       │  Probability   │  Probability   │
  │───────────────────────────────────────
  │ MEDIUM│  [Accept]      │  [Mitigate]    │
  │       │  with monitoring│              │
  │───────────────────────────────────────
  │ LOW   │  [Accept]      │  [Accept]      │
  │       │                │               │
  └───────────────────────────────────────
              LOW              HIGH
                      PROBABILITY
```

**Response strategies:**
- **Avoid:** Change the plan so the risk cannot materialize
- **Mitigate:** Reduce the probability or impact
- **Transfer:** Pass the risk to another party (insurance, provider, contract)
- **Accept:** Acknowledge the risk and have a contingency plan

---

## Active risk register

### R-001 — Shared PostgreSQL instance is a single point of failure

| Field | Value |
|-------|-------|
| **ID** | R-001 |
| **Category** | Technical |
| **Description** | All five schemas (auth, customers, products, sales, workflow) share one PostgreSQL instance per environment (ADR-009). If that instance goes down, every domain and the saga become unavailable simultaneously |
| **Probability** | Low |
| **Impact** | High |
| **Risk level** | High |
| **Strategy** | Accept |
| **Mitigation plan** | Per-service connection-pool sizing to prevent one service from saturating the instance; `docker compose` health check restarts the container on failure; operational monitoring through Prometheus and Grafana (deployment.md §8) |
| **Contingency plan** | Restart the instance; if the volume is corrupted, restore from the last backup. All sagas in RUNNING state resume automatically after restart (ADR-007 Decision 1) |
| **Trigger** | Any service's health check (`GET /health`) reports `database: down` |
| **Owner** | Angel Gustavo Solano Trujillo (Tech Lead) |
| **Review date** | 2026-10-05 |
| **Status** | Active |

**References:** ADR-009 "Accepted cost", `overview.md` AT-002 (reopened), `security-threat-model.md` D-3.

---

### R-002 — GRANT misconfiguration allowing cross-schema access

| Field | Value |
|-------|-------|
| **ID** | R-002 |
| **Category** | Technical |
| **Description** | With schema-based isolation (ADR-009), an incorrect GRANT in a `-db` migration could give a service access to another domain's schema. Unlike separate instances, the engine does not prevent the connection — only the permissions do |
| **Probability** | Low |
| **Impact** | High |
| **Risk level** | High |
| **Strategy** | Mitigate |
| **Mitigation plan** | Each `-db`'s CI rebuild check (drop, rebuild from V001, verify) confirms that the domain's `_writer` role has access only to its own schema. The `03_dcl/` migration structure separates role creation from grants, making the scope visible in review |
| **Contingency plan** | If discovered: revoke the incorrect GRANT immediately, add a regression test to CI, and audit the affected schema for unauthorized changes |
| **Trigger** | CI rebuild check fails with a permission error on a table outside the domain's schema |
| **Owner** | Angel Gustavo Solano Trujillo (Tech Lead) |
| **Review date** | 2026-10-05 |
| **Status** | Active |

**References:** ADR-009 "Accepted cost", ADR-005 Decision 3, `security-threat-model.md` T-3.

---

### R-003 — U-script discipline: rollback scripts must be maintained manually

| Field | Value |
|-------|-------|
| **ID** | R-003 |
| **Category** | Technical |
| **Description** | Every V migration must have its U rollback under `05_rollbacks/`. Flyway never runs U scripts automatically — they exist only for manual emergency rollback. If a U script is missing or incorrect, a rollback in production leaves the schema in an inconsistent state |
| **Probability** | Medium |
| **Impact** | Medium |
| **Risk level** | Medium |
| **Strategy** | Mitigate |
| **Mitigation plan** | The CI rebuild check runs the full rollback sequence (U scripts in reverse order) after rebuilding, then rebuilds again. A missing or broken U script fails the check. PR review verifies that every V has its U |
| **Contingency plan** | If a U script is missing at rollback time: write it manually, test it against a copy of the environment, then apply |
| **Trigger** | A V migration is merged without its corresponding U script |
| **Owner** | Jordan Ramirez Gallego |
| **Review date** | 2026-10-05 |
| **Status** | Active |

**References:** ADR-005 Decision 2, `models.md` principle 5.

---

### R-004 — Service tokens rotated manually every 30 days

| Field | Value |
|-------|-------|
| **ID** | R-004 |
| **Category** | Technical |
| **Description** | `synkro-workflow` and `synkro-worker` use service tokens that expire after 30 days. Rotation requires an ADMIN to issue new tokens through `synkro-auth-api` and update the environment secrets. If the token expires unnoticed, the saga and the worker stop working |
| **Probability** | Medium |
| **Impact** | High |
| **Risk level** | High |
| **Strategy** | Mitigate |
| **Mitigation plan** | A calendar reminder is set 7 days before expiry. The worker logs include the token's expiry date on startup. Post-MVP, a JWKS endpoint (cross-cutting.md §5) would automate rotation |
| **Contingency plan** | Issue a new token immediately; restart the affected service with the new secret |
| **Trigger** | The workflow or worker starts returning 401 on internal calls |
| **Owner** | Sergio Andrés Ordóñez Díaz |
| **Review date** | 2026-10-05 |
| **Status** | Active |

**References:** ADR-006, `overview.md` AT-005, `security-policy.md` "Secret Rotation".

---

### R-005 — Connection pool saturation in the shared instance

| Field | Value |
|-------|-------|
| **ID** | R-005 |
| **Category** | Technical |
| **Description** | Five services plus the worker share one PostgreSQL instance. If one service opens too many connections (a slow report, a burst of saga steps), the others may be unable to get a connection, causing cascading 503s |
| **Probability** | Low |
| **Impact** | Medium |
| **Risk level** | Medium |
| **Strategy** | Mitigate |
| **Mitigation plan** | Each service declares an explicit connection pool size (10 by default, cross-cutting.md §8) and a 5-second wait timeout. Reports use indexed aggregation queries with a statement timeout of 5 seconds. Total connections across all services stay well below PostgreSQL's `max_connections` |
| **Contingency plan** | If saturation occurs: identify the service consuming connections (Prometheus metrics), reduce its pool size or kill the slow query, and restart the affected service |
| **Trigger** | Connection wait time exceeds 5 seconds in any service's metrics |
| **Owner** | Fredman Santiago Plazas Artunduaga |
| **Review date** | 2026-10-05 |
| **Status** | Active |

**References:** ADR-009 "What must be watched", `security-threat-model.md` D-3, `cross-cutting.md` §8.

---

### R-006 — No message broker: eventual consistency only through the saga

| Field | Value |
|-------|-------|
| **ID** | R-006 |
| **Category** | Technical |
| **Description** | ADR-007 Decision 5 deferred RabbitMQ and the outbox pattern out of the MVP. Consistency between domains is handled exclusively by the saga. If a future requirement needs other contexts to react to a sale (e.g. email notifications, analytics), there is no event bus to carry that information |
| **Probability** | Low |
| **Impact** | Low |
| **Risk level** | Low |
| **Strategy** | Accept |
| **Mitigation plan** | None needed for the MVP — no requirement consumes an event. When a consumer appears, a new ADR adopts the broker and applies the outbox pattern to the publishing service (recorded in tech-backlog.md TD-002) |
| **Contingency plan** | Not applicable — this is a scope decision, not a failure scenario |
| **Trigger** | A requirement appears that needs a context to react to a business event from another context |
| **Owner** | Angel Gustavo Solano Trujillo (Tech Lead) |
| **Review date** | 2026-10-05 |
| **Status** | Active |

**References:** ADR-007 Decision 5, `pattern-guide.md` "Outbox Pattern: Deferred".

---

## Closed risks / Lessons learned

| ID | Risk | Outcome | Lesson |
|----|------|---------|--------|
| — | No closed risks yet | — | — |

---

## Correlations

- Architectural technical debt → `05-architecture/overview.md` §8 (AT-001 to AT-005)
- Technical backlog (deferred items) → `15-project-control/tech-backlog.md`
- Security threats (STRIDE) → `05-architecture/security-threat-model.md`
- Architecture decisions that originated these risks → ADR-005, ADR-006, ADR-007, ADR-009
