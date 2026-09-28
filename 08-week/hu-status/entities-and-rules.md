# Entities and Business Rules

> Catalog of entities, aggregates, and business rules (invariants) of the domain.
> Money is modeled as integers in minor units (1/100 of a Colombian peso), named `…Cents`,
> so no amount is ever rounded (ADR-005). Prices and totals are still primitive types
> (no Value Objects), to keep things simple at this stage of the project.

---

## Identifier strategy: UUID

All entities in the system use **UUID** as their identifier, instead of auto-increment (`SERIAL`/`BIGSERIAL`). Justification:

| Criterion | Why UUID wins in this project |
|---|---|
| Microservice independence | Each service generates its own IDs without depending on the database to hand it the next number — consistent with the real independence principle defined in ADR-001 |
| Cross-service references without real FKs | External references (`customerId`, `productId` in Sales) remain unambiguous across the separate database of each domain (ADR-005) |
| Hexagonal architecture | The domain can build a complete aggregate, identity included, **before** touching infrastructure — it does not depend on the database assigning the ID after the INSERT |
| Does not leak business volume | An ID like `customerId = 42` leaks how many customers exist; a UUID does not reveal that information |

**Accepted trade-off:** UUIDs are less readable in logs/debugging than a sequential number. Mitigated with structured logging (always include descriptive fields alongside the ID, not just the raw UUID).

---

## Context: Authentication and Users (Auth)

### Aggregate: User (root) + RefreshToken (child entity)

## Entity: User
**Belongs to:** Auth
**Identifier:** userId

### Attributes
| Attribute | Type | Description | Required | Rules |
|-----------|------|-------------|----------|-------|
| userId | UUID | Unique identifier | Yes | — |
| name | string | Employee's full name | Yes | — |
| email | string | Email used to log in | Yes | Unique across the system |
| passwordHash | string | Encrypted password (bcrypt) | Yes | Never stored in plain text |
| role | enum | ADMIN, SALESPERSON, or INVENTORY | Yes | Only one of the 3 valid values |
| registeredAt | datetime | User creation date | Yes | — |
| active | boolean | Whether the user can log in | Yes | Default `true` |

### Business rules (invariants)
- [ ] `email` must be unique among all active users.
- [ ] A deactivated user (`active = false`) cannot log in or receive new tokens.
- [ ] `role` can only take one of the 3 defined values; there are no custom roles in the MVP. `SERVICE` exists only inside service tokens and is never assigned to a user (ADR-006).
- [ ] The password is never exposed in any API response.

### Behaviors (domain methods)
- `authenticate(credentials)`: validates email/password and returns the user if correct.
- `changeRole(newRole)`: can only be executed by an ADMIN on another user.
- `deactivate()`: sets `active = false` and revokes all their active refresh tokens.

---

## Entity: RefreshToken
**Belongs to:** Auth (child entity of the User aggregate)
**Identifier:** tokenId

### Attributes
| Attribute | Type | Description | Required | Rules |
|-----------|------|-------------|----------|-------|
| tokenId | UUID | Unique identifier | Yes | — |
| userId | UUID | Owning user | Yes | FK to User |
| token | string | Refresh token value (hash) | Yes | — |
| expiresAt | datetime | When it stops being valid | Yes | 7 days after issuance |
| active | boolean | Whether the token can be used | Yes | Default `true` |

### Business rules (invariants)
- [ ] A refresh token can only be used once (mandatory rotation).
- [ ] An expired token is never valid, even if `active = true`.
- [ ] Revoking a user automatically revokes all of their refresh tokens.

### Behaviors (domain methods)
- `rotate()`: invalidates the current token and issues a new one.
- `revoke()`: sets `active = false` manually (logout).
- `isExpired()`: returns whether `expiresAt` has already passed.

---

## Context: Customers

### Aggregate: Customer (root, no child entities)

## Entity: Customer
**Belongs to:** Customers
**Identifier:** customerId

### Attributes
| Attribute | Type | Description | Required | Rules |
|-----------|------|-------------|----------|-------|
| customerId | UUID | Unique identifier | Yes | — |
| name | string | Name or business name | Yes | — |
| identityDocument | string | Identity document (cédula or NIT), used to find the customer at the point of sale | Yes | Unique across all customers, active or not |
| email | string | Contact email | No | — |
| phone | string | Contact phone | No | — |
| address | string | Contact address | No | — |
| registeredAt | datetime | Creation date | Yes | — |
| active | boolean | Whether the customer can be linked to new sales | Yes | Default `true` |

