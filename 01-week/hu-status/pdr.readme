# 6. Preliminary Data Model

The system will use an independent database for each microservice. Each service will be responsible for managing and persisting only the information belonging to its own domain.

## 6.1 Customers Service — customers DB

### Table: customers

* `cliente_id` (PK)
* `nombre`
* `documento_identidad`
* `correo`
* `telefono`
* `direccion`
* `fecha_registro`

This table stores the basic information of the customers registered in the system.

## 6.2 Products Service — products DB

### Table: productos

* `producto_id` (PK)
* `nombre`
* `precio`
* `stock`
* `categoria_id` (FK)

### Table: categorias

* `categoria_id` (PK)
* `nombre`

The `productos` table stores the product catalog and available stock. The relationship with `categorias` is maintained within the same database using a foreign key.

## 6.3 Sales Service — sales DB

### Table: ventas

* `venta_id` (PK)
* `cliente_id` (external reference, validated through API)
* `fecha`
* `total`

### Table: detalle_venta

* `detalle_id` (PK)
* `venta_id` (FK)
* `producto_id` (external reference, validated through API)
* `cantidad`
* `precio_unitario`
* `subtotal`

The `ventas` table stores the general information of each sale, while `detalle_venta` contains the products associated with each transaction.

The `cliente_id` and `producto_id` fields are not real foreign keys because they belong to databases managed by other microservices. Their existence will be validated through communication between services.

## 6.4 Reports Service — reports DB

### Table: resumen_ventas

* `fecha`
* `total_ventas_dia`
* `total_ventas_mes`
* `producto_id`
* `cantidad_vendida`

This structure will store or retrieve summarized information required to generate daily, monthly, and top-selling product reports.

> **Distributed design note:** Each microservice has its own PostgreSQL database and does not have direct access to another service's database. References between domains are handled through API communication, maintaining the independence of each service.

---

# 7. Interfaces (APIs) — Preliminary

Communication between microservices will initially be implemented through REST APIs over HTTP. These interfaces will allow each service to access or modify information without directly accessing another service's database.

| Service   | Endpoint (example)            | Method | Description                 |
| --------- | ----------------------------- | ------ | --------------------------- |
| Customers | `/api/clientes`               | POST   | Register customer           |
| Customers | `/api/clientes/{id}`          | GET    | Retrieve customer           |
| Customers | `/api/clientes/{id}`          | PUT    | Update customer             |
| Customers | `/api/clientes/{id}`          | DELETE | Delete customer             |
| Products  | `/api/productos`              | POST   | Register product            |
| Products  | `/api/productos/{id}/stock`   | PATCH  | Update product stock        |
| Sales     | `/api/ventas`                 | POST   | Create sale                 |
| Sales     | `/api/ventas/{id}`            | GET    | Retrieve sale details       |
| Reports   | `/api/reportes/diario`        | GET    | Daily sales report          |
| Reports   | `/api/reportes/mensual`       | GET    | Monthly sales report        |
| Reports   | `/api/reportes/top-productos` | GET    | Top-selling products report |

### Communication between services

* **Sales → Customers:** Validate the existence of the customer before creating a sale.
* **Sales → Products:** Validate stock availability, retrieve the product price, and update stock after the sale.
* **Reports → Sales:** Retrieve historical sales information required to generate reports.

All routes, except authentication-related routes, will require a valid JWT token through the following header:

`Authorization: Bearer <token>`
