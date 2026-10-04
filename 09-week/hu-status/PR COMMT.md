## HU-DOCS-64 — Completes risk register and technical backlog

- `15-project-control/risks.md` (updated)
- `15-project-control/technical-backlog.md` (updated)

| Field | Value |
|---|---|
| Branch | `docs/fill-risk-register-and-tech-backlog` |
| Owner | Sergio |

**Rama:**
```
docs/fill-risk-register-and-tech-backlog
```

**Commit:**
```
docs(project-control): fill risk register and technical backlog from ADR-005 through ADR-009
```

**Título del PR:**
```
docs(project-control): fill risk register and technical backlog from ADR-005 through ADR-009
```

**Descripción del PR:**

## Description

### Summary

Fills both `risks.md` and `technical-backlog.md` with the real risks and
deferred items identified in ADR-005 through ADR-009. Both files were
empty templates with only placeholder entries.

### Changes

- **`risks.md`**: 6 active risks with full detail (description,
  probability, impact, strategy, mitigation plan, contingency plan,
  trigger, owner, review date). Each traces to its originating ADR.
  R-001 (shared instance SPOF) cross-references AT-002 in overview.md
  and D-3 in the threat model. R-002 (GRANT misconfiguration)
  cross-references T-3.
- **`technical-backlog.md`**: 6 deferred items with description, reference,
  impact if not resolved, trigger to pick up, estimated effort and
  priority. TD-005 (DDL schema prefix rename) is marked High priority
  and traces to HU-DOCS-71. TD-002 (message broker) traces to ADR-007
  Decision 5 and the pattern guide.
- Template placeholders (R-001/R-002 with `[Risk name]`) replaced with
  real content.
- Correlations sections added to both files, pointing to the threat
  model, the AT table in overview.md, and the originating ADRs.

### Definition of Done

- [x] `risks.md` contains the risks from ADR-005 through ADR-009 consequences: shared instance SPOF, GRANT discipline, U-script discipline, manual service-token rotation, connection pool saturation, no message broker
- [x] `technical-backlog.md` contains deferred items: daily sales closing (ADR-007), message broker and outbox (ADR-007), per-product stock threshold (ADR-007), JWKS endpoint (cross-cutting.md §5), DDL schema prefix rename (HU-DOCS-71), operational monitoring (ADR-009)
- [x] Every tech-backlog entry has a "Reference" field pointing to the ADR that originated it
- [x] Every risk has an owner assigned from the team
- [x] Reviewed and approved by Angel (Tech Lead) and Jordan

**Reviewers:** Angel (Tech Lead, validate that the risks and their mitigations are correct) and Jordan (author of ADR-005, which originated several of these items).

Closes HU-DOCS-64 (part of HU-12).
