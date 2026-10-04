# Domain Map

> SynkroTech SAS business mental model — this is not technology, it is the understanding
> of the problem that the system solves before writing code.

## Bounded Contexts

### Authentication and Users (Auth)
**Responsibility:** manage the identity of the system's internal users (SynkroTech SAS employees), their credentials, roles, and sessions, and issue the service tokens that the workflow and the worker use to call other contexts (ADR-006).
**Main Entities:** User, RefreshToken
**Owning Team:** entire team (no fixed service owner yet)

### Customers
**Responsibility:** manage information about SynkroTech SAS customers (buyers).
**Main Entities:** Customer
**Owning Team:** entire team (no fixed service owner yet)

### Products and Inventory
**Responsibility:** manage the product catalog, its categories, and available stock control: manual stock adjustments, the stock reservations that a sale needs, and the low-stock alerts.
**Main Entities:** Product, Category, StockReservation, StockAlert (child entities: StockAdjustment, StockReservationLine)
**Owning Team:** entire team (no fixed service owner yet)

### Sales
**Responsibility:** register the commercial transactions that the sale-registration saga sends it, with their details and the prices frozen at reservation, and serve as the source of data for reports (calculated as a derived view, not as an entity of its own — see `entities-and-rules.md`). Sales makes no calls to other contexts.
**Main Entities:** Sale, SaleDetail
**Owning Team:** entire team (no fixed service owner yet)

### Orchestration and scheduled work (not bounded contexts)

`synkro-workflow` and `synkro-worker` own no business entity, so they are not bounded contexts. `synkro-workflow` runs the `register-sale` saga across Customers, Products and Sales and keeps only its own execution state (the saga instance, ADR-007); `synkro-worker` runs the scheduled low-stock job against Products. Both call the contexts through their public APIs with a service token issued by Auth.

---

## Relationship map

```text
                    ┌──────────┐
                    │   Auth   │  (upstream — all contexts depend
                    └────┬─────┘   on it to validate JWTs, and it
                         │         issues the service tokens)
       validates JWT (locally, with the public key)
         ┌───────────────┼───────────────┐
         ▼               ▼               ▼
   ┌──────────┐    ┌───────────┐    ┌──────────┐
   │ Customers│    │   Sales   │    │ Products │
   └──────────┘    └─────┬─────┘    └──────────┘
                         │
                         ▼
                (derived reporting
                 view,
                 not an independent
                 context)
```

Calls that cross contexts are made only by the orchestrator and the worker:

```text
   synkro-workflow   ── 1. validate-customer ──────────────► Customers
   (register-sale    ── 2. reserve-stock / release-stock ──► Products
    saga)            ── 3. register-sale ──────────────────► Sales
   synkro-worker     ── low-stock job ─────────────────────► Products
```

**Relationships between contexts**

| Context A | Relationship | Context B | Description |
|-----------|-------------|-----------|-------------|
| Customers | downstream-of | Auth | Customers locally validates the JWT issued by Auth |
| Products | downstream-of | Auth | Products locally validates the JWT issued by Auth |
| Sales | downstream-of | Auth | Sales locally validates the JWT issued by Auth |

**Calls that cross contexts (orchestration)**

| Caller | Step | Context called | What it does |
|--------|------|----------------|--------------|
| `synkro-workflow` | 1 `validate-customer` | Customers | Reads the customer and checks that it is active; nothing changes if it is not |
| `synkro-workflow` | 2 `reserve-stock` | Products | Reserves the stock of every line in one step and receives each line's frozen unit price; its compensation is `release-stock` |
| `synkro-workflow` | 3 `register-sale` | Sales | Sends the customer, the lines with their frozen prices and `createdBy`; Sales calculates subtotals and total |
| `synkro-worker` | low-stock job | Products | Lists the products at or below the threshold and opens or resolves their alerts |

**Note:** no context is upstream of Auth. Auth does not depend on any other context, which confirms that it is a properly isolated cross-cutting context.

**Note:** Sales stores `customerId` and `productId` as plain identifiers. It never calls or queries Customers or Products: the saga checks the customer and reserves the stock before the sale is written (`entities-and-rules.md`, ADR-007).

---

## Ubiquitous Language (ubiquitous language by context)

| Term | Context | Exact meaning in this context |
|---------|----------|--------------------------------------|
| User | Auth | SynkroTech SAS employee who logs into the system |
| Service token | Auth | Token that Auth issues to `synkro-workflow` or `synkro-worker`, with only the permissions that service needs; it is never assigned to a person |
| Customer | Customers | Individual or legal entity that purchases products — **never** refers to a system user |
| Product | Products | Catalog item with its own price and stock |
| Stock reservation | Products | Stock set aside for every line of a sale in one step, with each line's price frozen; it is given back if the saga compensates |
| Stock alert | Products | Notice opened by the worker when a product's stock is at or below the threshold; a product has at most one open alert |
| Sale | Sales | Confirmed transaction that has already deducted stock and generated traceability |
| Report | Sales | View calculated on demand from Sale/SaleDetail — it is not a persisted entity |
| Saga | — (orchestration) | The `register-sale` sequence run by `synkro-workflow`: validate the customer, reserve the stock, register the sale, with a compensation for the stock; its state survives a restart |

---

## References

- Entity and Rules Catalog → `02-domain/entities-and-rules.md`
- Domain Events Catalog → `02-domain/domain-events.md`
- This map directly feeds → `05-architecture/overview.md` (bounded contexts → microservices)
- Architecture Decision → `05-architecture/decisions/records/ADR-001-architecture.md`
- Saga, stock reservations and stock alerts → `05-architecture/decisions/records/ADR-007-persistent-saga-and-scheduled-work.md`
- Service tokens → `05-architecture/decisions/records/ADR-006-token-validation-per-service.md`
- Shared glossary → `01-context/glossary.md`
