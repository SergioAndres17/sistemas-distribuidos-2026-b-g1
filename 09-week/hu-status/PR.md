# HU-DOCS-83 — Security documents and non-functional requirements

**Branch:** `docs/align-security-and-nfr-with-two-engines`
**PR title:** `docs(governance): align security documents and nfr with two engines`
**Depends on:** HU-ARQ-23, HU-ARQ-25
| HU-DOCS-83 | HU-ARQ-23, HU-ARQ-25 | Sergio |

**Branch:** `docs/align-security-and-nfr-with-two-engines`
**Commit title:** `docs(governance): align security documents and nfr with two engines`

```markdown
## Description

### Summary

Updates the security policy, technical security rules, STRIDE threat model
and non-functional requirements for ADR-010 (Sales on MongoDB), ADR-011
(Angular Customers portal) and ADR-012 (instance bootstrap and the new
credential model). The credential model correction applies to every
PostgreSQL domain, not only to what changed for Sales.

### Changes

- **`00-governance/security-policy.md`**: Least Privilege principle and
  Secret Management rewritten for ADR-012's fixed-name service users and
  administrator-only migrations; new "Document Query Injection Prevention"
  section for Sales' MongoDB queries; OWASP A03 row updated.
- **`00-governance/security-rules.md`**: A03 gains a MongoDB-specific rule
  (typed query builders, no raw body as a query document, validator as a
  second layer).
- **`05-architecture/security-threat-model.md`**: Scope extended to both
  instances; T-3 and D-3 rewritten to explain the stronger isolation Sales
  gets from being on a separate instance; new **T-7** for document-query
  injection; E-1 notes the Angular portal is unaffected; Status Summary
  updated (Tampering 6→7, Total 25→26).
- **`04-requirements/non-functional.md`**: NFR-003, NFR-004, NFR-007 and
  NFR-009 updated for two engines and the ADR-012 credential model; priority
  matrix and correlations extended.

### Definition of Done

- [x] No document in this PR still describes the two-credential-pair-per-domain
      model or cites `<DOMAIN>_DB_USER`/`FLYWAY_PLACEHOLDERS_APP_USER`
- [x] Every mention of Sales' isolation explains it is a separate instance,
      not a schema, and is at least as strong as the PostgreSQL model
- [x] The new T-7 threat has a real mitigation already implemented in the
      system's design, not a placeholder
- [x] NFR-009's metric names a concrete, greppable check for both engines
- [x] Reviewed and approved by Sergio and Angel (Tech Lead)

Closes HU-DOCS-83 (part of HU-14).
```

