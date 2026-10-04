

**Rama:**
```
docs/correct-security-docs-for-shared-instance
```

**Commit:**
```
docs(security): correct threat model, security policy and cross-cutting for shared-instance topology
```

**Título del PR:**
```
docs(security): correct threat model, security policy and cross-cutting for shared-instance topology
```

**Descripción del PR:**

## Description

### Summary

Corrects the three security and cross-cutting documents to reflect the
shared-instance topology decided in ADR-009: one PostgreSQL instance per
environment with schema-level isolation, instead of one instance per
domain.

### Changes

- **`security-threat-model.md` scope**: "each domain has its own database
  instance" → "all domains share one instance per environment, isolated
  by schema and GRANT".
- **T-3**: isolation by per-schema credentials and CI verification, not
  by separate instances.
- **T-6**: `workflow-db` → `workflow_schema` in the shared instance.
- **D-3**: the most significant change — a slow report query can now
  affect ALL domains, not just Sales. Adds connection-pool sizing as
  mitigation and references AT-002 (shared instance as accepted single
  point of failure).
- **E-3**: "only synkro-auth-api connects to auth-db" → "only
  synkro-auth-api has credentials for auth_schema".
- **`cross-cutting.md` §4**: health check dependency table points to
  `synkro-db — connectivity to <domain>_schema` instead of separate
  `<domain>-db` instances.
- **`cross-cutting.md` §9**: summary table updated.
- **`security-policy.md`**: principle 2 (Least Privilege) and SQL
  Injection Prevention now say "schema in the shared instance".
- **References sections** in all three files cite ADR-009.

### What is NOT changed

The STRIDE threat count stays at 25 (no new threats added, no threats
removed). D-3's status remains "Risk accepted" — the shared instance
widens the blast radius, but the mitigation (indexed queries,
connection-pool limits) and the accepted-risk declaration in ADR-009
cover it.

### Definition of Done

- [x] Threat model scope reflects the shared instance isolated by schema and GRANT
- [x] T-3 explains isolation by per-schema credentials, not by separate instances
- [x] D-3 acknowledges the shared instance as a shared point of failure with connection-pool mitigation
- [x] E-3 says "has credentials for auth_schema", not "connects to auth-db"
- [x] T-6 says "workflow_schema in the shared instance", not "its own instance"
- [x] Cross-cutting health check table points to synkro-db with schema names
- [x] Security policy least-privilege and SQL injection sections say "schema in the shared instance"
- [x] Full-text search for "its own instance" and "its own database" returns zero hits in all three files
- [x] Reviewed and approved by Angel (Tech Lead) and Jordan (ADR-005 author)

**Reviewers:** Angel (Tech Lead, ADR-009 author) y Jordan (autor del ADR-005 original que contenía las decisiones de aislamiento).

Closes HU-DOCS-56 (part of HU-11).


