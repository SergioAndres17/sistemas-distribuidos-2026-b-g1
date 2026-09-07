# SynkroTech MVP — Backend

## Prerequisites

- Java 21
- PostgreSQL running on port 5435 (or configure via environment variables)
- Maven (included via mvnw wrapper)

## Setup

1. Clone the repository and switch to the `develop` branch
2. Copy `.env.example` to `.env` and fill in your database credentials
3. Create the database:
   ```sql
   CREATE DATABASE synkrotech_db;
   ```
4. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
5. The backend starts on `http://localhost:8080`
6. On first run with an empty database, `DataSeeder` automatically creates:
   - 3 users (Ana/ADMIN, Carlos/SALESPERSON, Laura/INVENTORY)
   - 3 sample customers
   - 3 categories and 7 products

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5435/synkrotech_db` | JDBC connection string |
| `DB_USERNAME` | `postgres` | Database user |
| `DB_PASSWORD` | `123456` | Database password |

## API Base URL

All endpoints are under `http://localhost:8080/api`.

## Main Endpoints

- `POST /api/auth/login` — simulated login (no JWT yet)
- `GET/POST /api/customers` — customer CRUD
- `GET/POST /api/products` — product CRUD
- `GET/POST /api/categories` — category CRUD
- `GET/POST /api/sales` — sale registration and history

## Tech Stack

- Java 21 + Spring Boot 4.1.1
- PostgreSQL (single schema `public` for this MVP)
- JPA/Hibernate with `ddl-auto=update`
