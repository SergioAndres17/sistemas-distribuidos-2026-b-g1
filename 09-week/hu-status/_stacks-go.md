# Stack: Go

> This guide is for the Go services of SynkroTech SAS: `synkro-products-api`,
> `synkro-sales-api` and `synkro-worker`. The Go release is pinned in each
> `go.mod` and is the same in the three (ADR-008).
>
> The example is the Products catalog. The concepts are in
> `05-architecture/hexagonal-architecture.md`; this guide gives the concrete
> folders, commands and code.

---

## Tools and minimum versions

| Tool | Version | Verify with |
|------|---------|------------|
| Go | The release pinned in `go.mod` | `go version` |
| Docker | 24+ | `docker --version` |
| Docker Compose | 2.20+ | `docker compose version` |

---

## Microservice folder structure (Hexagonal)

```
synkro-products-api/
├── .github/                          # common to every code repository
├── cmd/
│   └── products-api/
│       └── main.go                   # composition root: the only place that knows every concrete type
├── deploy/
│   ├── compose.yml                   # declares this service; the database instance belongs to synkro-infra-postgres (ADR-009)
│   └── Dockerfile
├── internal/
│   ├── adapter/
│   │   ├── in/
│   │   │   └── httpapi/              # inbound adapter: HTTP
│   │   │       ├── auth.go           # verifies the RS256 token
│   │   │       ├── errors.go         # error envelope; the only place that maps a domain error to a status
│   │   │       ├── handler.go        # routes, validation, response objects
│   │   │       ├── handler_test.go
│   │   │       └── middleware.go     # correlation id, access log
│   │   └── out/
│   │       └── persistence/          # outbound adapter: PostgreSQL
│   │           ├── memory.go         # in-memory repository, used by the HTTP tests
│   │           ├── postgres.go
│   │           └── postgres_integration_test.go
│   ├── application/
│   │   ├── port/
│   │   │   ├── in/
│   │   │   │   └── products.go       # use case interfaces, commands and results
│   │   │   └── out/
│   │   │       └── ports.go          # repository and id generator interfaces
│   │   └── usecase/
│   │       ├── products.go
│   │       └── products_test.go
│   ├── config/
│   │   └── config.go                 # environment variables and explicit limits
│   └── domain/
│       └── model/
│           └── product.go            # entities, invariants, typed errors
├── .env.example
├── .gitignore
├── go.mod
├── go.sum
└── README.md
```

**Dependency rule:** `internal/domain` imports only the Go standard library;
`internal/application` imports the domain; the adapters import the ports and the
domain; only `cmd/` and `internal/config` know all of them. In Go this is
verified by reading the imports of each package.

| Piece | Folder |
|---|---|
| Domain | `internal/domain/model/` |
| Inbound ports | `internal/application/port/in/` |
| Outbound ports | `internal/application/port/out/` |
| Use cases | `internal/application/usecase/` |
| HTTP adapter | `internal/adapter/in/httpapi/` |
| Persistence adapter | `internal/adapter/out/persistence/` |
| Composition root | `cmd/products-api/main.go` and `internal/config/` |

---

## Libraries

The Go services use the standard library wherever it is enough (`net/http`,
`log/slog`, `crypto/rsa`, `encoding/json`, `database/sql`). The worker is fixed
to the standard library by ADR-008 Decision 3.

```go
module github.com/code-corhuila/synkro-products-api

go 1.xx // the release pinned for the three Go services (ADR-008)

require (
	github.com/go-playground/validator/v10 v10.x // input validation in the HTTP adapter
	github.com/jackc/pgx/v5 v5.x                 // PostgreSQL driver, used through database/sql
)
```

- **Fixed by the documents:** structured JSON logs with `slog`
  (`05-architecture/cross-cutting.md` §2), a connection pool configured on
  `database/sql` (§8), a token verifier with a closed list of algorithms
  (ADR-006) and input validated with `go-playground/validator` before the use
  case is invoked (`00-governance/security-rules.md`).
