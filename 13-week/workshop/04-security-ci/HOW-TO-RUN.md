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

## Real console output (CP8)

Run on my Windows machine on 2026-10-30, Go 1.27.3 (windows/amd64):

```text
> go vet ./...

> go test -v ./...
=== RUN   TestPriceChangeRejectsForgedAndUnauthorizedTokens
--- PASS: TestPriceChangeRejectsForgedAndUnauthorizedTokens (0.17s)
PASS
ok      synkro/products-authz   0.204s
```

`go vet` printed nothing, so it found no problems. The test passes against the proposed
fix: the forged ADMIN tokens get `401` and the SALESPERSON token gets `403`, which proves
the first acceptance criterion of issue #271. The same test pointed at the current
synkro-products-api would fail, because its `requireAuth` accepts any JWT-shaped token.

## Workflow validation (CP8)

```text
> go run github.com/rhysd/actionlint/cmd/actionlint@v1.7.7 security.yml

> echo %ERRORLEVEL%
0
```

actionlint 1.7.7 printed no errors and exited with code 0, so `security.yml` is a valid
GitHub Actions workflow (syntax, job `needs`, expressions and action inputs).
