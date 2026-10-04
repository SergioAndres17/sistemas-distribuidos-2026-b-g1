## HU-DOCS-75 — Alinea `domain-map.md` con la saga y el workflow

**Entregable**

- `02-domain/domain-map.md` (responsabilidad de Sales, mapa y tabla de relaciones)
- `01-context/glossary.md` (solo si faltan términos)

| Campo | Valor |
|---|---|
| Rama | `docs/align-domain-map-with-saga` |
| Responsable | Sergio |
| Depende de | Ninguna |
| Prioridad | Must Have |

**Rama y commit**

```
docs/align-domain-map-with-saga
```

```
docs(domain): align domain map with the saga and the workflow

Show the saga steps and the worker as the cross-context calls; Sales no longer calls other contexts.
```

**Descripción del PR**

## Description

### Summary

Aligns `domain-map.md`, the source of truth for the domain, with ADR-007
and `entities-and-rules.md`: the map still said Sales orchestrates
Customers and Products and validates against them before selling. The
sale is registered by the `register-sale` saga of `synkro-workflow`,
and Sales makes no outgoing calls.

### Changes

- **Sales**: responsibility rewritten (registers what the saga sends,
  with prices frozen at reservation; no calls to other contexts).
- **Products and Inventory / Auth**: main entities now include
  `StockReservation` and `StockAlert`; Auth is also the issuer of the
  service tokens (ADR-006).
- **Orchestration and scheduled work**: new section explaining that
  `synkro-workflow` and `synkro-worker` own no business entity, so they
  are not bounded contexts; the list stays Auth, Customers, Products and
  Inventory, Sales.
- **Relationship map and tables**: the arrows from Sales to Customers
  and Products are removed; the three saga steps (`validate-customer`,
  `reserve-stock` / `release-stock`, `register-sale`) and the worker's
  low-stock job are shown as calls of the orchestrator and the worker.
- **Ubiquitous language**: Service token, Stock reservation, Stock alert
  and Saga added.
- **`glossary.md`**: the "Microservice" and "Schema" rows describe one
  schema per domain in the shared instance (ADR-009) instead of one
  database per domain; "Service Token" row added.

### What is NOT changed

`entities-and-rules.md` and the definitions of the existing
terms are untouched. The "Sale" definition stays as it is.

### Definition of Done

- [x] The relationship map and tables have no relation from Sales to Customers or Products
- [x] The three saga steps appear as calls of `synkro-workflow`, each with its participant
- [x] `synkro-worker` → Products and Auth issuing service tokens appear in the map
- [x] `synkro-workflow` and `synkro-worker` are explained as orchestrator and scheduled job, not as bounded contexts
- [x] No statement contradicts `entities-and-rules.md` ("Resolving external references between services") or ADR-007
- [x] Glossary rows no longer describe one database per domain
- [x] Reviewed and approved by Angel (author of `entities-and-rules.md` and ADR-006) and Jordan (author of ADR-005)

**Reviewers:** Angel and Jordan.

Closes HU-DOCS-75 (part of HU-13).
