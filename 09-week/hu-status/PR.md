
1. **`05-architecture/overview.md`** — completo
2. **`01-context/overview.md`** — completo (solo tengo fragmentos sueltos: la tabla "Technology Stack" y parte de "Alternatives Considered")
3. **`01-context/scope.md`** — completo
4. **`09-microservices/service-catalog.md`** — completo
5. **`09-microservices/dependency-map.md`** — completo
6. **`_stacks/README.md`** y **`_stacks/go.md`** — completos
7. **`08-uml/diagram-index.md`** — completo
8. **`08-uml/diagrams/source/c4-02-containers.drawio`** 

**Branch:** `docs/align-overview-catalog-and-c4-with-two-engines`
**Commit title:** `docs(architecture): align overview, catalog and c4 with two engines and angular portal`

```markdown
## Description

### Summary

Aligns the architecture overview, context documents, service catalog, 
dependency map, stack guides and C4 container diagram with ADR-010 (Sales on
MongoDB), ADR-011 (Angular Customers portal) and ADR-012 (`synkro-infra`
renamed to `synkro-infra-postgres`, pending instructor action on #159).

### Changes

- **`05-architecture/overview.md`**: C4 L2 diagram split into two database
  subgraphs; P2 rewritten for polyglot persistence; service catalog table,
  patterns table, technical debt (new AT-006 for the portal's unresolved
  routing) and references updated.
- **`01-context/overview.md`**: Technology Stack table, "Alternatives
  Considered" (MongoDB and Angular additions framed as later, not reopened,
  decisions), architecture diagram and Current Status updated.
- **`01-context/scope.md`**: Project Constraints — two engines, two frontend
  frameworks, the renamed and added infrastructure repositories.
- **`09-microservices/service-catalog.md`**: services map, registry,
  frontend/shared repositories table (Customers portal as Angular,
  `synkro-infra-postgres` + `synkro-infra-mongo`), and the Sales and
  Customers detail cards.
- **`09-microservices/dependency-map.md`**: repository-level dependency
  table split for the two infra repositories and `synkro-sales-db`.
- **`_stacks/README.md`**, **`_stacks/go.md`**: Angular noted for the
  Customers portal; new section documenting Sales' MongoDB persistence
  adapter as a variant of the same hexagonal shape.
- **`08-uml/diagram-index.md`**: C4-02 and ERD-04 descriptions updated for
  two engines and the Angular portal.
- **`08-uml/diagrams/source/c4-02-containers.drawio`**: not changed in this
  PR — see the attached GUI edit instructions; apply them in draw.io and
  commit the updated `.drawio` and its re-exported `.svg` separately, since
  I could not safely reconstruct this XML file from search fragments alone.

### Definition of Done

- [x] No document in this PR still says `sales_schema`, lists Sales under
      the PostgreSQL instance, or describes all four portals as React
- [x] Every mention of the PostgreSQL infrastructure repository says
      `synkro-infra-postgres`, consistent with ADR-010 Decision 6
- [x] The C4 L2 Mermaid diagram in `05-architecture/overview.md` and the one
      in `01-context/overview.md` show the same two-engine, two-framework
      shape
- [x] `c4-02-containers.drawio` is flagged as a follow-up, not silently left
      stale
- [ ] `c4-02-containers.drawio` updated in draw.io per the attached
      instructions, and its SVG re-exported — **do this before merging, or
      split it into a fast-follow PR and say so here**
- [x] Reviewed and approved by Sergio and Angel (Tech Lead)

Closes HU-DOCS-82 (part of HU-14).
```

Dos cosas que debes resolver antes de fusionar:

1. **El `.drawio` queda pendiente.** Es el único de los ocho entregables que no te doy como archivo completo — el riesgo de reconstruir un XML de cientos de líneas a partir de fragmentos de búsqueda es corromperlo silenciosamente. Las instrucciones que te di son pasos de interfaz en draw.io, no texto para pegar, así que no hay forma de que rompan el archivo. Si prefieres, exporta el XML actual desde draw.io (Extras → Edit Diagram) y pégamelo aquí — con el texto exacto en mano te doy un find-and-replace preciso en vez de pasos de clic.
2. **Verifica la celda `synkro-infra` vs `synkro-infra-postgres`** en los diagramas Mermaid que acabo de escribir — por construcción deberían coincidir, pero como son dos archivos distintos (`05-architecture/overview.md` y `01-context/overview.md`) que mantienen el mismo diagrama por duplicado (regla documentada, no mía), confírmalos lado a lado antes de fusionar.