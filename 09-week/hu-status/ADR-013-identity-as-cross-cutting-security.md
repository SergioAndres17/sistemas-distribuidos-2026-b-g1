# ADR-013 — Identity Service as the Cross-Cutting Security Service

> **Status stays `Proposed`**: the instructor has not yet answered the linked issue.
> This ADR passes to `Accepted` only once that answer arrives — see Decision below.

| Field | Value |
|-------|-------|
| **ID** | ADR-013 |
| **Date** | — (set on acceptance) |
| **Status** | Proposed |
| **Authors** | Sergio Andrés Ordóñez Díaz |
| **Reviewers** | Angel Gustavo Solano Trujillo — Tech Lead, Jordan Ramirez Gallego, Fredman Santiago Plazas Artunduaga — Development team |
| **Modifies** | — (with Option A, replaces no decision) |
| **Traced to** | HU-ARQ-26 |

---

## Context

The course architecture requires, besides one service per domain, a cross-cutting security service that manages identity and issues tokens.

The system already has a component that does exactly that. `synkro-auth-api` manages users and their roles, is the only holder of the RS256 private key, issues the tokens of people and the service tokens of `synkro-workflow` and `synkro-worker`, and every other service validates with its public key without calling it (ADR-006). `09-microservices/service-catalog.md` already classifies it as a cross-cutting identity support service, and `02-domain/domain-map.md` places it upstream of every bounded context.

What is not written is whether that component **is** the cross-cutting security service or whether a separate one is required. Its repositories are named after a domain (`synkro-auth-db`, `synkro-auth-api`, `synkro-auth-portal`), not after a cross-cutting repository. Without a decision, the team could end up building a second service that duplicates token issuance, or delivering the system without the required component.

The question was sent to the instructor in a `synkro-docs` issue: _[issue link]_.

**Constraints:**
- ADR-006 is immutable: `synkro-auth-api` is the only holder of the private key, and every service validates the token itself.
- Existing repository names do not change; a new repository is created only by the instructor.
- The `synkro-auth-api.yaml` contract already covers registration, login, refresh, logout, user lookup and service tokens.

---

## Decision — Which component is the cross-cutting security service

### Options

**Option A — `synkro-auth-api` is the cross-cutting security service.** It stays as it is, with its database and its portal.
- **Pros:** no new component; identity, credentials and token issuance have a single owner; no contract or ADR changes.
- **Cons:** the cross-cutting service carries a domain-style name; someone reading only the repository list would not recognize it as cross-cutting.

**Option B — A new cross-cutting repository issues the tokens; Auth keeps only users and roles.** The new service receives sign-in, asks `synkro-auth-api` to verify the credentials, and signs the token.
- **Pros:** the repository's name reflects its cross-cutting nature.
- **Cons:**
  - One more service to build, test and deploy, with its own store for refresh tokens.
  - A new contract on `synkro-auth-api` to verify credentials, which does not exist today.
  - The private key changes owner, which replaces an ADR-006 constraint.
  - A bootstrapping problem: the new service's call to Auth needs a service token, and the one who issues them is the new service itself.
  - Every sign-in now depends on two services instead of one.

### Decision

**We propose: Option A**, pending the instructor's confirmation.

- `synkro-auth-api` is the system's cross-cutting security service. It is cross-cutting by what it does, not by its name: every service depends on it to trust a token, and it depends on none of them.
- Its three repositories keep their name and structure: `synkro-auth-db` (schema `auth_schema`), `synkro-auth-api` and `synkro-auth-portal`.
- No repository, contract or store is created.
- `02-domain/domain-map.md` describes it with that role explicitly.

**Resolution on repository naming.** `synkro-auth-db`, `synkro-auth-api` and `synkro-auth-portal` follow the domain-triad pattern (`-<domain>-<piece>`), not the cross-cutting pattern (`-security`) the course uses for this kind of function elsewhere. **This ADR does not rename them.** The mismatch is recorded here as an accepted and documented deviation, not as an open question: renaming three repositories that already hold history, branch protection and `CODEOWNERS` set up by the instructor has a real cost, and nothing about how the system operates depends on the name matching the pattern — Decision 2 above is what makes Auth cross-cutting (every service depends on it, it depends on none), not its repository name. If the instructor's answer on the linked issue requires the naming pattern to match too, that is a new, separate request to the instructor, handled the same way the renames in ADR-010 Decision 6 were: an issue asking for the rename, not a silent assumption.

### Dominant criterion

**No new component without a need that justifies it.** Separating token issuance from identity solves no problem in the system and adds a service to every sign-in's path.

### Accepted cost

- The cross-cutting security service is named like a domain (`auth`), not like the `-security` pattern the course uses elsewhere for this kind of function — an accepted, documented deviation, not a renaming commitment.
- If the instructor requires a separate repository, this decision is replaced by Option B with all its costs.

---

## Consequences

**If Option A is confirmed:**
- No repository, contract, schema or variable changes.
- `02-domain/domain-map.md` gains one line naming Auth as the cross-cutting security service.
- This ADR moves to `Accepted` with the date of the answer.

**If the instructor requires Option B:**
- This ADR records Option B as decided, with its costs, and moves to `Accepted`.
- A follow-up story is created to: request the new repository, extend the endpoint register with credential verification, decide with a new ADR the private key's change of owner (replaces an ADR-006 constraint), and define the store for refresh tokens.
- No already-accepted document is edited outside that story.

**What must be watched:**
- While this ADR is `Proposed`, no additional security service is built.
- The first delivery does not depend on this answer: the development sign-in covers identity until `synkro-auth-api` exists (ADR-008 Decision 4).

---

## Affected documents

| Document | Required change |
|----------|-----------------|
| `02-domain/domain-map.md` | Auth's role as the cross-cutting security service (this PR, once Option A is accepted) |
| `15-project-control/open-questions.md`, `15-project-control/dependencies.md` | Open question and dependency on the instructor's answer (HU-DOCS-88) |
| `05-architecture/decisions/README.md` | ADR-013 row with its status (this PR) |
| `09-microservices/service-catalog.md` | **Unchanged**: already describes it as a cross-cutting identity support service |
| ADR-006 | **Unchanged**: immutable |

---

## Immutability rule

Once `Accepted`, this ADR is not edited. Any change is a new ADR that names this one, and the sections it replaces, in its **Modifies** field.

---

## References

- Token validation per service and service tokens → `05-architecture/decisions/records/ADR-006-token-validation-per-service.md`
- Identity and roles in the original architecture → `05-architecture/decisions/records/ADR-001-architecture.md`
- Development sign-in → `05-architecture/decisions/records/ADR-008-cross-cutting-stack.md` Decision 4
- Auth contract → `07-api/contracts/openapi/synkro-auth-api.yaml`
- Context map → `02-domain/domain-map.md`
- `synkro-auth-api` service card → `09-microservices/service-catalog.md`