- **Default, not fixed by any ADR:** the `pgx` driver, and `testify` in tests.
  Adding a router or a framework is a team decision.
- **Not used:** a message broker client (ADR-007 Decision 5) and a migration
  library. Migrations live in the `-db` repository and use Flyway (ADR-005
  Decision 2); the service never migrates at startup.

---

## Configuration and explicit limits

The service reads its configuration from environment variables
(`05-architecture/deployment.md` §6) and declares its limits in the composition
root, with their value.

| Variable | Example |
|---|---|
| `DATABASE_URL` | `postgres://products_app:${PRODUCTS_APP_PASSWORD}@synkro-db:5432/synkro?search_path=products_schema&sslmode=disable` |
| `JWT_PUBLIC_KEY` | PEM on one line, with literal `\n` |
| `HTTP_PORT` | `8080` |

```go
// cmd/products-api/main.go (excerpt)
// http.ListenAndServe leaves the server times at zero, which means no limit.
srv := &http.Server{
	Addr:              ":" + cfg.HTTPPort,
	Handler:           handler,
	ReadHeaderTimeout: 5 * time.Second,
	WriteTimeout:      15 * time.Second,
	IdleTimeout:       60 * time.Second,
}
db.SetMaxOpenConns(10) // pool size; each query also carries a context with a deadline
```

Values and the graceful shutdown (`srv.Shutdown(ctx)`, 20 s) are explained in
`05-architecture/cross-cutting.md` §8.

---

## Example: create a product

The use case receives a command, builds the entity (which applies its
invariants) and asks the repository to save the product and its idempotency key
in **one transaction**. If the key was already used, the repository keeps nothing
and returns the original product.

**Domain — `internal/domain/model/product.go`**

```go
package model

import "errors"

var (
	ErrNameRequired     = errors.New("name is required")
	ErrPriceNotPositive = errors.New("price must be greater than 0")
)

type Product struct {
	ID         string
	Name       string
	PriceCents int64 // minor units, 1/100 COP (ADR-005 Decision 3)
	Stock      int
	CategoryID string
	Active     bool
}

// NewProduct applies the invariants of 02-domain/entities-and-rules.md.
// A new product starts with stock 0.
func NewProduct(id, name string, priceCents int64, categoryID string) (Product, error) {
	if name == "" {
		return Product{}, ErrNameRequired
	}
	if priceCents <= 0 {
		return Product{}, ErrPriceNotPositive
	}
	return Product{ID: id, Name: name, PriceCents: priceCents, CategoryID: categoryID, Active: true}, nil
}
```

**Ports — `internal/application/port/in/products.go` and `port/out/ports.go`**

```go
package in

import "context"

type CreateProductCommand struct {
	IdempotencyKey string
	Name           string
	PriceCents     int64
	CategoryID     string
}

type CreateProductResult struct {
	ProductID string
	Created   bool // false when the Idempotency-Key was already used
}

type ProductUseCases interface {
	Create(ctx context.Context, cmd CreateProductCommand) (CreateProductResult, error)
}
```

```go
package out

import (
	"context"

	"github.com/code-corhuila/synkro-products-api/internal/domain/model"
)

type ProductRepository interface {
	// CreateOnce inserts the product and the idempotency key in one transaction.
	// If the key already exists it rolls back and returns the original product id.
	CreateOnce(ctx context.Context, key string, p model.Product) (id string, created bool, err error)
}

type IDGenerator interface {
	NewID() string
}
```

**Use case — `internal/application/usecase/products.go`**

