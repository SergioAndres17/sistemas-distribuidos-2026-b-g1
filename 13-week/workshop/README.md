# Week 13 Workshop - Threat Modeling

<!-- CONFIG-START -->
- FULL_NAME: Sergio Andrés Ordóñez Díaz
- GITHUB_USER: SergioAndres17
- TEAM: synkro-tech
- DOCS_REPO: https://github.com/code-corhuila/synkro-docs
- BOUNDARY: synkro-api-gateway -> synkro-products-api (permissions to change catalog, prices and stock)
- CLAIM_ISSUE: https://github.com/code-corhuila/synkro-docs/issues/267
- PULL_REQUEST: (written in CP2)
- STORIES: (written in CP3)
- LIBRARY_BOOK: Pressman, Roger S. Ingeniería del software: un enfoque práctico, 7.ª ed., McGraw-Hill, 2010. Call number: 005.3 P727a, copy 1 (inventory 0004985). Pages: 402-403 (sections 17.7.2 and 17.7.3)
- LIBRARY_RECORD_TIME: 2026-10-28 19:00 (UTC-5)
- REFLECTION_DOC: https://docs.google.com/document/d/1PfncCPQVoNdzMJ6WIADe8nQ2siG-SQ_-dEik3usE05k/edit?usp=sharing
<!-- CONFIG-END -->

## Boundary

`synkro-api-gateway` (NGINX) routes `/api/v1/products/` and `/api/v1/stock-alerts/`
to `synkro-products-api` (Go). The gateway only checks that an `Authorization` header
exists (ADR-006, ADR-008); products-api decides who can change the catalog, the
`priceCents` of a product and its stock through `POST /api/v1/products/{id}/stock-adjustments`.