### Business rules (invariants)
- [ ] `identityDocument` must be unique across all customers, active or not: the document identifies one real person or company.
- [ ] A deactivated customer cannot be linked to new sales (historical sales remain intact).

### Behaviors (domain methods)
- `updateInfo(data)`: updates the customer's contact fields.
- `deactivate()`: sets `active = false`, preserving their sales history.

---

## Context: Products and Inventory

### Aggregate 1: Product (root, references Category by ID) + StockAdjustment (child entity)

## Entity: Product
**Belongs to:** Products
**Identifier:** productId

### Attributes
| Attribute | Type | Description | Required | Rules |
|-----------|------|-------------|----------|-------|
| productId | UUID | Unique identifier | Yes | — |
| name | string | Product name | Yes | — |
| priceCents | integer | Unit sale price, in minor units | Yes | Must be > 0 |
| stock | integer | Available quantity | Yes | Can never be negative |
| categoryId | UUID | Category it belongs to | Yes | FK to Category |
| active | boolean | Whether the product can be sold | Yes | Default `true` |

### Business rules (invariants)
- [ ] `priceCents` must always be greater than 0.
- [ ] `stock` can never become a negative value.
- [ ] A deactivated product cannot be added to a new sale or to a new stock reservation.
- [ ] Stock only changes through a stock reservation (`reserve`, `release`) or a stock adjustment (`adjust`). Updating the product's details never changes its stock.

### Behaviors (domain methods)
- `updateDetails(name, priceCents, categoryId)`: updates the product's catalog data; never touches `stock`.
- `adjust(delta, reason, adjustedBy)`: manual correction by INVENTORY (positive to restock, negative for damage or loss); records a `StockAdjustment`; fails if `delta = 0` or if `stock + delta < 0`.
- `reserve(quantity)`: decreases stock for a stock reservation; fails if `quantity > stock` or the product is inactive.
- `release(quantity)`: gives stock back when a stock reservation is released.
- `deactivate()`: sets `active = false`.

---

## Entity: StockAdjustment
**Belongs to:** Products (child entity of the Product aggregate)
**Identifier:** adjustmentId

### Attributes
| Attribute | Type | Description | Required | Rules |
|-----------|------|-------------|----------|-------|
| adjustmentId | UUID | Unique identifier | Yes | — |
| productId | UUID | Adjusted product | Yes | FK to Product |
| delta | integer | Units added (positive) or removed (negative) | Yes | Never 0 |
| reason | string | Why the stock was corrected | Yes | Not empty |
| adjustedBy | UUID | System user who made the adjustment (external reference) | Yes | Taken from the `sub` of the validated token (ADR-006) |
| adjustedAt | datetime | When the adjustment was made | Yes | — |

### Business rules (invariants)
- [ ] An adjustment is recorded in the same transaction as the stock change it describes.
- [ ] An adjustment is never edited or deleted; a mistake is corrected with a new adjustment.

---

### Aggregate 2: Category (root, independent)

## Entity: Category
**Belongs to:** Products
**Identifier:** categoryId

### Attributes
| Attribute | Type | Description | Required | Rules |
|-----------|------|-------------|----------|-------|
| categoryId | UUID | Unique identifier | Yes | — |
| name | string | Category name | Yes | Unique among active categories |
| active | boolean | Whether the category can be used on new products | Yes | Default `true` |

### Business rules (invariants)
- [ ] `name` must be unique among active categories.
- [ ] A category with active products still assigned to it cannot be deactivated (validated in the use case).

### Behaviors (domain methods)
- `rename(newName)`: updates the category's name.
- `deactivate()`: sets `active = false`.

---

### Aggregate 3: StockReservation (root) + StockReservationLine (child entity)

## Entity: StockReservation
**Belongs to:** Products
**Identifier:** reservationId

Created by the sale-registration saga to set aside the stock of every line of a sale in one step (ADR-007).

### Attributes
| Attribute | Type | Description | Required | Rules |
|-----------|------|-------------|----------|-------|
| reservationId | UUID | Unique identifier | Yes | — |
| status | enum | `RESERVED` or `RELEASED` | Yes | Starts as `RESERVED` |
| createdAt | datetime | When the stock was reserved | Yes | — |
| releasedAt | datetime | When the stock was given back | No | Present only when `status = RELEASED` |