```go
package usecase

import (
	"context"

	"github.com/code-corhuila/synkro-products-api/internal/application/port/in"
	"github.com/code-corhuila/synkro-products-api/internal/application/port/out"
	"github.com/code-corhuila/synkro-products-api/internal/domain/model"
)

type Products struct {
	repo out.ProductRepository
	ids  out.IDGenerator
}

func NewProducts(repo out.ProductRepository, ids out.IDGenerator) *Products {
	return &Products{repo: repo, ids: ids}
}

func (u *Products) Create(ctx context.Context, cmd in.CreateProductCommand) (in.CreateProductResult, error) {
	product, err := model.NewProduct(u.ids.NewID(), cmd.Name, cmd.PriceCents, cmd.CategoryID)
	if err != nil {
		return in.CreateProductResult{}, err // typed domain error; the HTTP adapter maps it
	}
	id, created, err := u.repo.CreateOnce(ctx, cmd.IdempotencyKey, product)
	if err != nil {
		return in.CreateProductResult{}, err
	}
	return in.CreateProductResult{ProductID: id, Created: created}, nil
}
```

The real use case is longer: it also checks, through a second port, that the
category exists and is active.

**Error mapping — `internal/adapter/in/httpapi/errors.go`**

```go
package httpapi

import (
	"errors"
	"net/http"

	"github.com/code-corhuila/synkro-products-api/internal/domain/model"
)

// statusFor is the only place that knows what an HTTP status is.
// The domain throws typed errors and never learns what a 422 means.
func statusFor(err error) (status int, code string) {
	switch {
	case errors.Is(err, model.ErrNameRequired), errors.Is(err, model.ErrPriceNotPositive):
		return http.StatusUnprocessableEntity, "BUSINESS_RULE_VIOLATION"
	default:
		return http.StatusInternalServerError, "INTERNAL_ERROR" // neutral message; the detail goes only to the log
	}
}
```

**Test written first — `internal/application/usecase/products_test.go`**

The test needs no server and no database: it uses a hand-written fake of the port.

```go
package usecase

import (
	"context"
	"errors"
	"fmt"
	"testing"

	"github.com/code-corhuila/synkro-products-api/internal/application/port/in"
	"github.com/code-corhuila/synkro-products-api/internal/domain/model"
)

// fakeProducts is a hand-written fake of the ProductRepository port.
```

---

## Sales: the same shape, a different persistence adapter

`synkro-sales-api` follows this exact same folder layout, dependency rule and
test-first flow — only `internal/adapter/out/persistence/` changes. Instead of
`postgres.go` built on `database/sql`, it has a `mongo.go` built on the official
MongoDB Go driver, because Sales' database is a MongoDB instance, not a schema
in the shared PostgreSQL one (ADR-010).

```go
module github.com/code-corhuila/synkro-sales-api

go 1.xx // same release as the other two Go services (ADR-008)

require (
	github.com/go-playground/validator/v10 v10.x
	go.mongodb.org/mongo-driver/v2 v2.x          // MongoDB driver, in place of pgx
)
```

What stays identical to Products: the domain package imports nothing beyond the
standard library, the use cases depend only on the ports, and `memory.go` — an
in-memory fake of `SaleRepository` — is what the HTTP adapter's tests run
against, exactly like `memory.go` in Products. What changes:

| Piece | Products (PostgreSQL) | Sales (MongoDB) |
|---|---|---|
| Connection variable | `DATABASE_URL` | `MONGODB_URI` (`deployment.md` §6) |
| Persistence file | `postgres.go`, over `database/sql` | `mongo.go`, over the MongoDB driver |
| Idempotent creation | Insert the resource and its key in one SQL transaction | Insert the sale document, with `idempotencyKey` as one of its fields, in one atomic write; a duplicate-key error on the unique index means the key was already used (ADR-010 Decision 3) |
| Integration test target | A PostgreSQL schema reachable at `TEST_DATABASE_URL` | A MongoDB replica set reachable at `TEST_MONGODB_URI`; skipped when the variable is absent, same convention as the PostgreSQL services |

---

## `synkro-worker`: the same shape with another inbound adapter

