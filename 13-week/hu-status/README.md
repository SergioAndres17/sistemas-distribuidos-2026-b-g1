<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       13-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 13

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Sergio Andrés Ordóñez Díaz
- GITHUB_USER: SergioAndres17
- TEAM: synkro-tech
- SPRINT_GOAL: Threat model of the synkro-api-gateway to synkro-products-api boundary and its security backlog.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| N/A — Workshop CP1 | Attack surface and trust boundaries | done | [Commit](https://github.com/SergioAndres17/sistemas-distribuidos-2026-b-g1/commit/9397231) |
| N/A — Workshop CP2 | STRIDE of gateway to products-api in synkro-docs | done | [Pull Request](https://github.com/code-corhuila/synkro-docs/pull/270) |
| #271 | Verify RS256 before changing catalog, prices or stock (P0) | todo | [Issue](https://github.com/code-corhuila/synkro-docs/issues/271) |
| #272 | Stock adjustments recorded only under the verified user (P1) | todo | [Issue](https://github.com/code-corhuila/synkro-docs/issues/272) |
| #273 | Keep the author of every price change (P2) | todo | [Issue](https://github.com/code-corhuila/synkro-docs/issues/273) |
| N/A — Workshop CP4 | Security gates and negative authorization test | done | [CI](https://github.com/SergioAndres17/sistemas-distribuidos-2026-b-g1/commit/70ddb4f), [test](https://github.com/SergioAndres17/sistemas-distribuidos-2026-b-g1/commit/006f559) |

The workshop rows are individual checkpoints. The issues are the team backlog items.

## 2. My individual contribution
- Mapped every Synkro service, store and trust boundary in a Mermaid data flow diagram.
- Analysed the boundary synkro-api-gateway to synkro-products-api with STRIDE.
- Found that synkro-products-api does not verify the RS256 signature of tokens (`middleware.go`).
- Opened three security stories with testable criteria and priority labels.
- Wrote a GitHub Actions proposal with gitleaks, govulncheck and a negative authorization test.
- Self-reviewed my STRIDE table (no teammate opened a STRIDE pull request): [review comment](https://github.com/code-corhuila/synkro-docs/pull/270/changes#r4408792964); see section 2.1.

### 2.1 Peer review (self-review)

No teammate of synkro-tech opened a STRIDE pull request during the class, so, as the
guide allows, I reviewed my own table in PR #270 against the worked example:

- **Missed threat (Tampering/Repudiation):** synkro-products-api reuses the incoming
  `X-Correlation-Id` without validating its format or length (`middleware.go`,
  `withCorrelation`) and returns it in the response header and as the `traceId` of
  every error body. A caller can send a misleading or very long value, which breaks
  the correlation of its own requests in the logs. Proposed row: validate `[A-Za-z0-9._-]{1,64}`
  or replace the value with a new UUID. I will add it to the table in CP6.
- **Probability check:** threats #6 and #1 keep probability 3, because the missing
  RS256 verification is in the current code, not only in the design.
- **Mitigation fit:** RS256 verification can be done with the Go standard library
  (`crypto/rsa`), as the module in `04-security-ci/products-authz` shows, so the
  mitigation fits the products-api stack without new dependencies.
- **History note:** the first commit of PR #270 contained a draft for another
  boundary; the second commit replaced it. It is explained in the PR description.

## 3. Blockers and risks
- The negative test runs on a minimal Go module with the proposed fix, not on synkro-products-api itself.
- The GitHub Actions workflow is a proposal in my fork; it is not in the team pipeline yet.
- Probability and impact values are my estimates and must be reviewed with the team.

## 4. Plan for next week
- Answer the review on my pull request and finish the residual risk section.
- Refine the three issues and link them to related teammate issues.
- Run the negative test and record the console output.

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [x] Testable acceptance criteria
- [x] Tests added/updated (unit / integration)
- [ ] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

The test signs tokens with a temporary RSA key created during the test; no key is committed.

## 6. Evidence links
- [Workshop files](../workshop/README.md)
- [Attack surface](../workshop/01-attack-surface.md)
- [Security CI and negative test](../workshop/04-security-ci/HOW-TO-RUN.md)
- [STRIDE pull request](https://github.com/code-corhuila/synkro-docs/pull/270)
- [Reflection document](https://docs.google.com/document/d/1PfncCPQVoNdzMJ6WIADe8nQ2siG-SQ_-dEik3usE05k/edit?usp=sharing)
