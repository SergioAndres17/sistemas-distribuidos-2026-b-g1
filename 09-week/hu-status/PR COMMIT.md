## HU-DOCS-76 — Barrido de residuos del modelo anterior

**`01-context/overview.md` 
**`scope.md` 
**`documentation-rules.md` 
**`security-rules.md` 
**`microservices-documentation.md` 
**`non-functional.md` 
**`service-catalog.md`


| Campo | Valor |
|---|---|
| Rama | `docs/sweep-stale-context-and-governance-references` |
| Responsable | Sergio |
| Depende de | HU-DOCS-75 (usa

 

**Rama y commit**

```
docs/sweep-stale-context-and-governance-references
```

```
docs(context): sweep stale pre-ADR-009 references across context and governance

Fix the week, branch name, stack table, and a scope.md claim that contradicted ADR-005 Decision 5 on sales_summary.
```

**Descripción del PR**

## Description

### Summary

Sweeps references to the superseded model (instance-per-domain, 4
services, `dev` branch) across `01-context/`, `00-governance/` and
`04-requirements/non-functional.md`. Fixes one real contradiction:
`scope.md` claimed the `sales_summary` table exists, which ADR-005
Decision 5 explicitly rejects.

### Changes

- **`overview.md`**: current week and milestones updated to week 9;
  `dev` → `develop`; Sales row no longer says it orchestrates Customers
  and Products; Gateway, Workflow and Worker rows added to the stack
  table (they were never listed); Infrastructure row updated to 8
  components; RabbitMQ note cites ADR-007 Decision 5 and TD-002.
- **`scope.md`**: week updated; repository and architecture constraints
  reflect the real 15-repository ecosystem and the saga model;
  **Sales Reports row corrected — it no longer claims `sales_summary`
  exists** (ADR-005 Decision 5).
- **`documentation-rules.md`**: `dev/qa/main` → `develop/qa/main`.
- **`security-rules.md`**: A03 says "schema in the shared instance"
  (ADR-009), matching `security-policy.md`.
- **`microservices-documentation.md`**: "Clients" → "Customers"; single-
  database reference → ADR-009; `golang-migrate` for Go → Flyway for
  every domain (ADR-005 Decision 2).
- **`non-functional.md`**: NFR-003 component count updated.
- **`service-catalog.md`**: note names both example folders and flags
  the Redis schema that was never adopted.

### Definition of Done

- [x] `01-context/overview.md` and `scope.md` state the real week, `develop` as the development branch and the current repository ecosystem
- [x] No diagram or text shows Sales calling Customers or Products; RabbitMQ appears only as deferred (ADR-007 Decision 5)
- [x] `scope.md` no longer claims the `sales_summary` table exists
- [x] A text search for `dev` as a branch name returns zero hits in `00-governance/` and `01-context/`
- [x] `security-rules.md` A03 says "own schema in the shared instance" (ADR-009)
- [x] `microservices-documentation.md` uses full component names and says Flyway in the `-db` repository
- [x] The status of the `_example-*` folders is explicit and the catalog note names both
- [x] Reviewed and approved by Angel (Tech Lead) and Jordan (author of ADR-005)

**Reviewers:** Angel and Jordan, because Jordan wrote ADR-005 and can confirm that the `sales_summary` correction is correct.

Closes HU-DOCS-76 (part of HU-13).
```

