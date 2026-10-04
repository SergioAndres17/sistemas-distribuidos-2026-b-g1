# Technical Security Rules

> Mandatory technical controls that apply to all project code.
> These rules complement the security policy (`security-policy.md`) with
> concrete implementation practices for the Java (Spring Boot) and Go stacks.

---

## OWASP Top 10 — Controls by Category

### A01 — Broken Access Control

**Rules:**

- Every protected endpoint MUST have the token-validation middleware/filter applied (Spring Security in Java, custom middleware in Go), in every service that receives requests — never only at the gateway
- The middleware accepts only `RS256`, requires `exp` and `sub`, tolerates at most 30 seconds of clock skew, and answers `401 UNAUTHORIZED` otherwise
- Identity, roles and permissions are read only from the validated token; any `X-User-*` header is ignored
- Permissions are verified in the application layer (use case), not in the controller
- A resource is only returned if the user has the `read` permission
- Write operations require `write` or the corresponding specific permission
- Operations reserved to service tokens (stock reservations, sale registration, stock alerts) check the permission, not the role, and answer `403 FORBIDDEN` to any other caller

### A02 — Cryptographic Failures

**Rules:**

- Passwords: use **bcrypt** with a cost factor ≥ 12. Never use MD5 or SHA-1 for passwords
- JWT: sign with RS256 (asymmetric), as defined in ADR-001; only `synkro-auth-api` holds the private key, and validators reject `none` and `HS256` explicitly
- Sensitive data in transit: HTTPS is mandatory in all environments except local
- Never log passwords, tokens, or card data

### A03 — Injection

**Rules:**

- Always use parameterized queries. Zero concatenated SQL strings.
- Validate and sanitize all input using a validation library (Bean Validation in Java, `go-playground/validator` in Go)
- Each service connects only to its own schema in the shared instance (ADR-009) — never build a schema or table name from user input

### A05 — Security Misconfiguration

```text
# Environment Verification Checklist

□ Stack traces NOT visible in production
□ Security headers configured
□ Unnecessary ports closed: no -api, -db, workflow or worker compose file declares `ports:`
□ Development credentials NOT in production: development keys, tokens and the development sign-in exist only in develop
```

### A06 — Vulnerable Components

**Rules:**

- Run dependency scanning (`mvn dependency-check` in Java, `govulncheck` in Go) before every release
- Critical/High vulnerabilities block deployment
- Update dependencies every sprint (at least once)

### A07 — Identification and Authentication Failures

- JWTs with a maximum expiration of **1 hour** for access tokens
- Refresh tokens with an expiration of **7 days** and rotation on every use
- Rate limiting on `/api/v1/auth/login` at the gateway: maximum 10 attempts per IP within 5 minutes
- Service tokens expire after 30 days and are replaced before they expire

### A09 — Security Logging and Monitoring Failures

- Every failed authentication attempt must be logged with IP address and timestamp
- Logs for soft deletion operations (`active = false`) including who, when, and what was deactivated
- Security logs are retained for a minimum of 90 days

---

## User Input Handling

**Rule:** all external input (HTTP body, query parameters, path parameters) must pass through a validation schema before reaching the domain.

- Java: use `jakarta.validation` (`@Valid`, `@NotNull`, `@Email`, etc.) in the input DTOs of each controller.
- Go: use `go-playground/validator` with validated input structs before invoking the use case.

---

## Secure Error Handling

**Rule:** never expose internal details (stack traces, database exception messages) in HTTP responses. Return a generic error code and log the details only on the server side.

```text
✗ BAD — exposes internal details to the client
✓ GOOD — generic message + internal error code for log correlation
```

---

## References

- Security policy (management, access control) → `00-governance/security-policy.md`
- Authentication and JWT → `07-api/authentication.md`
- Security architecture decisions → `05-architecture/decisions/records/ADR-001-architecture.md`, ADR-005, ADR-006
