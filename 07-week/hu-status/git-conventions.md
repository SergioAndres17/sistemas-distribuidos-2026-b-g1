# Git Conventions

> **Read this document before making your first commit to the project.**
>
> This file records **how our team works**. The course rule, which is non-negotiable,
> lives in `00-governance/branching-policy.md` — where the two documents differ,
> the course policy takes precedence. This file was aligned with that policy in HU-DOCS-28.

---

## Branch Strategy

Three permanent branches in the code repositories: `dev`, `qa`, `main`.
**Equivalence note:** in this project we use `dev` as the name for what
`branching-policy.md` calls `develop` — it is the same concept and the same
place in the workflow. We use `dev` consistently across all backend, frontend,
and database repositories.

**Single rule, no exceptions: no permanent branch (`dev`, `qa`, `main`)
accepts a direct commit.** You always enter through a child branch, and you
always leave through a Pull Request.

Each permanent branch only receives child branches of its own type:

| Parent branch | Child branches that feed it | Example | Gate |
|---|---|---|---|
| `dev` | `feat/` `fix/` `chore/` | `feat/auth-jwt-login` | Green CI · review according to team rule |
| `qa` | `qa/` | `qa/hu-07-loan-renewal` | Contract tests (Pact) · review according to team rule |
| `main` | `release/` `hotfix/` | `release/1.2.0` | **1 approval from `ariel5253`** + CODEOWNERS + resolved conversations |

```
main        ← Production. Only receives release/ and hotfix/. Always stable.
  qa        ← Pre-production validation (staging). Only receives qa/.
  dev       ← Continuous integration. Only receives feat/, fix/, chore/.
    feat/[description]    ← One branch per feature/user story
    fix/[description]     ← One branch per bug fix
    chore/[description]   ← Infrastructure, docs, dependencies
```

**`hotfix/` is created from `main`, not from `dev`.** It is an urgent
production fix: it branches from `main`, the fix is made, a PR is opened back
to `main`, and **afterwards it is reapplied (with `cherry-pick -x`)** to
`qa` and `dev` so that the three branches do not become out of sync.

**SynkroTech — team rules:**
- No one makes direct commits to `main`, `qa`, or `dev`.
- Each task (HU) = one branch + one Pull Request.
- One branch = one task (do not mix different features in the same branch).
- Branches are deleted after the merge.
- The 4 backend repositories, the 4 frontend repositories, and the database repository
  follow this same `main/qa/dev` + `feat/fix/chore/hotfix` scheme.
- `dev` feeds the Development environment; `qa` feeds the Staging environment;
  `main` feeds Production (when it exists).

---

## Promotion: Reapplication, Never Merge

**`merge dev → qa` and `merge qa → main` do not exist in this model.**

To move a user story from one stage to the next, create a child branch from
the **destination** branch (not the source branch), and reapply the commit there
with `git cherry-pick -x`.

```bash
git switch qa && git pull
git switch -c qa/hu-07-loan-renewal
git cherry-pick -x <sha-of-commit-in-dev>
git push -u origin qa/hu-07-loan-renewal
# open PR → qa
```

**The `-x` flag is mandatory.** Reapplying a commit gives it a new SHA, so
`git merge-base` can no longer prove that what is in `qa` came from `dev`.
`-x` writes `(cherry picked from commit <sha>)` in the message, and that
trace is the only link between the two branches. Without it, in week 15
no one can prove which version of a user story reached production.

**Why this replaces our previous "Merge policy" section:**
the previous version of this document said "use Merge Commit for
`dev → qa` and `qa → main` promotions" — that directly contradicts the
course policy. It is corrected here as part of HU-DOCS-28.

---

## Release Branches

A release is cut from `main` and **filled gradually** — one commit
for each user story that has already been validated in `qa` (a fresh commit,
or a controlled `cherry-pick -x`). When complete, it enters `main`
through a Pull Request.

```
main ──┬───────────────────────────────────── PR ──> main
       └── release/1.2.0
             ├─ commit ← HU-07 (validated in qa)
             ├─ commit ← HU-09 (validated in qa)
             └─ commit ← HU-11 (validated in qa)
```

- Default cadence: **one release per MVP** — MVP1 (week 5), MVP2
  (week 10), MVP3 (week 15).
- The release Pull Request must list the user stories it includes together
  with their `cherry picked from` trace — that list is the evidence that each
  commit passed through `qa`.
- `hotfix/` is the only other child of `main`: an urgent production fix,
  reapplied afterwards to `qa` and `dev` so that the branches do not diverge.

---

## Approvals

