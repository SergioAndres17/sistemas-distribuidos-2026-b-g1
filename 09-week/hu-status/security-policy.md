# Security Policy

> Security is not a feature — it is a property of the system built from day one.
> This document defines the mandatory practices.
> Any deviation must be explicitly approved by the team.

---

## Security Principles

1. **Defense in Depth:** multiple layers of security. If one fails, the others contain the damage.
2. **Least Privilege:** each component has only the minimum permissions required: each service connects to the shared instance with credentials that only access its own schema, with a role that can read and write but not delete (ADR-009, ADR-005 Decision 3), and each service token carries only the permissions that service needs (ADR-006).
3. **Fail Securely:** in the event of an error, the system denies access rather than allowing it.
4. **Security by Design:** security controls are designed from the beginning, not added at the end.
5. **Zero Trust:** no request is trusted because of where it comes from. Every service validates the token itself, ignores identity headers, and accepts internal calls only with a valid service token. Network isolation limits exposure, but it does not grant trust (ADR-006).

---

## Authentication

### JWT (JSON Web Tokens)

| Property | Required Value |
|----------|---------------|
| Signature Algorithm | RS256 (asymmetric) — defined in ADR-001 |
| Access Token Expiration | 1 hour (`exp`) |
| Refresh Token Expiration | 7 days |
| Required Claims | `sub` (user_id), `roles`, `permissions`, `iat`, `exp`, `jti` (unique token ID) |
| Client Storage (web) | Access token in memory; refresh token in `sessionStorage`; sent as `Authorization: Bearer` (see `07-api/authentication.md`, "Browser session") |

**Prohibited in the Payload:**

- Passwords
- Card data
- Full PII (only the user ID)

### Refresh Token

- Stored in `auth.refresh_token` (hash, not plaintext)
- Mandatory rotation on every use (one refresh token = one-time use)
- Invalidated on logout and password change
- ALL active tokens are invalidated if the use of a revoked token is detected

### Service Tokens

- `synkro-workflow` and `synkro-worker` authenticate with their own service token, issued by `synkro-auth-api` to an `ADMIN` only, with role `SERVICE` and only the permissions that service needs.
- They expire after 30 days and are replaced before expiring. Details: `07-api/authentication.md`, "Service tokens".

---

## Authorization

### RBAC (Role-Based Access Control)

| Role | Description | Permissions |
|------|-------------|------------|
| `ADMIN` | Business administrator | Full access: users/roles, customers, products, sales, and reports |
| `SALESPERSON` | Sales staff | Manages customers, creates sales, checks stock, views reports of their own sales |
| `INVENTORY` | Inventory staff | Manages products, categories, and stock; no access to customers, sales, or reports |

**Permission Model:**

```text
Permission: [action]

Examples applied to the project:
  customers:create
  customers:read
  customers:update
  customers:delete
  products:read
  products:write
  sales:create
  sales:read
  reports:read
  users:manage

Service tokens only:
  stock:reserve
  stock:release
  sales:register
  stock-alerts:read
  stock-alerts:write
```

**Validation:**

- Every service that receives requests validates the token's signature (RS256 only), `exp` and `sub` with Auth's public key. The gateway only checks that a credential is present (ADR-006).
- Each service validates role permissions for the specific operation on its own resources.
- Roles are included in the JWT as the claim `roles: ["SALESPERSON"]`.

---

## Secure Communication

### Transmission

- **HTTPS is mandatory** in all environments except local
- Minimum TLS 1.2; TLS 1.3 recommended
- HSTS enabled in production

### Internal Communication Between Services

- Every internal call carries the caller's service token as `Authorization: Bearer <token>`; the receiving service validates it like any other token and checks its permissions (ADR-006)

---

## Secret Management

```text
✗ NEVER in source code
✗ NEVER in a committed .env file
✗ NEVER in logs
✗ NEVER in client-facing error messages
✓ Environment variables
✓ Per-domain database credentials (owner and service user) injected through environment variables
✓ JWT_PRIVATE_KEY only in synkro-auth-api; SERVICE_TOKEN only in synkro-workflow and synkro-worker
```

**Secret Rotation:**

- Database passwords: every 6 months or immediately if compromise is suspected
- Service tokens: before their 30-day expiry, or immediately if compromise is suspected

---

## Input Validation and Sanitization

### General Rules

1. **Never trust user input.** Validate at the edge (controller) before processing.
2. **Whitelist, not blacklist.** Define what is allowed, not only what is forbidden.
3. **Fail fast.** If input is invalid, return 400 and stop processing.

### SQL Injection Prevention

Each service connects only to its own schema in the shared instance (ADR-009) and MUST use parameterized queries exclusively — never build dynamic SQL using schema or table names from user input.

---

## OWASP Top 10 — Review Checklist

| Vulnerability | Implemented Control |
|---------------|-------------------|
| A01: Broken Access Control | Token validation and RBAC/permission checks in every service; service tokens with scoped permissions |
| A02: Cryptographic Failures | TLS 1.2+, bcrypt for passwords, RS256 for JWT |
| A03: Injection | Prepared SQL statements, schema validation |
| A05: Security Misconfiguration | Review of default values before every release |
| A07: Authentication Failures | RS256-only validation in every service, refresh rotation, login rate limiting at the gateway |
| A09: Logging Failures | Logs without PII, with security events recorded |

---

## Security Auditing and Logging

### Events That Are ALWAYS Logged

```text
auth.login.success
auth.login.failure
auth.token.revoked
auth.unauthorized_access_attempt
admin.role.changed
```

**Required Fields in Security Logs:**

- `userId` (or `ANONYMOUS` if not authenticated)
- `action`
- `resource`
- `result` (SUCCESS / FAILURE)
- `timestamp`

---

## References

- Security Non-Functional Requirements → `04-requirements/non-functional.md`
- Authentication ADRs → `05-architecture/decisions/records/ADR-001-architecture.md`, ADR-006
- Schema-per-domain isolation → ADR-009 (supersedes ADR-005 Decision 1); credentials and roles → ADR-005 Decision 3
- Tokens, RBAC per resource and browser session → `07-api/authentication.md`
