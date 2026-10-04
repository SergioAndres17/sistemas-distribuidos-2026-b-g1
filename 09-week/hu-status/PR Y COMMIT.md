
---

**Rama:**
```
docs/align-products-api-catalog-contract
```

**Commit:**
```
docs(api): rewrite products contract for catalog, categories and stock adjustments
```

**Título del PR:**
```
docs(api): rewrite products contract for catalog, categories and stock adjustments
```

**Descripción del PR:**

## Description

### Summary

Rewrites `synkro-products-api.yaml` from scratch to match ADR-004
Decisions 2 (categories) and 4 (product catalog and stock adjustments),
replacing the pre-ADR-004 contract that still had `PATCH /stock` and
a "KNOWN GAP" note about missing categories.

### Changes

- **11 endpoints** covering the full product and category lifecycle
  plus manual stock adjustments and the health check.
- `PATCH /api/products/{id}/stock` removed (ADR-004 Decision 4);
  replaced by `POST /api/v1/products/{id}/stock-adjustments` with
  Idempotency-Key, delta, reason, and adjustedBy from the token's sub.
- Category CRUD under `/api/v1/products/categories` (ADR-004 Decision
  2), with duplicate-name and active-products-on-deactivate guards.
- All money fields are `…Cents` as `integer` (int64) — ADR-005
  Decision 3.
- All schemas reference `_shared.yaml` for Error, PageMeta, parameters
  and common responses.
- Health check documents connectivity to `products_schema` in the
  shared instance (ADR-009, cross-cutting.md §4).
- Server URL uses `synkro-products-api:8080` (inside the platform
  network, not published).

### What is NOT in this contract

Stock reservations (create, release) and stock alerts (list, create,
resolve) belong to HU-DOCS-58 and will be added in a follow-up PR.

### Definition of Done

- [x] Contract contains all catalog, category and manual-stock-adjustment endpoints from ADR-004
- [x] `PATCH /api/products/{id}/stock` is absent (removed by ADR-004)
- [x] Every path cites the decision that introduced it (ADR-001 §8 or ADR-004)
- [x] Money fields named `…Cents` and typed as integer (ADR-005 Decision 3)
- [x] Role and permission requirements per endpoint documented and consistent with authentication.md
- [x] Schemas reference `_shared.yaml` components
- [x] Reviewed and approved by Angel (Tech Lead, ADR-004 co-author) and Jordan (ADR-004 co-author)

**Reviewers:** Angel and Jordan.

Closes HU-DOCS-57 (part of HU-11).


