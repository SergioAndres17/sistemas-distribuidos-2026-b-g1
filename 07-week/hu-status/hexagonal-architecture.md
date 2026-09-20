# Hexagonal Architecture (Ports and Adapters)

> This document closes Pillar 7. Until now, "hexagonal architecture" was
> only mentioned in passing in `overview.md` and cited by name from
> `CONTRIBUTING.md` and `technical-onboarding.md` — but the file itself
> never existed. This is that file.
>
> **Note on the example used:** with ADR-003 already approved, sale
> registration moves to a Saga orchestrated by `synkro-workflow`. Inside
> `sales-service`, what remains is simpler and is actually a better
> single-domain example: receiving an already-validated sale (active
> customer, stock already reserved by `workflow`) and persisting it,
> also writing the Outbox event in the same transaction. That is the use
> case used below.

---

## Why this separation exists

Pillar 7 requires that domain code **never import a framework**. The
reason isn't dogma — it's that business rules (can a sale's `total`
differ from the sum of its lines? can stock go negative?) shouldn't stop
compiling just because the team decides to switch from Spring Boot to a
different framework, or from one relational database to another.

If the domain imports JPA's `@Entity`, or a Go `sql.DB` directly, the
business rules become tied to an infrastructure decision that has
nothing to do with the business itself.

---

## The 3 layers

```
┌─────────────────────────────────────────────────────────┐
│                     INFRASTRUCTURE                        │
│   (the only place that touches I/O: HTTP, SQL, queues)    │
│                                                             │
│   REST Controllers · Repositories · HTTP Clients            │
│   ┌───────────────────────────────────────────────┐       │
│   │                 APPLICATION                     │       │
│   │   (orchestrates the domain, defines what it needs) │    │
│   │                                                  │       │
│   │   Use Cases · Ports (interfaces)                │       │
│   │   ┌─────────────────────────────────────┐      │       │
│   │   │              DOMAIN                   │      │       │
│   │   │  (business rules, nothing else)       │      │       │
│   │   │                                        │      │       │
│   │   │   Entities · Invariants               │      │       │
│   │   └─────────────────────────────────────┘      │       │
│   └───────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────────┘

Dependency rule: import arrows only point inward.
Infrastructure → Application → Domain.  Never the other way around.
```

### Domain

Contains the entities and their invariants — exactly what is already
documented in `02-domain/entities-and-rules.md`, now expressed as code.
**It imports nothing external**: no web framework, no database driver,
no HTTP library. If `sales-service`'s domain can compile without
PostgreSQL or Gin/Echo installed, it's done right.

### Application

Contains the **use cases** — the orchestration of a complete operation —
and defines the **ports**: interfaces that state *what* the use case
needs, without saying *how* it's implemented. An outbound port
(`SaleRepository`) says "I need to be able to save a sale", without
knowing whether that means PostgreSQL, a file, or memory.

### Infrastructure

The **adapters** — the only classes/structs that actually touch I/O.
They split into two directions:

- **Inbound adapters**: receive something from outside and call the use
  case. Example: a REST controller.
- **Outbound adapters**: implement a port the use case needs. Example: a
  PostgreSQL repository that implements `SaleRepository`.

---

## Ports and adapters, in code

A **port** is an interface defined in the Application layer:

**Go:**
```go
// application/ports.go
package application

type SaleRepository interface {
    Save(ctx context.Context, sale *domain.Sale) error
}
```

**Java:**
```java
// application/port/out/SaleRepository.java
public interface SaleRepository {
    void save(Sale sale);
}
```

An **adapter** implements that port, in the Infrastructure layer:

**Go:**
```go
// infrastructure/postgres/sale_repository.go
package postgres

type PostgresSaleRepository struct {
    db *sql.DB
}

func (r *PostgresSaleRepository) Save(ctx context.Context, sale *domain.Sale) error {
    // the only place in the service that writes real SQL
    tx, err := r.db.BeginTx(ctx, nil)
    // ...
}
```

**Java:**
```java
// infrastructure/persistence/PostgresSaleRepository.java
@Repository
public class PostgresSaleRepository implements SaleRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(Sale sale) {
        // the only place in the service that writes real SQL
    }
}
```

**The rule this enforces:** the use case (Application) never mentions
`sql.DB`, `JdbcTemplate`, or any PostgreSQL detail — it only knows the
`SaleRepository` interface. A use-case test can be written with an
in-memory `FakeSaleRepository`, with no database running. This is what
makes Pillar 2 (TDD) possible: testing business logic without real
infrastructure.

---

## Complete example: `RegisterSaleUseCase`

Walks through the 3 layers for the use case that remains in
`sales-service` after ADR-003 — receiving a sale already validated by
`synkro-workflow` (Saga step 3) and persisting it together with its
Outbox event, in the same transaction.

**Shown in both languages on purpose**, for comparison — even though in
reality `sales-service` is Go only; the Java version is a comparison
exercise, not a real service.

### 1. Domain — the `Sale` entity with its invariants

