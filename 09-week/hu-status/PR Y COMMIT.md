

**Rama:**
```
docs/align-workflow-saga-contract
```

**Commit:**
```
docs(api): rewrite workflow contract for the saga resource with persisted state
```

**Título del PR:**
```
docs(api): rewrite workflow contract for the saga resource with persisted state
```

**Descripción del PR:**

## Description

### Summary

Rewrites `synkro-workflow.yaml` from scratch to match ADR-007
Decision 2 (saga as a resource with idempotent steps) and ADR-004
Decision 6 (endpoint register). Replaces the pre-ADR-007 contract that
still routed to `POST /api/sales`, returned the Sale object directly,
had no idempotency, no GET for saga state, and referenced ADR-003's
stateless orchestration.

### Changes

- **POST /api/v1/sagas/register-sale**: starts the saga with the
  person's token, Idempotency-Key, customerId and lines (no prices —
  resolved by step 2). Answers 201 with the saga (COMPLETED if steps
  finished, RUNNING if timeout). Same key answers 200 idempotently.
- **GET /api/v1/sagas/{id}**: returns the saga's external state
  (status, completedSteps, failedStep, saleId). Never exposes internal
  fields (input, step_results, error_detail).
- **SagaResponse schema**: 4 statuses (RUNNING, COMPLETED,
  COMPENSATED, FAILED) matching the CHECK constraint in models.md's
  saga_instance table.
- **Health check**: documents connectivity to workflow_schema in the
  shared instance (ADR-009).
- **Server URL**: synkro-workflow:8080 inside the platform network.
- **Stack**: Java / Spring Boot (ADR-008), no longer TBD.

### What was removed

- `POST /api/sales` route — replaced by `/api/v1/sagas/register-sale`
- Direct `Sale` response from sales-api — replaced by `SagaResponse`
- References to ADR-003 stateless orchestration — superseded by ADR-007
- References to outbox and SaleCompleted — removed by ADR-007 Decision 5
- TBD stack note — resolved by ADR-008

### Definition of Done

- [x] Contract contains POST /api/v1/sagas/register-sale and GET /api/v1/sagas/{id}
- [x] POST requires person's Bearer token and Idempotency-Key
- [x] Response schema includes status (RUNNING, COMPLETED, COMPENSATED, FAILED) and per-step status
- [x] GET response includes failedStep and error when status is COMPENSATED or FAILED
- [x] Schemas reference _shared.yaml for common error and pagination components
- [x] Reviewed and approved by Angel (Tech Lead) and Santiago

**Reviewers:** Angel (Tech Lead, validates consistency with other contracts) and Santiago (reviewed deployment where the workflow configuration is located).

Closes HU-DOCS-60 (part of HU-11).
