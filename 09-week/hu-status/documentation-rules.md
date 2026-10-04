# Documentation Rules

> These rules determine how documentation is written, organized, and maintained in this project.
> Documentation that does not follow these rules may be rejected in code review.

---

## Core principle

> **"Documentation is code. If it's not up to date, it's broken."**

Every HU that modifies system behavior MUST include updating the affected documents.
The DoD requires it.

---

## Language

| Artifact | Language |
|----------|----------|
| Source code (variables, functions, classes) | English |
| Code comments | English |
| Commits | English (Conventional Commits) |
| Branch names | English |
| Markdown documentation | English |
| OpenAPI contracts (descriptions) | English |
| Error messages returned to frontend | English (or localized) |
| Internal system logs | English |

> **Rule:** Once the language for each category is chosen, it is binding for the entire project.
> Mixing languages in the same category is grounds for PR rejection.

---

## File structure

```
Each section has its README.md that explains the folder's purpose.
Content documents use kebab-case.md (e.g.: domain-map.md, risk-register.md).
Templates are prefixed with _ to appear first (e.g.: _template-hu.md, _template-adr.md).
ADRs are numbered sequentially: ADR-001-short-title.md.
```

---

## Component naming

Every deployable component is named exactly as its repository. Repositories are audited by exact name and never renamed after creation, so one name per component avoids a second vocabulary.

| Kind | Form | Values | Examples |
|---|---|---|---|
| Domain component | `synkro-<domain>-<piece>` | Domains: `auth`, `customers`, `products`, `sales`. Pieces: `db`, `api`, `portal` | `synkro-sales-api`, `synkro-customers-db` |
| Cross-cutting component | `synkro-<name>` | `api-gateway`, `workflow`, `worker`, `infra`, `front`, `docs` | `synkro-workflow` |

**Rules:**
- Every identifier uses the full component name: file names, contract titles, diagrams, tables, compose services and the `service` field of the logs. The documentation repository is `synkro-docs`.
- Inside a compose file, a `-db` repository declares its migration runner as `<domain>-db-migrate` (`customers-db-migrate` lives in `synkro-customers-db`). The database instance (`synkro-db`) is declared only by `synkro-infra` (ADR-009). Every other compose service uses its repository name (`synkro-sales-api`).
- Bounded-context names (Auth, Customers, Products and Inventory, Sales) are used only when talking about the domain, never as a component name: "the Sales context" is the domain; `synkro-sales-api` is the service.
- Contract files are `07-api/contracts/openapi/synkro-<domain>-api.yaml` and `synkro-workflow.yaml`.
- Templates and examples kept from the documentation template carry a leading `_` (`_template-adr.md`, `_example-api-gateway.yaml`), so they are never mistaken for project documents.

---

## What to document and what NOT to

### DO document

| What | Where |
|------|-------|
| Non-obvious architectural decisions | `05-architecture/decisions/records/ADR-NNN.md` |
| Business rules and domain invariants | `02-domain/entities-and-rules.md` |
| API contracts for each service | `07-api/contracts/openapi/synkro-<domain>-api.yaml` (see "Component naming") |
| Data model changes | `06-data/models.md` |
| Operational procedures | `13-operations/` |
| Identified risks | `15-project-control/risks.md` |

### DO NOT document

- What the code already says clearly (do not repeat in comments what can be read in the code)
- Temporary decisions or experiments that will be reverted
- Implementation details of external libraries (those have their own documentation)
- Change history (that's what git log is for)

---

## Owners per section

| Section | Owner | Review frequency |
|---------|-------|-----------------|
| `00-governance/` | Angel Gustavo Solano Trujillo (Technical Lead) — validates governance changes before pushing to `main` | Start of each sprint |
| `02-domain/` | Entire team + Product Owner (instructor) | When the domain changes |
| `04-requirements/` | Product Owner (instructor) | Each sprint |
| `05-architecture/` | Entire team | Each design decision |
| `07-api/contracts/` | **No fixed per-service owner yet** (see `domain-map.md`, "Owning Team: entire team, no fixed service owner yet") — until one is assigned, whoever opens the PR touching that contract | Each API change |
| `09-microservices/` | Same as above — no fixed per-service owner yet | Each release |

---

## Document format

### Headings
- `# H1` — only one per file; it is the title
- `## H2` — main sections
- `### H3` — subsections
- Do not use H4 or deeper; if you need it, the document has too much hierarchy

### Tables
Use tables for comparisons, registers, and matrices. Do not use tables for simple lists.

### Code
Always use code blocks with the language specified.

### Template instructions
Blocks marked `> [!NOTE] INSTRUCTIONS` indicate the document is an unfilled template.
Remove them when the document is complete.

---

## Update process

This process differs depending on where the document lives, because the `docs` repo has only one permanent branch (`main`) while code repos have three (`develop/qa/main`):

**For documentation inside a code repo** (backend, frontend, database):
1. The developer identifies which documents their change affects.
2. Updates the documents together with the code (same PR).
3. The reviewer verifies the documentation is up to date.
4. If the PR closes a HU with API impact → the OpenAPI contract must be updated.

**For documentation in the `docs` repo:**
1. The section owner (see table above) identifies which file changes.
2. Drafts the content and shares it for team review.
3. Creates a `docs/<short-description>` branch from `main`, commits the
   change there, and opens a Pull Request into `main` — direct commits to
   `main` are not allowed, per `00-governance/branching-policy.md`. See
   `00-governance/git-conventions.md`, "Exception: the `docs` repository",
   for the exact commands.
4. The Pull Request requires 1 approval from `ariel5253` before merging,
   same as `main` in the code repositories.
5. Updates `04-requirements/user-stories.md`, marking the corresponding HU as done.

---

## Correlations

- Git conventions → `00-governance/git-conventions.md`
- Per-microservice documentation standard → `00-governance/microservices-documentation.md`
- Definition of Done (docs as part of DoD) → `00-governance/definition-of-done.md`
- Definition of Ready → `00-governance/definition-of-ready.md`
