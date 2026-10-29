# Security gates and negative authorization test

## What it covers

- Story: [security] only a verified ADMIN or INVENTORY token can change catalog,
  prices and stock in synkro-products-api (STRIDE threat #6, Elevation of privilege,
  risk 9, P0). Issue: https://github.com/code-corhuila/synkro-docs/issues/271
- Acceptance criterion: `PUT /api/v1/products/{id}` answers `401` for a token whose
  payload was edited (`alg: none` or signed with another key) and `403` for a valid
  SALESPERSON token.
- Today synkro-products-api only checks the JWT shape (`middleware.go`), so the
  forged ADMIN token would get `200`. `products-authz/` is a minimal Go module with
  the proposed fix: RS256 verification first, then the same `canWriteCatalog` rule.

## Run the test locally

Requires Go 1.22 or later.

```shell
cd 13-week/workshop/04-security-ci/products-authz
go test -v ./...
```

The test creates the Auth key and an attacker key during the run (no key is committed)
and sends five requests: no token (401), edited payload with `alg: none` (401),
ADMIN token signed by the attacker (401), SALESPERSON (403) and INVENTORY (200).

## Pipeline

`security.yml` runs on every pull request: gitleaks (secrets), govulncheck
(dependencies) and the negative test. The test job needs the other two, so a red step
blocks the merge. In the team it belongs in `.github/workflows/` of synkro-products-api,
pointing at `internal/adapter/in/httpapi`; here it is a proposal and stays in this folder.