**Go:**
```go
// domain/sale.go
package domain

import "errors"

type Sale struct {
    ID         string
    CustomerID string
    CreatedBy  string
    Details    []SaleDetail
    Total      float64
}

// NewSale applies the invariants from entities-and-rules.md when constructing.
func NewSale(customerID, createdBy string, details []SaleDetail) (*Sale, error) {
    if len(details) == 0 {
        return nil, errors.New("a sale must have at least 1 detail line")
    }
    total := 0.0
    for _, d := range details {
        if d.Quantity <= 0 {
            return nil, errors.New("quantity must be greater than 0")
        }
        expectedSubtotal := d.UnitPrice * float64(d.Quantity)
        if d.Subtotal != expectedSubtotal {
            return nil, errors.New("subtotal must equal quantity * unitPrice")
        }
        total += d.Subtotal
    }
    return &Sale{CustomerID: customerID, CreatedBy: createdBy, Details: details, Total: total}, nil
}
```

**Java:**
```java
// domain/Sale.java
public class Sale {
    private final String customerId;
    private final String createdBy;
    private final List<SaleDetail> details;
    private final BigDecimal total;

    // The constructor applies the same invariants — throws a domain
    // exception, not a framework exception, if something doesn't hold.
    public Sale(String customerId, String createdBy, List<SaleDetail> details) {
        if (details.isEmpty()) {
            throw new InvalidSaleException("a sale must have at least 1 detail line");
        }
        BigDecimal computedTotal = BigDecimal.ZERO;
        for (SaleDetail d : details) {
            if (d.getQuantity() <= 0) {
                throw new InvalidSaleException("quantity must be greater than 0");
            }
            BigDecimal expected = d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity()));
            if (d.getSubtotal().compareTo(expected) != 0) {
                throw new InvalidSaleException("subtotal must equal quantity * unitPrice");
            }
            computedTotal = computedTotal.add(d.getSubtotal());
        }
        this.customerId = customerId;
        this.createdBy = createdBy;
        this.details = details;
        this.total = computedTotal;
    }
}
```

**Why this is pure domain:** neither version imports anything from HTTP,
SQL, or a framework. `InvalidSaleException` is the domain's own
exception, not a Spring exception or an HTTP library exception.

### 2. Application — the use case and the port

**Go:**
```go
// application/register_sale.go
package application

type RegisterSaleUseCase struct {
    repo SaleRepository // port, not the concrete implementation
}

func (uc *RegisterSaleUseCase) Execute(ctx context.Context, cmd RegisterSaleCommand) (*domain.Sale, error) {
    sale, err := domain.NewSale(cmd.CustomerID, cmd.CreatedBy, cmd.Details)
    if err != nil {
        return nil, err // domain error, propagated as-is
    }
    if err := uc.repo.Save(ctx, sale); err != nil {
        return nil, err
    }
    return sale, nil
}
```

**Java:**
```java
// application/usecase/RegisterSaleUseCase.java
@Service
public class RegisterSaleUseCase {
    private final SaleRepository repository; // port, not the implementation

    public RegisterSaleUseCase(SaleRepository repository) {
        this.repository = repository;
    }

    public Sale execute(RegisterSaleCommand cmd) {
        Sale sale = new Sale(cmd.getCustomerId(), cmd.getCreatedBy(), cmd.getDetails());
        repository.save(sale);
        return sale;
    }
}
```

**What this layer does, and does NOT do:** it orchestrates (creates the
entity, calls the repository) but contains no business rule of its own —
those were already applied inside `domain.NewSale(...)` / `new
Sale(...)`. If tomorrow the use case also needs to write the Outbox
event (ADR-003 §5), a second port (`EventPublisher`) is added and called
here, without touching the domain.

### 3. Infrastructure — the inbound adapter (REST) and the outbound adapter (Postgres + Outbox)

**Go — inbound adapter:**
```go
// infrastructure/http/sale_controller.go
package http

func (h *SaleHandler) RegisterSale(w http.ResponseWriter, r *http.Request) {
    var req registerSaleRequest
    json.NewDecoder(r.Body).Decode(&req)

    cmd := application.RegisterSaleCommand{
        CustomerID: req.CustomerID,
        CreatedBy:  req.CreatedBy, // comes from the X-User-Id header the Gateway forwards
        Details:    req.toDomainDetails(),
    }

    sale, err := h.useCase.Execute(r.Context(), cmd)
    if err != nil {
        writeError(w, err) // maps the domain error to an HTTP status code
        return
    }
    writeJSON(w, http.StatusCreated, sale)
}
```