The professor is a gate only on `main` — in both types of repository
(code and `docs`).

| Branch | Requirement |
|---|---|
| `dev` | Green CI (mandatory check) · review rule defined by the team |
| `qa` | Contract / Pact tests (mandatory check) · review rule defined by the team |
| `main` | **1 approval from `ariel5253`** + CODEOWNERS + resolved conversations + dismiss stale approvals |

CI and Pact are **automated checks, not human approvals**: they do not cost
anyone time, and they are the only objective evidence that a change passed
through its stage before being reapplied to the next one.

**Our own review rule for `dev` and `qa`** (the professor does not require this; we defined it ourselves):
- Minimum 1 teammate approval before merging.
- The reviewer has a maximum of 24 business hours to review.
- The PR must not exceed 400 lines of code (excluding tests) — if it is larger,
  split it.
- Merge only proceeds if all pipeline checks pass (green CI).

---

## Exception: The `docs` Repository

The `docs` repository **does not follow the `main/qa/dev` flow**. Only the
`main` branch exists — no `qa`, `dev`, or code branches (`feat/*`, etc.).

**How to contribute (updated in HU-DOCS-28):**

```bash
git switch main && git pull
git switch -c docs/short-name-in-english
# ... edit the documentation ...
git push -u origin docs/short-name-in-english
# open PR → main
```

```
main  ←──PR──  docs/<description>
                1 approval · ariel5253
                merge → delete the branch (optional)
```

**`main` in `docs` requires the same approval from `ariel5253`** as `main`
in the code repositories — it is not an exception to that rule, only to the
three-permanent-branch structure.

**Correction regarding the previous version of this document:** previously
it said that "it is not possible to open a PR because the repository has no
branches" and that work was done "from the `main` of the fork itself".
Both statements were incorrect under the course policy and are corrected here.
The `docs` repo does have working branches (`docs/*`); it simply does not have
the three permanent branches used by the code repositories.

**Why this three-branch exception exists (unchanged from before):**
the documentation must always be readable from `main` without ambiguity. If
intermediate branches (`dev`, `qa`) existed, anyone reading the documentation
(a teammate, the professor, an evaluator) could risk seeing outdated
information by looking at the wrong branch. With a single permanent branch,
`main` is always the single source of truth.

**What remains the same as in the other repositories:** each HU goes in a
separate PR whenever possible, with at least 1 reviewer and green CI before
merging — the only difference is that the destination is directly `main`,
not `dev`.

---

## Branch Naming Format

```
[type]/[description-in-kebab-case]

Examples:
feat/auth-jwt-login
fix/sales-stock-discount
chore/docs-adr-001-correction
docs/align-git-conventions
hotfix/auth-token-null-expiration
release/1.2.0
qa/hu-07-loan-renewal
```

---

## Commit Format (Conventional Commits)

```
[type]([scope]): [lowercase description, imperative mood, no final period]

[optional body — explains WHY, not what]

[optional footer — reference to the HU]
```

**Types:**
| Type | When to use it |
|------|----------------|
| `feat` | New functionality |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `style` | Formatting, whitespace (no logic change) |
| `refactor` | Code refactoring without behavior changes |
| `test` | Add or modify tests |
| `chore` | Tools, dependencies, CI |
| `perf` | Performance improvement |

**Examples applied to the project:**
```
feat(auth): implement JWT login

fix(sales): correct stock validation before confirming a sale
Closes HU-VEN-03

docs(requirements): unify FR/NFR identifiers and remove PDR dependency

chore(deps): upgrade Spring Boot to 3.2.0
```

---

## Pull Request Policy

- **Size:** maximum 400 lines of code (excluding tests). If it is larger, split it.
- **Reviewers:** minimum 1 approval before merging (in `dev`/`qa`); in
  `main`, the approval from `ariel5253` is additional and mandatory; it does
  not replace the team review.
- **Review time:** the reviewer has a maximum of 24 business hours.
- **Green CI:** merge only proceeds if all pipeline checks pass.

---

## Tags and Versioning

We follow [SemVer](https://semver.org/): `MAJOR.MINOR.PATCH`

```bash
# When releasing to production, after release/* is merged into main
git tag -a v1.0.0 -m "Release v1.0.0: SynkroTech sales management MVP"
git push origin v1.0.0
```

---

## Cross-References

- Non-negotiable course rule → `00-governance/branching-policy.md`
- Definition of Done → `00-governance/definition-of-done.md`
- Definition of Ready → `00-governance/definition-of-ready.md`
- Documentation rules (to be updated separately — see note below) → `00-governance/documentation-rules.md`