### Business rules (invariants)
- [ ] A reservation has at least 1 line, and each product appears in at most one line.
- [ ] Creating a reservation reserves the stock of **every** line in one transaction; if any product lacks stock or is inactive, nothing is reserved.
- [ ] Releasing a reservation gives back the stock of every line exactly once. Releasing a reservation that is already `RELEASED` changes nothing and succeeds.
- [ ] `RELEASED` is a final status.

### Behaviors (domain methods)
- `reserve(lines)`: creates the reservation and calls `Product.reserve(quantity)` for each line.
- `release()`: calls `Product.release(quantity)` for each line and sets `status = RELEASED`; does nothing if already released.

---

## Entity: StockReservationLine
**Belongs to:** Products (child entity of the StockReservation aggregate)
**Identifier:** lineId

### Attributes
| Attribute | Type | Description | Required | Rules |
|-----------|------|-------------|----------|-------|
| lineId | UUID | Unique identifier | Yes | — |
| reservationId | UUID | Reservation it belongs to | Yes | FK to StockReservation |
| productId | UUID | Reserved product | Yes | FK to Product |
| quantity | integer | Reserved units | Yes | Must be > 0 |
| unitPriceCents | integer | Product price when the stock was reserved | Yes | Copied from `Product.priceCents`; never changes afterward |

### Business rules (invariants)
- [ ] `quantity` must always be greater than 0.
- [ ] `unitPriceCents` is the price the sale will use: it freezes the price at the moment the stock is reserved.

---

### Aggregate 4: StockAlert (root, independent)

## Entity: StockAlert
**Belongs to:** Products
**Identifier:** alertId

Opened and resolved by the worker's low-stock job; listed by INVENTORY and ADMIN (ADR-007). What counts as "low" is the worker's global threshold, not a product attribute.

### Attributes
| Attribute | Type | Description | Required | Rules |
|-----------|------|-------------|----------|-------|
| alertId | UUID | Unique identifier | Yes | — |
| productId | UUID | Product with low stock | Yes | FK to Product |
| status | enum | `OPEN` or `RESOLVED` | Yes | Starts as `OPEN` |
| stockAtOpening | integer | Stock when the alert was opened | Yes | Must be >= 0 |
| openedAt | datetime | When the alert was opened | Yes | — |
| resolvedAt | datetime | When the product was back above the threshold | No | Present only when `status = RESOLVED` |

### Business rules (invariants)
- [ ] A product has at most one `OPEN` alert at a time.
- [ ] `RESOLVED` is a final status; a new drop opens a new alert.
- [ ] Resolving an alert that is already `RESOLVED` changes nothing and succeeds.

### Behaviors (domain methods)
- `open(productId, stock)`: creates an `OPEN` alert, unless the product already has one.
- `resolve()`: sets `status = RESOLVED`; does nothing if already resolved.

---

## Context: Sales

### Aggregate: Sale (root) + SaleDetail (child entity)

## Entity: Sale
**Belongs to:** Sales
**Identifier:** saleId

### Attributes
| Attribute | Type | Description | Required | Rules |
|-----------|------|-------------|----------|-------|
| saleId | UUID | Unique identifier | Yes | — |
| customerId | UUID | Associated customer (external reference) | Yes | Validated by the saga before the sale is registered |
| createdBy | UUID | System user who registered the sale (external reference) | Yes | Sent by the saga from the salesperson's validated token (ADR-002, ADR-006) |
| date | datetime | Sale date | Yes | — |
| totalCents | integer | Sum of the detail's subtotals, in minor units | Yes | Must equal the sum of `saleDetail.subtotalCents` |
| active | boolean | Whether the sale is valid (not voided) | Yes | Default `true` |

### Business rules (invariants)
- [ ] A sale must have at least 1 associated `SaleDetail`.
- [ ] `totalCents` must always equal the sum of its detail's `subtotalCents` values.
- [ ] `customerId` must correspond to an active customer: the saga checks it in its first step, before any stock is reserved.
- [ ] Every line is backed by stock already reserved in Products: the saga reserves it before registering the sale.
- [ ] `createdBy` is the salesperson who started the sale. Sales accepts it only from a caller holding `sales:register`, which only the workflow's service token has (ADR-006).

### Behaviors (domain methods)
- `register(customerId, createdBy, lines)`: creates the sale with its detail and calculates the total; fails if there are no lines.
- `addDetail(productId, quantity, unitPriceCents)`: adds a line to the detail and recalculates the total.
- `calculateTotal()`: recalculates the total from the current detail.