**Go — outbound adapter, with Outbox in the same transaction:**
```go
// infrastructure/postgres/sale_repository.go
package postgres

func (r *PostgresSaleRepository) Save(ctx context.Context, sale *domain.Sale) error {
    tx, err := r.db.BeginTx(ctx, nil)
    if err != nil {
        return err
    }
    defer tx.Rollback()

    if _, err := tx.ExecContext(ctx,
        `INSERT INTO sales (sale_id, customer_id, created_by, total) VALUES ($1, $2, $3, $4)`,
        sale.ID, sale.CustomerID, sale.CreatedBy, sale.Total); err != nil {
        return err
    }

    // Outbox (ADR-003 §5): same commit, same transaction.
    payload, _ := json.Marshal(sale)
    if _, err := tx.ExecContext(ctx,
        `INSERT INTO outbox (event_type, payload) VALUES ('SaleCompleted', $1)`,
        payload); err != nil {
        return err
    }

    return tx.Commit()
}
```

**Java — the same pair of adapters:**
```java
// infrastructure/rest/SaleController.java
@RestController
@RequestMapping("/api/sales")
public class SaleController {
    private final RegisterSaleUseCase useCase;

    @PostMapping
    public ResponseEntity<SaleResponse> registerSale(
            @RequestBody RegisterSaleRequest req,
            @RequestHeader("X-User-Id") String createdBy) {
        RegisterSaleCommand cmd = req.toCommand(createdBy);
        Sale sale = useCase.execute(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(SaleResponse.from(sale));
    }

    @ExceptionHandler(InvalidSaleException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSale(InvalidSaleException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }
}
```

```java
// infrastructure/persistence/PostgresSaleRepository.java
@Repository
public class PostgresSaleRepository implements SaleRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional // Spring guarantees both INSERTs share one transaction
    public void save(Sale sale) {
        jdbcTemplate.update(
            "INSERT INTO sales (sale_id, customer_id, created_by, total) VALUES (?, ?, ?, ?)",
            sale.getId(), sale.getCustomerId(), sale.getCreatedBy(), sale.getTotal());

        // Outbox (ADR-003 §5): same transaction, thanks to @Transactional
        jdbcTemplate.update(
            "INSERT INTO outbox (event_type, payload) VALUES ('SaleCompleted', ?::jsonb)",
            toJson(sale));
    }
}
```

**What makes this "infrastructure":** it's the only code across all 6
snippets that mentions `sql.DB`, `JdbcTemplate`, `@RestController`, or
JSON — everything that touches the outside world lives here, and nowhere
else.

---

## How this enables TDD (Pillar 2)

The use case can be tested without PostgreSQL, using a fake repository
that only lives in memory:

**Go:**
```go
type fakeSaleRepository struct {
    saved *domain.Sale
}

func (f *fakeSaleRepository) Save(ctx context.Context, sale *domain.Sale) error {
    f.saved = sale
    return nil
}

func TestRegisterSaleUseCase_RejectsEmptyDetails(t *testing.T) {
    uc := RegisterSaleUseCase{repo: &fakeSaleRepository{}}
    _, err := uc.Execute(context.Background(), RegisterSaleCommand{
        CustomerID: "c1", CreatedBy: "u1", Details: []domain.SaleDetail{},
    })
    if err == nil {
        t.Fatal("expected error for empty details, got nil")
    }
}
```

This test is written **before** the implementation (red → green →
refactor), does not depend on a running database, and runs in
milliseconds — exactly what Pillar 2 requires.

---

## Folder structure per language

```
# Go (products-service, sales-service)
cmd/
  server/main.go          ← starts the server, wires everything together
internal/
  domain/                 ← entities, invariants, no external imports
  application/            ← use cases + ports (interfaces)
  infrastructure/
    http/                 ← inbound adapters (controllers)
    postgres/              ← outbound adapters (repositories)

# Java (auth-service, customers-service)
src/main/java/com/synkrotech/
  domain/                 ← entities, invariants, no framework annotations
  application/
    usecase/               ← use cases
    port/
      in/                  ← inbound ports (optional in simple services)
      out/                 ← outbound ports (interfaces like SaleRepository)
  infrastructure/
    rest/                  ← inbound adapters (@RestController)
    persistence/            ← outbound adapters (@Repository)
```

---

## Common mistakes this document prevents

| Mistake | Why it breaks the architecture |
|---|---|
| Annotating the domain entity with `@Entity` (JPA) | The domain becomes dependent on Hibernate — it can no longer be tested without an `EntityManager` |
| The use case runs a `SELECT` directly with SQL | It skips the port — the repository can no longer be swapped for a fake in tests |
| The controller contains business logic ("if total is greater than X, apply a discount") | That rule should live in the domain; the controller only translates HTTP ↔ command |
| The domain throws a Spring exception (`ResponseStatusException`) | The domain shouldn't know HTTP exists; it should throw its own exception, and the adapter translates it to an HTTP status code |

---

## Correlations

- Domain invariants this example implements → `02-domain/entities-and-rules.md`
- Decision that redefines what `sales-service` does after the Saga → `05-architecture/decisions/records/ADR-003-gateway-saga-async.md`
- `outbox` table the outbound adapter writes to → `06-data/models.md`
- General architectural principle (Pillar 7) → `05-architecture/overview.md`, Architectural Principles
- Full testing strategy → `11-quality/testing-strategy.md` (pending)
