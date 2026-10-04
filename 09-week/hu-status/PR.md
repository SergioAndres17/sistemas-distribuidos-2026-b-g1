# HU-ARQ-26 — Register, issue for the instructor and domain map

**Branch:** `docs/add-adr-013-identity-as-cross-cutting-security`
**Commit and PR title:** `docs(architecture): add ADR-013 — identity service as cross-cutting security`

Order of work:

1. Open the issue of section 2 and put its link in the ADR's Context.
2. Merge the ADR as `Proposed` with the register row of section 1.
3. When the instructor answers, open a second pull request from a new `docs/` branch: status, date and, if Option A is confirmed, the domain-map text of section 3.

---

## 1. Changes in `05-architecture/decisions/README.md`

### New row (after ADR-012)

```markdown
| [ADR-013](records/ADR-013-identity-as-cross-cutting-security.md) | Identity Service as the Cross-Cutting Security Service | Proposed | 2026-10 | — | — | ADR-006 (private key holder, token validation in every service), ADR-001 (identity and roles) |
```

No "Modified or superseded by" cell changes: with Option A, ADR-013 replaces no decision.

---

## 2. Issue for the instructor

**Title:** `Question: does synkro-auth-api satisfy the cross-cutting security service?`

**Body:**

```markdown
## Question

Does `synkro-auth-api` count as the system's cross-cutting security service, or is a separate cross-cutting repository required?

## What `synkro-auth-api` already does

- Manages the identity of the system's users: credentials and roles.
- Is the only holder of the RS256 private key.
- Issues the tokens of people (login, refresh, logout) and the service tokens of `synkro-workflow` and `synkro-worker`.
- Every other service validates tokens locally with its public key, without calling it.

Its repositories are `synkro-auth-db`, `synkro-auth-api` and `synkro-auth-portal`.

## What we propose

Keep `synkro-auth-api` as the security service, with no new repository. A second service that only signs tokens would add a component to every login, a new contract to verify credentials, and the question of who issues its own service token.

## Decision record

`05-architecture/decisions/records/ADR-013-identity-as-cross-cutting-security.md`, status `Proposed` until this issue is answered (HU-ARQ-26).

## If a separate repository is required

We will record that option in the same ADR and request the repository in a new issue.
```

---

## 3. Text for `02-domain/domain-map.md` (only when Option A is confirmed)

### In the Auth bounded context, add this line after its **Responsibility**

```markdown
**Role in the system:** cross-cutting security service. It manages identity and issues every token, for people and for services; no other component signs tokens (ADR-006, ADR-013).
```

### Replace the note under "Relationships between contexts"

Current:

```markdown
**Note:** no context is upstream of Auth.
```

New:

```markdown
**Note:** no context is upstream of Auth. Auth is the system's cross-cutting security service (ADR-013): every context, `synkro-workflow` and `synkro-worker` depend on it to trust a token, and it depends on none of them.
```

### Commit of the second pull request

`docs(architecture): accept ADR-013 and state the security role of auth in the domain map`
