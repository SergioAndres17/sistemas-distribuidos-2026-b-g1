# ADR-001: Initial Architecture of the Sales Management System

**project_key:** 
**Member:** 
**Responsibility:** 

---

## Context
<!-- Responsible: Jordan Ramirez Gallego (HU-ADR-01) -->

SynkroTech SAS needs to centralize the management of customers, products, inventory,
and sales, currently dispersed across manual tools. The Distributed Systems course
requires, as a pedagogical requirement, a distributed architecture with
4 backend repositories (Java and Go), 4 frontend and 4 database repositories, under
hexagonal architecture. It is important to be transparent: SynkroTech SAS's current
volume does not require microservices for a real scalability need; the
decision is conditioned by the academic requirement, not by an organic
business driver. Within that constraint, there are 4 identifiable
bounded contexts with low coupling between them: Authentication, Customers,
Products, and Sales (the latter includes Reports).

## Decision
<!-- Responsible: Angel Gustavo Solano Trujillo (HU-ADR-02) -->

4 independent microservices under hexagonal architecture, each with its
own PostgreSQL database:
- **Auth** (Java/Spring Boot): JWT issuance and validation (RS256), roles.
- **Customers** (Java/Spring Boot): customer management.
- **Products** (Go): catalog, categories, stock.
- **Sales** (Go): sales, orchestration with Customers/Products, and Reports
  (merged as an internal module).

Synchronous communication via REST between services; local JWT validation
(public key) in each service, without calling Auth for each request.

## Considered Alternatives
<!-- Responsible: Sergio Andrés Ordóñez Díaz (HU-ADR-03) -->

**(a) 5 complete microservices** (Auth, Customers, Products, Sales, and
Reports as independent services) — rejected: exceeds the limit of
4 backend repos required by the course; Reports alone has neither the volume
nor the independent scalability need that justifies its own service.

**(b) Modular monolith** — rejected: does not satisfy the pedagogical objective of the
course (communication between distributed services, multiple languages,
multiple databases).

**(c) Auth embedded within Customers** (without its own service) — rejected:
mixes two distinct domains (internal system users vs. purchasing customers),
which violates the bounded context principle and complicates
roles/permissions management.

## Consequences
<!-- Responsible: Fredman Santiago Plazas Artunduaga (HU-ADR-04) -->

**Positive:**
+ Clear separation of responsibilities by domain (bounded contexts).
+ Balance of 2 services in Java / 2 in Go, meeting the course requirement.
+ Centralized Auth simplifies roles and security management.

**Negative:**
- Sales concentrates more responsibility than ideal (orchestrates Customers,
  Products, and generates reports).
- Synchronous communication between services introduces temporal coupling
  and points of failure (if Products goes down, Sales cannot create sales).
- No ACID transactions between services; explicit handling of
  eventual consistency is required.

---

## Immutability Rule

This ADR, once accepted, **shall not be modified**. Any change to this
architectural decision must be documented in a new file
(`adr-002-*.md`) that explicitly references ADR-001 as the record
being replaced, indicating what changed and why.