The worker has the hexagonal shape of an API, but its inbound adapter is a
scheduler and it has no database and no HTTP endpoint. It reaches the other
domains through their API with its service token, never through their database
(ADR-007 Decision 6).

```
synkro-worker/
├── .github/
├── cmd/
│   └── worker/
│       └── main.go
├── deploy/
│   ├── compose.yml
│   └── Dockerfile
├── internal/
│   ├── adapter/
│   │   ├── in/
│   │   │   └── scheduler/
│   │   │       └── scheduler.go      # runs the job every LOW_STOCK_EVERY
│   │   └── out/
│   │       └── products/
│   │           ├── client.go         # HTTP client of synkro-products-api, with the service token
│   │           └── client_test.go
│   ├── application/
│   │   ├── port/
│   │   │   ├── in/
│   │   │   │   └── job.go
│   │   │   └── out/
│   │   │       └── products.go
│   │   └── usecase/
│   │       ├── low_stock_alerts.go
│   │       └── low_stock_alerts_test.go
│   ├── config/
│   │   └── config.go
│   └── correlation/
│       └── correlation.go            # one correlation id per run
├── .env.example
├── go.mod
└── README.md
```

Variables of the worker (`deployment.md` §6): `SERVICE_TOKEN`, `PRODUCTS_API_URL`,
`LOW_STOCK_EVERY`, `LOW_STOCK_THRESHOLD`, `BATCH_SIZE`, `RUN_TIMEOUT`,
`HTTP_TIMEOUT`, `HTTP_ATTEMPTS`. Backoff, jitter and the run timeout are written by
hand and covered by its tests (ADR-008 Decision 3).

---

## Project commands

| Task | Command |
|------|---------|
| Run locally | `go run ./cmd/products-api` |
| Unit and HTTP tests | `go test ./...` |
| Coverage | `go test -coverprofile=coverage.out ./...` and `go tool cover -func=coverage.out` |
| Integration tests | `TEST_DATABASE_URL=postgres://… go test ./...` (Sales: `TEST_MONGODB_URI=mongodb://… go test ./...`) — skipped when the variable is not set |
| Static checks | `go vet ./...` |
| Vulnerability scan | `govulncheck ./...` — before every release (`security-rules.md`, A06) |
| Build | `go build -o bin/products-api ./cmd/products-api` |

`TEST_DATABASE_URL` / `TEST_MONGODB_URI` point to an instance that already has
the schema or validator of the `-db` repository (`testing-strategy.md`, Tier 2).
Starting the whole system is described in `05-architecture/deployment.md` §9 and
`10-devops/local-setup.md`.

---

## Naming conventions (Go)

| Artifact | Convention | Example |
|----------|-----------|---------|
| Interfaces | `PascalCase` (no `I` prefix) | `ProductRepository` |
| Structs | `PascalCase` | `CreateProductCommand` |
| Files | `snake_case.go` | `low_stock_alerts.go` |
| Variables and functions | `camelCase` | `idByKey`, `statusFor` |
| Constants and errors | `PascalCase` if exported | `ErrPriceNotPositive` |
| Packages | `lowercase`, no hyphens | `model`, `usecase`, `httpapi`, `persistence` |
| Money | `int64`, name ending in `Cents` | `PriceCents` |

---

## Correlations

- Hexagonal concepts and where each layer lives → `05-architecture/hexagonal-architecture.md`
- Test levels, thresholds and CI → `11-quality/testing-strategy.md`
- TDD cycle → `11-quality/tdd-guide.md`
- Migrations and the database instances → `05-architecture/deployment.md` §4, §5, ADR-005 Decision 2, ADR-009, ADR-010 Decision 5, ADR-012
- Token validation and service tokens → ADR-006
- Worker and saga → ADR-007
- Versions and stack → ADR-008
- Sales domain on MongoDB → ADR-010
- Contract of this example → `07-api/contracts/openapi/synkro-products-api.yaml`
