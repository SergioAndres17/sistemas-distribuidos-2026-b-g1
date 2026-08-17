# Weekly status - Week 02

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Sergio Andres Ordoñez Diaz
- GITHUB_USER: SergioAndres17
- TEAM: Group - SynkroTech SAS
- SPRINT_GOAL: Data model and interfaces (APIs) and Document Rejected Alternatives.

| Full Name                          | GitHub User                                                 |
| ----------------------------       | ------------------------------------------                  |
| Sergio Andres Ordoñez Diaz         | https://github.com/SergioAndres17                           |
| Fredman Santiago Plazas Artunduaga | https://github.com/SantiagoPlazas2005                       |
| Jordan Ramirez Gallego             | https://github.com/JordanRG420                              |
| Angel Gustavo Solano Trujillo      |  https://github.com/AsolanoT                                |

## 1. User stories worked on this week

| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-PDR-03 | Data model and interfaces (APIs) | done | [[COMMIT_URL](https://github.com/AsolanoT/01-invoice-app/issues/9)](https://github.com/AsolanoT/01-invoice-app/issues/9) |
| HU-ADR-03 | Document Rejected Alternatives | done | [https://github.com/AsolanoT/01-invoice-app/issues/10](https://github.com/AsolanoT/01-invoice-app/issues/10) |

## 2. My individual contribution

- I designed the preliminary data model for the 4 microservices, defining the main tables and data responsibilities for each service.
- I defined the REST APIs and interfaces for the Clients, Products, Sales, and Reports services.
- I documented the architectural alternatives considered for the system and the specific reasons for rejecting them.
- I documented three rejected alternatives: 5 complete microservices, a modular monolith, and embedding Authentication within the Customers service.
- I prepared the initial structure that will be used to create the PostgreSQL database scripts for the 4 services.

## 3. Blockers and risks

- No blockers were identified during the development of these user stories.
- The preliminary data model and API definitions may require minor adjustments during implementation as the services are developed and integrated.

## 4. Plan for next week

- Prepare the initial PostgreSQL database scripts for the 4 microservices.
- Validate the data model during the implementation of the services.
- Review and adjust the REST APIs as service integration begins.

## 5. Compliance self-check

- [ ] Conventional Commits - `type(scope): summary`
- [ ] HU branch by environment + PR to that environment (hu-xxx-dev -> develop, ...)
- [x] Verifiable acceptance criteria
- [ ] Tests added/updated (unit / integration)
- [x] DDD / hexagonal boundaries respected (the domain has no I/O)
- [x] No secrets; configuration via environment variables

## 6. Evidence links
- Contribution to Requirements and Architecture: [`pdr.md`](./pdr.md)
  
## Considered Alternatives

<!-- Responsible: Sergio Andrés Ordóñez Díaz (HU-ADR-03) -->

**(a) 5 complete microservices**

Auth, Customers, Products, Sales, and Reports as independent services.

**Rejected:** This exceeds the limit of 4 backend repositories required by the course. Additionally, the Reports service does not have enough workload or independent scalability requirements to justify an additional service.

**(b) Modular monolith**

**Rejected:** This approach does not satisfy the main pedagogical objective of the course, which requires communication between distributed services, multiple programming languages, and multiple databases.

**(c) Auth embedded within Customers**

Authentication was considered as part of the Customers service instead of being handled independently.

**Rejected:** This would mix two different domains: internal system users and purchasing customers. This violates the bounded context principle and makes role and permission management more complex.
