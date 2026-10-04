## HU-DOCS-73 — Reescribe el contrato de `synkro-customers-api`

- `07-api/contracts/openapi/synkro-customers-api.yaml` (reescrito)


| Campo | Valor |
|---|---|
| Rama | `docs/rewrite-customers-api-contract` |
| Responsable | Sergio |
| Depende de | Ninguna |
| Prioridad | Must Have |

**Rama y commit**

```
docs/rewrite-customers-api-contract
```

```
docs(api): rewrite the Customers contract with the ADR-004 customer list

Add the customer list with identity-document filter and move the contract to /api/v1 with the common conventions.
```

**Descripción del PR**

```markdown
## Description

### Summary

Rewrites `synkro-customers-api.yaml` (v1.0.0, written before ADR-004)
with the customer list of ADR-004 Decision 1 and the common conventions.
The old contract still carried a "KNOWN GAP" about the point-of-sale
lookup by identity document, which ADR-004 resolved, and ADR-004 states
that no contract keeps a known gap.

### Changes

- **`GET /customers`**: paginated list with the filters
  `identityDocument` (exact match) and `active`; unknown filters and
  out-of-range limits answer `400 VALIDATION_ERROR`. Without `active`,
  active and inactive customers are listed, so the point of sale can see
  that a customer exists but is inactive.
- **`POST /customers`**: requires `Idempotency-Key` (201 with
  `Location`, 200 on a repeat). A duplicate identity document is
  `422 BUSINESS_RULE_VIOLATION`; the old `409` and
  `IDENTITY_DOCUMENT_ALREADY_EXISTS` are gone.
- **`GET`, `PUT`, `DELETE /customers/{id}`**: a missing or inactive
  customer answers 404; deactivating an already inactive customer
  answers 200 (a transition already done, per `guidelines.md`).
  `PUT` also answers 422 for an identity document of another customer.
- **Security**: ADMIN and SALESPERSON use every operation; INVENTORY
  receives 403; the workflow's token holds `customers:read` for saga
  step 1.
- **Server and routes**: `http://synkro-customers-api:8080/api/v1`
  with `/customers` paths, replacing `localhost:8082/api/customers` and
  the `/` and `/{id}` routes. The schema `Customer` is now
  `CustomerResponse`.

### Definition of Done

- [x] The contract contains create, list, get, update, deactivate and health under `/api/v1/customers`
- [x] The list is paginated and supports `identityDocument` and `active` (ADR-004 Decision 1); the "KNOWN GAP" text is gone
- [x] Every error code belongs to the closed catalog; unknown filters and out-of-range limits answer `400 VALIDATION_ERROR`
- [x] Roles are documented: ADMIN and SALESPERSON; INVENTORY answers 403; the workflow's token holds `customers:read`
- [x] Schema limits match `06-data/models.md`
- [x] Deactivating an already inactive customer answers `200`; a get or update of a missing or inactive customer answers `404`; the list includes inactive customers unless `active` is given
- [x] The server URL is `http://synkro-customers-api:8080/api/v1`
- [x] Reviewed and approved by Jordan (co-author of ADR-004) and Santiago

**Reviewers:** Jordan, who is a co-author of ADR-004, and Santiago.

Closes HU-DOCS-73 (part of HU-13).
```