---

## Entity: SaleDetail
**Belongs to:** Sales (child entity of the Sale aggregate)
**Identifier:** detailId

### Attributes
| Attribute | Type | Description | Required | Rules |
|-----------|------|-------------|----------|-------|
| detailId | UUID | Unique identifier | Yes | — |
| saleId | UUID | Sale it belongs to | Yes | FK to Sale |
| productId | UUID | Product sold (external reference) | Yes | Reserved by the saga before the sale is registered |
| quantity | integer | Units sold | Yes | Must be > 0 |
| unitPriceCents | integer | Product's price at the time of the sale, in minor units | Yes | The price frozen by the stock reservation |
| subtotalCents | integer | `quantity * unitPriceCents` | Yes | Calculated, not directly editable |

### Business rules (invariants)
- [ ] `quantity` must always be greater than 0.
- [ ] `subtotalCents` must always equal `quantity * unitPriceCents`.
- [ ] `unitPriceCents` is frozen when the stock is reserved (it does not change if the product's price changes later).

### Behaviors (domain methods)
- `calculateSubtotal()`: recalculates `subtotalCents` from `quantity` and `unitPriceCents`.

---

## Resolving external references between services

Since `customerId` (on Sale) and `productId` (on SaleDetail) are UUIDs owned by other domains, each with its own database (ADR-005), **there is no real foreign key** — Sales never performs a direct JOIN against the Customers or Products tables. Resolution happens at two distinct moments:

### 1. At write time (when registering the sale) — the saga

The sale is registered by the `register-sale` saga of `synkro-workflow` (ADR-007), never by a direct call to Sales:

1. **`validate-customer`**: the saga reads the customer from Customers. If it does not exist or has `active = false`, the saga ends before anything changes.
2. **`reserve-stock`**: the saga creates one `StockReservation` in Products with every line. Products checks that each product is active and has enough stock, reserves all lines in one transaction, and returns each line's `unitPriceCents`.
3. **`register-sale`**: the saga sends Sales the customer, the lines with their frozen prices, and `createdBy`. Sales calculates subtotals and total and stores the sale. If this step fails, the saga releases the reservation.

**What Sales actually stores:** only the UUID (`customerId`, `productId`) — it does **not** store a copy of the customer's name or the product's name. The only exception is `unitPriceCents`, which is copied because it is part of the business rule (the price of a past sale must not change if the product's price increases later).

### 2. At read time (when showing a sale) — the portal composes

Sales returns only identifiers. When the UI needs to display the customer's name or each product's name, the sales portal asks each domain for them through the gateway, with the person's own token:

```
GET /api/v1/sales/{id}                 → Sales returns: customerId, detail[] (with productId)
GET /api/v1/customers/{customerId}     → Customers returns: name, identityDocument, etc.
GET /api/v1/products/{productId}       (per line) → Products returns: name, category, etc.
```

Sales itself makes no calls to other domains: it holds no service token (ADR-006).

**Known limitation (accepted for the MVP):** if a list of sales needs to display the names of many different customers/products, this implies several HTTP calls (an N+1 pattern across services). This cost is accepted for the MVP for the sake of simplicity. If this becomes a real performance issue later, the options to evaluate would be: (a) a "batch lookup" endpoint (for example, customers filtered by a list of IDs) to reduce the number of calls, or (b) a small read-only cache in the portal — neither is implemented in this MVP.

---

## Note on Reports (SalesSummary)

`SalesSummary` **is not modeled as a domain entity**. It was decided to treat it as a **derived view/projection**, calculated on demand via aggregation queries (`SUM`, `GROUP BY`) directly over `Sale` and `SaleDetail`, with no domain logic or invariants of its own. This avoids maintaining a manually synchronized counter and eliminates the risk of the summary becoming out of sync with the actual sales.

Per ADR-005, the `sales_summary` table is not created. Reports (FR-008, FR-009) are served directly from aggregation queries over `sale` and `sale_detail`.

---

## Correlations

- Context map → `02-domain/domain-map.md`
- Domain events → `02-domain/domain-events.md`
- Technical data model (tables per domain) → `06-data/models.md`
- Money in minor units, database per domain → ADR-005
- Service tokens and sale authorship → ADR-006
- Sale-registration saga, stock reservation and stock alerts → ADR-007
- Business rules → acceptance criteria in `04-requirements/user-stories.md`
