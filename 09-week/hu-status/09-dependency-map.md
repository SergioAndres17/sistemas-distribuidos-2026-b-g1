# Dependency Map

> Which service depends on which, at two levels: the calls made while the
> system is running, and the repositories that must exist before each one
> can start. A cycle in either graph would mean two components cannot both
> be the first to come up — this map exists to catch that before it
> happens.

---

## Runtime call graph

```
synkro-front
    │
    ▼
synkro-api-gateway
    │
    ├──▶ synkro-auth-api
    ├──▶ synkro-customers-api
    ├──▶ synkro-products-api
    ├──▶ synkro-sales-api
    └──▶ synkro-workflow
              │
              ├──▶ synkro-customers-api   (saga step 1: validate-customer)
              ├──▶ synkro-products-api    (saga step 2: reserve-stock / release-stock)
              └──▶ synkro-sales-api       (saga step 3: register-sale)

synkro-worker ──▶ synkro-products-api    (low-stock job)
```

| Service | Calls (outgoing) | Called by (incoming) |
|---|---|---|
| `synkro-front` | `synkro-api-gateway` | the browser |
| `synkro-api-gateway` | all 4 domain `-api`, `synkro-workflow` | `synkro-front` |
| `synkro-auth-api` | — | `synkro-api-gateway` |
| `synkro-customers-api` | — | `synkro-api-gateway`, `synkro-workflow` |
| `synkro-products-api` | — | `synkro-api-gateway`, `synkro-workflow`, `synkro-worker` |
| `synkro-sales-api` | — | `synkro-api-gateway`, `synkro-workflow` |
| `synkro-workflow` | `synkro-customers-api`, `synkro-products-api`, `synkro-sales-api` | `synkro-api-gateway` |
| `synkro-worker` | `synkro-products-api` | nothing (runs on a schedule, no inbound requests) |

**No cycle exists.** Verified with a depth-first search over the edges
above (`synkro-workflow → {customers, products, sales}`,
`synkro-worker → products`, `synkro-api-gateway → {all 5}`,
`synkro-front → gateway`): zero back-edges were found. The graph is a
tree rooted at `synkro-front`, with `synkro-auth-api`,
`synkro-customers-api`, `synkro-products-api` and `synkro-sales-api` as
its only sinks (nodes with no outgoing call) — see `service-boundary-rules.md`,
Rule 2, for why `synkro-sales-api` in particular must stay a sink. This
graph is unaffected by Sales moving to MongoDB (ADR-010) or by the
Customers portal moving to Angular (ADR-011): both are implementation
details behind `synkro-sales-api` and `synkro-customers-api`'s own HTTP
contracts, not new edges.

**The one non-call dependency:** every service that validates a JWT
holds `synkro-auth-api`'s RS256 public key as configuration, not as a
runtime call (`AUTH --> CUST`, dotted line, in `05-architecture/overview.md`'s
diagram). This is distribution of a key, never a request, so it is not
drawn as an edge above — including it as a call edge would wrongly
suggest `synkro-auth-api` is contacted on every request, which ADR-006
specifically rules out.

---

## Why this shape, not a mesh

A naive implementation of the register-sale flow could have had
`synkro-sales-api` call `synkro-customers-api` and `synkro-products-api`
directly to validate and reserve before writing the sale. That shape was
rejected (ADR-007): it makes Sales both a domain service and an
orchestrator, and it has no natural place to persist "which step did
this saga reach" if the process is interrupted. Centralizing the
cross-domain calls in `synkro-workflow` keeps every domain service a
sink, and keeps the one place that needs to coordinate also being the
one place that remembers how far it got.

---

## Repository-level dependency (what must exist to build/run each one)

This is a different graph from the one above — it is about repositories
and schemas, not runtime HTTP calls.

| Repository | Depends on | Why |
|---|---|---|
| `synkro-infra-postgres` | — | Defines the platform network and the shared PostgreSQL instance; nothing it starts depends on another repository existing first (`deployment.md` §3, §4). Renamed from `synkro-infra` (ADR-010 Decision 6); rename pending instructor action on [#159](https://github.com/code-corhuila/synkro-docs/issues/159) |
| `synkro-infra-mongo` | — | Defines the MongoDB instance for Sales; independent of `synkro-infra-postgres`, included by it into the same root composition (ADR-010) |
| `synkro-auth-db`, `synkro-customers-db`, `synkro-products-db` | `synkro-infra-postgres` | Each migration runner needs the shared PostgreSQL instance to be healthy before it can connect (ADR-009, ADR-012) |
| `synkro-sales-db` | `synkro-infra-mongo` | Its Liquibase runner needs the MongoDB instance to be healthy before it can connect (ADR-010) |
| `synkro-workflow` | `synkro-infra-postgres` (for `workflow_schema`), `synkro-auth-api` (for its service token) | Needs its own schema migrated and a valid service token to call the other three APIs |
| `synkro-auth-api`, `synkro-customers-api`, `synkro-products-api` | `synkro-infra-postgres`, their own `-db`'s migrations applied | A service answering requests before its schema is migrated gets `500 INTERNAL_ERROR` on every query (`deployment.md` §9) |
| `synkro-sales-api` | `synkro-infra-mongo`, `synkro-sales-db`'s migrations applied | Same failure mode as the PostgreSQL services, against its MongoDB database instead |
| `synkro-worker` | `synkro-auth-api` (for its service token), `synkro-products-api` (the service it calls) | Needs a valid service token and the target service reachable; it has no schema of its own |
| `synkro-api-gateway` | the 4 `-api` + `synkro-workflow` (to have somewhere to route to) | Can start before them, but every route answers `502`/`503` until its target is healthy |
| `synkro-front`, the 4 portals | `synkro-api-gateway` | The frontend host loads the portals as remotes (3 via Module Federation, 1 as an Angular custom element) and calls the gateway; neither needs a backend to build, only to work |

**This graph is also acyclic.** `synkro-infra-postgres` and
`synkro-infra-mongo` are the only nodes with no dependency; every other
repository depends on one of them directly or transitively, and nothing
depends back on a `-db`, an `-api` or a portal.

---

## Correlations

- Full startup sequence and exact commands → `05-architecture/deployment.md` §9
- Service responsibilities behind each node → `09-microservices/service-catalog.md`
- Why the calls are shaped this way → `09-microservices/service-boundary-rules.md`, Rule 4
- The saga this graph implements → ADR-007
- Shared instance and schema ownership → ADR-009
- Sales domain on MongoDB and infra repository naming → ADR-010
- Angular Customers portal → ADR-011
