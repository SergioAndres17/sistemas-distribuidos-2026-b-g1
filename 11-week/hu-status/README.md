<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       11-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 11

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Sergio Andrés Ordóñez Díaz
- GITHUB_USER: SergioAndres17
- TEAM: synkro-tech
- SPRINT_GOAL: Document and test observability for the Synkro sale-registration journey.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| N/A — Workshop CP1 | Sale-registration journey and trace map | done | [Commit](https://github.com/SergioAndres17/sistemas-distribuidos-2026-b-g1/commit/d6f0df993174f60057ae57de954185febc531717) |
| N/A — Workshop CP2 | Structured log contract | done | [Commit](https://github.com/SergioAndres17/sistemas-distribuidos-2026-b-g1/commit/d6aaf538df8c8ff646b891c7ead0e0a3fb557718) |
| N/A — Workshop CP3 | Trace propagation, metrics and automated tests | done | [First push](https://github.com/SergioAndres17/sistemas-distribuidos-2026-b-g1/commit/6caa01b53d193f25a30d662cd0c331b7b6121e6d), [second push](https://github.com/SergioAndres17/sistemas-distribuidos-2026-b-g1/commit/c146fc889fe0125d4248bef0a7c269c5b73d3a6f) |
| N/A — Workshop CP4 | Golden signals and metric catalog | done | [Commit](https://github.com/SergioAndres17/sistemas-distribuidos-2026-b-g1/commit/c0dde9fd112bcf8bc01373d090015271fcd5ffe3) |
| N/A — Workshop CP6 | SLOs and error budget for register-sale | done | [Commit](CP6_COMMIT_URL) |
| N/A — Workshop CP7 | Burn-rate alert rules and alert policy | done | [Rules](CP7_RULES_COMMIT_URL), [policy](CP7_POLICY_COMMIT_URL) |
| N/A — Workshop CP8 | Runbook for the fast-burn alert | done | [Commit](CP8_COMMIT_URL) |
| N/A — Workshop CP9 | Incident drill and blameless postmortem | done | [Commit](CP9_COMMIT_URL) |

These are individual workshop checkpoints, not newly assigned team HU identifiers.
Done refers to the technical artifact, not grading or full-platform acceptance.

## 2. My individual contribution
- Mapped sale registration through Gateway, Workflow, Customers, Products and Sales.
- Defined JSON log fields, domain fields and rules to avoid exposing sensitive data.
- Prepared a Java/Spring Boot workshop module with trace-context handling.
- Added an outgoing HTTP call to a controlled Customers test server.
- Added a request counter and a duration histogram exposed at /metrics.
- Added tests for trace propagation, metrics and downstream failure.
- Documented proposed metrics separately from those implemented in the module.
- Estimated register-sale traffic and defined availability, latency and stock reservation SLOs.
- Wrote burn-rate alert rules and checked them with promtool, including a rule unit test.
- Wrote the fast-burn runbook with the mitigations Synkro supports today.
- Ran a drill of a slow Customers lookup and wrote a blameless postmortem with three HUs.

## 3. Blockers and risks
- The module is a local instrumentation exercise, not the complete Workflow service.
- It does not implement authentication, persistence or actual sale registration.
- Customers is represented by a controlled HTTP server during the tests.
- Instrumentation across the real platform remains pending.
- Prometheus dashboards and proposed saturation metrics were not implemented.
- The traffic estimate is an assumption; no real sales data exists (NFR-001).
- The alert rules were validated with promtool only, not loaded in a running Prometheus.
- Synkro has no feature flags or circuit breaker, so the runbook cannot use them.
- Submission status and reflection sharing must be verified separately.

## 4. Plan for next week
- Propose HU-OBS-01 to HU-OBS-03 from the postmortem to the team.
- Bring the trace filter and metrics from the workshop module to the real synkro-workflow.
- Ask the team to load the alert rules in the Prometheus of synkro-infra-postgres.

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [x] Testable acceptance criteria
- [x] Tests added/updated (unit / integration)
- [ ] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

The workshop uses the personal course fork and separate checkpoint pushes.
No team environment branch or PR was created for this exercise.
The module demonstrates instrumentation; it does not demonstrate full DDD architecture.
Configuration uses Spring properties and supports environment overrides.

## 6. Evidence links
- [Workshop files](../workshop/README.md)
- [Journey and trace map](../workshop/01-journey.md)
- [Log contract](../workshop/02-log-contract.md)
- [Instrumentation instructions](../workshop/03-instrumentation/HOW-TO-RUN.md)
- [Automated tests](../workshop/03-instrumentation/src/test/java/co/edu/corhuila/synkro/WorkflowWorkshopTest.java)
- [Metric catalog](../workshop/04-metrics.md)
- [SLOs and error budget](../workshop/05-slo.md)
- [Alert rules](../workshop/06-alerts.yml) and [alert policy](../workshop/06-alert-policy.md)
- [Runbook](../workshop/07-runbook.md)
- [Incident drill and postmortem](../workshop/08-incident.md)
- [Personal reflection](https://docs.google.com/document/d/1m6I_DHEyFGBS9Qatmg6dprfiL6rRGgTfKo4TXa0CkXc/edit)

Local CP3 test report:
```text
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
```

The report verifies the workshop module, not the complete distributed platform.

CP7 rule check:
```text
promtool check rules 06-alerts.yml      SUCCESS: 10 rules found
promtool test rules 06-alerts.test.yml  SUCCESS
```