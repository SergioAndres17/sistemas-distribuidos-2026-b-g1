# Diagram Index

> The registry of all system diagrams.
> Diagrams go in `08-uml/diagrams/source/` (draw.io `.drawio` XML files).
> Exported renders go in `08-uml/diagrams/exports/` (`.svg`).

---

## When to create a diagram?

```
✓ When the flow involves 3+ services or actors
✓ When the architecture needs to be communicated to a non-technical stakeholder
✓ When data modeling is complex (ER diagram)
✓ When a bug took more than 2 hours to understand (retroactive sequence diagram)

✗ For simple 2-element flows — code describes them better
✗ To "document for documentation's sake" — an outdated diagram is worse than none
```

---

## Tool: draw.io

All diagrams in this folder use draw.io (diagrams.net) for better visual
fidelity — real BPMN notation (diamond gateways, thick-border end
events, proper swimlanes) and easier team readability than a text-based
alternative.

**How to export a diagram as SVG** (required after creating or editing
any `.drawio` file, so the rendered image in `exports/` stays in sync
with the source):

1. Open the `.drawio` file at [app.diagrams.net](https://app.diagrams.net) (File → Open From → Device)
2. File → Export as → SVG
3. Save it to `08-uml/diagrams/exports/` with the same base name as the source file (e.g. `bpmn-auth-login.drawio` → `bpmn-auth-login.svg`)

**Keep source and export together in every PR.** If you edit a
`.drawio` file, re-export its SVG in the same commit — an SVG that
doesn't match its source is exactly the kind of stale diagram this
file's own rule warns against.

---

## File naming

```
[type-prefix]-[descriptive-name].drawio

Examples:
  c4-01-context.drawio
  c4-02-containers.drawio
  bpmn-auth-login.drawio
  bpmn-sales-registration.drawio
```

---

## Diagram registry

### Architecture diagrams (C4)

| ID | Name | Type | Tool | Source file | Description |
|----|------|------|------|-------------|-------------|
| C4-01 | System Context | C4 L1 | draw.io | `source/c4-01-context.drawio` | SynkroTech and its 3 user roles (SALESPERSON, INVENTORY, ADMIN); no external systems |
| C4-02 | Container Diagram | C4 L2 | draw.io | `source/c4-02-containers.drawio` | The frontend host and its 4 portals (3 React remotes via Module Federation, 1 Angular custom element for Customers, ADR-011), the gateway, the 4 domain services, `synkro-workflow` and `synkro-worker`, one shared PostgreSQL instance per environment with 4 schemas (ADR-009, ADR-012), and one MongoDB instance per environment for the Sales domain (ADR-010) |

**Note:** the same content also lives as Mermaid diagrams in `05-architecture/overview.md` (§2, §3), which is where it originated and stays actively maintained. These draw.io versions were added to this folder for visual quality and team readability. Whoever updates the architecture (new service, new data flow) should update **both** places in the same PR — this is a deliberate exception to the single-source rule the rest of this index follows, made because the team specifically wants richer visuals here.

`08-uml/diagrams/source/c4-container-example.md` is the professor's original Mermaid template example (generic `servicio_a`/`servicio_b` placeholders) — kept as reference material, not filled in.

### Process diagrams (BPMN)

| ID | Name | Type | Tool | Source file | Description |
|----|------|------|------|-------------|-------------|
| BPMN-01 | Authentication (Login) | BPMN | draw.io | `source/bpmn-auth-login.drawio` | Login flow: rate limiting, credential validation, JWT issuance |
| BPMN-02 | Register Customer | BPMN | draw.io | `source/bpmn-customers-register.drawio` | Customer creation with uniqueness/validity checks |
| BPMN-03 | Update Customer | BPMN | draw.io | `source/bpmn-customers-update.drawio` | Customer update with active/validity checks |
| BPMN-04 | Deactivate Customer | BPMN | draw.io | `source/bpmn-customers-deactivate.drawio` | Soft-delete flow for a customer |
| BPMN-05 | Register Product | BPMN | draw.io | `source/bpmn-products-register.drawio` | Product creation with category/price/stock checks |
| BPMN-06 | Update Product | BPMN | draw.io | `source/bpmn-products-update.drawio` | Product update with active/validity checks |
| BPMN-07 | Deactivate Product | BPMN | draw.io | `source/bpmn-products-deactivate.drawio` | Soft-delete flow for a product |
| BPMN-08 | Sale Registration | BPMN | draw.io | `source/bpmn-sales-registration.drawio` | The `register-sale` saga of `synkro-workflow` (ADR-007 Decisions 1 and 2): state saved after every step, validate customer, reserve stock, register sale, and release stock as compensation; outcomes `COMPLETED`, `COMPENSATED` and `FAILED` |

**Note on BPMN-08:** the diagram follows ADR-007 Decisions 1 and 2. The saga state is saved after every step, so a restart resumes it instead of losing reserved stock. Each step is idempotent, and a compensation that still fails after its retries ends the saga in `FAILED` for a person to decide. Earlier versions showed the sales service calling the other domains directly, and later a stateless workflow. The saga's shape is unaffected by Sales moving to MongoDB (ADR-010): step 3 still calls `synkro-sales-api` over HTTP, which is all this diagram shows.

### Sequence diagrams

| ID | Name | Tool | Source file | Description |
|----|------|------|------|-------------|
| SEQ-01 | Login flow | — | — | Covered by BPMN-01 above; no separate sequence diagram needed |

### State diagrams

*(none yet — no entity in SynkroTech has a lifecycle complex enough to warrant one beyond `active`/`inactive`, already covered by the BPMN deactivate flows above)*

### ER diagrams (data)

| ID | Name | Tool | Source file | Service |
|----|------|------|------|---------|
| ERD-01 | Auth schema | Mermaid | `06-data/models.md`, "Domain: auth" | synkro-auth-api |
| ERD-02 | Customers schema | Mermaid | `06-data/models.md`, "Domain: customers" | synkro-customers-api |
| ERD-03 | Products schema | Mermaid | `06-data/models.md`, "Domain: products" | synkro-products-api |
| ERD-04 | Sales document model | Mermaid | `06-data/models.md`, "Domain: sales" | synkro-sales-api (MongoDB, ADR-010 — the `sale` collection with its lines embedded, not a relational schema) |

Note: removed "(incl. `outbox`)" — outbox was removed by ADR-007.

**Not converted to draw.io.** These stay as Mermaid inside `06-data/models.md`, right next to the actual table or document definitions they describe — moving them out would separate the diagram from the structure it documents.

---

## Correlations

- Architecture view (Mermaid originals) → `05-architecture/overview.md`
- Domain bounded contexts → `02-domain/domain-map.md`
- Data models per schema and per database → `06-data/models.md`
- Sale registration saga → `05-architecture/decisions/records/ADR-007-persistent-saga-and-scheduled-work.md`, Decisions 1 and 2 (persisted state; saga resource, steps and compensation). Replaces ADR-003 Decision 4 (stateless orchestration)
- Sales domain on MongoDB → `05-architecture/decisions/records/ADR-010-sales-on-mongodb.md`
- Angular Customers portal → `05-architecture/decisions/records/ADR-011-angular-customers-portal.md`
