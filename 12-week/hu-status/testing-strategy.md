# Testing Strategy

> Defines what, how much, and with which tools to test at each layer.
> This document is the project's quality contract. TDD (red → green →
> refactor) is Pillar 2 of the project: the test is always written
> before the code it validates.
>
> The project has two backend stacks: Java (Spring Boot) for
> `synkro-auth-api` and `synkro-customers-api`, and Go for
> `synkro-products-api` and `synkro-sales-api`. `synkro-workflow`
> (Java) and `synkro-worker` (Go) follow the same conventions as
> their respective stacks (ADR-008). The frontend has two frameworks,
> React 19 (the host and three portals) and Angular 21
> (`synkro-customers-portal`, ADR-011); its own tier is below. Two
> database engines are in play: PostgreSQL for Auth, Customers,
> Products and the saga store, and MongoDB for Sales (ADR-010) — each
> has its own integration-testing convention, in Tier 2.

---

## Testing pyramid

```
                 /\
                /  \
               / E2E \       ← Few, slow — critical flows and recorded acceptance
              / [5%]  \
             /──────────\
            / Integration \
           /    [25%]      \  ← HTTP adapter in memory; persistence adapter on PostgreSQL or MongoDB
          /────────────────\
         /  Contract Tests   \
        /      [20%]          \  ← OpenAPI contract compliance (synkro-products-api.yaml, etc.)
       /──────────────────────\
      /       Unit Tests        \
     /          [50%]            \  ← Domain + application layer, no I/O
    /────────────────────────────\
```

**Rule:** more tests at the bottom = more maintainable system at lower
cost. Inverting the pyramid makes CI slow and fragile. The frontend has
its own, separate pyramid-shaped tier below — a portal's tests are not
"integration tests" against a backend, they are unit-equivalent tests
against a double of the host's client.

---

## Coverage thresholds (backend)

| Layer | Minimum coverage | Measured by |
|-------|-----------------|-------------|
| Domain (`domain/`) | ≥ 90% lines | JaCoCo (Java) / `go test -coverprofile` (Go) |
| Application (`application/`) | ≥ 80% lines | Same |
| Infrastructure (`infrastructure/`) | ≥ 60% lines | Same — adapters are tested by integration tests, not unit tests |
| Global (all layers) | ≥ 80% lines | Same |

**Coverage rule:** if a PR lowers the global coverage, CI fails.
Coverage cannot go down — uncovered code requires tests in the same PR.
This applies identically to `synkro-sales-api`: its domain and
application layers are tested exactly like `synkro-products-api`'s; only
its persistence adapter (Tier 2) and its coverage-report tool differ by
driver, not by threshold.

Frontend coverage thresholds are separate — see "Frontend tests" below.

---

## Tier 1 — Unit tests

**Objective:** test business logic in complete isolation — no DB, no
HTTP, no framework.

| Aspect | Java (Spring Boot) | Go |
|--------|-------------------|-----|
| Framework | JUnit 5 | `testing` (standard library) |
| Mocking | Mockito | testify/mock or hand-written fakes |
| Assertions | AssertJ | testify/assert + testify/require |
| Speed | < 5 ms per test | < 5 ms per test |
| When they run | On every push and in CI | Same |

### What to test

- Domain entities: invariants, state transitions, value calculations
- Value objects: equality, validation
- Application use cases: orchestration logic with port fakes/mocks

### What NOT to test here

- Framework annotations (`@RestController`, HTTP router)
- SQL queries, document operations, repository implementations
- External HTTP calls

### Folder structure

**Java (three Maven modules, ADR-008):**

```
synkro-auth-api/
├── auth-core/src/test/java/co/edu/corhuila/synkro/auth/
│   ├── domain/model/SystemUserTest.java
│   └── application/usecase/
│       ├── RegisterUserUseCaseTest.java
│       └── FakeUserRepository.java                  # hand-written fake of the port
├── auth-adapters/src/test/java/…/adapter/out/persistence/
│   └── JdbcUserRepositoryIntegrationTest.java       # Tier 2
└── auth-app/src/test/java/…/app/
    └── AuthHttpTest.java                            # Tier 2
```

**Go:**

```
synkro-products-api/
├── internal/domain/model/product_test.go
├── internal/application/usecase/products_test.go    # with a hand-written fake of the ports
└── internal/adapter/
    ├── in/httpapi/handler_test.go                   # Tier 2: httptest and the in-memory repository
    └── out/persistence/postgres_integration_test.go # Tier 2: TEST_DATABASE_URL
```

`synkro-sales-api` follows the same Go layout; only its
`out/persistence/` package differs — `mongo_integration_test.go`
instead of `postgres_integration_test.go` — because its domain and
application layers import nothing beyond the standard library and the
engine behind the repository port, same as every other Go service
(`_stacks/go.md`, "Sales: the same shape, a different persistence
adapter").

### Naming conventions

**Java:**

```java
// Class: <ClassUnderTest>Test.java
// Method: should_<expectedBehavior>_when_<condition>

class SystemUserTest {
    @Test
    void should_reject_creation_when_email_is_empty() { ... }

    @Test
    void should_default_to_active_on_creation() { ... }
}
```

**Go:**

```go
// File: <module>_test.go in the same package
// Function: Test<Function>_<condition>

func TestNewProduct_RejectsZeroPrice(t *testing.T) { ... }

func TestAdjustStock_RejectsNegativeResult(t *testing.T) { ... }
```

### Example — domain unit test

**Java (auth domain):**

```java
// auth-core/src/test/java/co/edu/corhuila/synkro/auth/domain/model/SystemUserTest.java
@Test
void should_reject_creation_when_role_is_SERVICE() {
    assertThatThrownBy(() -> SystemUser.create("Alice", "alice@test.com", "hash", "SERVICE"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("SERVICE is not a valid person role");
}
```

**Go (products domain):**

```go
// internal/domain/model/product_test.go
func TestNewProduct_RejectsZeroPrice(t *testing.T) {
    _, err := model.NewProduct("p-1", "Mouse", 0, "c-1")
    require.ErrorIs(t, err, model.ErrPriceNotPositive)
}
```

### Example — application unit test with port fake

**Auth application — executable tests must be written before implementation:**

The earlier unauthenticated registration example is replaced by these acceptance
scenarios. Concrete port/fake signatures belong to the implementation story;
these scenarios do not invent a production API that has not been implemented.

```gherkin
Scenario: ADMIN creation is attributable
  Given a trusted human ADMIN and an unused idempotency key
  When the registration use case creates a permitted human role
  Then the user, key and one success audit record commit together
  And the password is hashed and no secret is present in audit evidence

Scenario: An unauthorized caller cannot register or replay
  Given a trusted SALESPERSON, INVENTORY or SERVICE caller
  When the caller registers or replays an existing registration key
  Then authorization is denied before returning a user
  And no user, key or successful audit record is written

Scenario: Audit persistence failure rolls back creation
  Given an ADMIN and an audit adapter that fails to persist evidence
  When registration is attempted
  Then the user and idempotency key are not committed
```

Verify application ports with fakes; verify transaction rollback against real
PostgreSQL. Repository and audit adapters must share the same transaction scope.

**Go (products application):**

```go
// internal/application/usecase/products_test.go
func TestCreate_ReturnsTheOriginalProductWhenTheKeyIsRepeated(t *testing.T) {
    uc := NewProducts(newFakeProducts(), &sequentialIDs{})
    cmd := in.CreateProductCommand{IdempotencyKey: "key-12345", Name: "Mouse", PriceCents: 45_990_00, CategoryID: "c-1"}

    first, _ := uc.Create(context.Background(), cmd)
    second, err := uc.Create(context.Background(), cmd)

    require.NoError(t, err)
    assert.False(t, second.Created)
    assert.Equal(t, first.ProductID, second.ProductID)
}
```

A hand-written fake of `synkro-sales-api`'s `SaleRepository` port is
written exactly the same way — it holds sales in a map, keyed by
`idempotencyKey`, and needs nothing from MongoDB to exist.

---

## Tier 2 — HTTP and integration tests

**Objective:** verify the adapters. The HTTP adapter is tested in memory; the
persistence adapter is tested against a real store. A fake repository only
proves that the fake was called: it does not prove that the query is valid, that
the mapping keeps the types or that the migration or validator exists.

| Level | What it verifies | What it needs |
|-------|------------------|---------------|
| HTTP | every variant of `401`, the error envelope and correlation, field validation, idempotent retry, page limit | the server in memory, with the in-memory repository |
| Integration (PostgreSQL domains) | round trip, rollback of the idempotency key, update and page | PostgreSQL with the schema of the `-db` repository, through `TEST_DATABASE_URL` |
| Integration (Sales, MongoDB) | round trip, the unique-index rejection on a repeated `idempotencyKey`, the validator's rejection of a malformed document | a MongoDB replica set with the `sale` collection and its validator applied by `synkro-sales-db`'s migrations, through `TEST_MONGODB_URI` |

| Aspect | Java (Spring Boot) | Go (PostgreSQL) | Go (Sales, MongoDB) |
|--------|-------------------|-----|-----|
| HTTP tests | `@SpringBootTest(webEnvironment = RANDOM_PORT)` in `<domain>-app`, with the in-memory repository | `net/http/httptest` in `internal/adapter/in/httpapi/` | Same as Go (PostgreSQL) — the HTTP adapter does not know which engine backs it |
| Integration tests | `<Class>IntegrationTest` in `<domain>-adapters`, enabled only when `TEST_DATABASE_URL` is defined | `postgres_integration_test.go`, skipped with `t.Skip` when `TEST_DATABASE_URL` is empty | `mongo_integration_test.go`, skipped with `t.Skip` when `TEST_MONGODB_URI` is empty |
| Speed | 1–5 s per test | 1–5 s per test | 1–5 s per test |
| When they run | HTTP: on every push; integration: in CI on every PR, where PostgreSQL and the `-db` migrations are available | Same | HTTP: on every push; integration: in CI on every PR, where the MongoDB replica set and `synkro-sales-db`'s Liquibase migrations are available |

### What to test

- The HTTP behavior of every service, which is verified by HTTP and not by language: `/health` without a token, `401` for a missing, expired, badly signed or non-RS256 token, the error envelope with the same `X-Correlation-Id`, `400` with one `details` entry per invalid field, `201` with `Location` and `200` with the same id on a repeated `Idempotency-Key`, a bounded page (`limit` above 100 is `400`)
- Repository adapters: CRUD against a real PostgreSQL schema, or in Sales' case, insert/find against the real `sale` collection
- Flyway migrations: the CI rebuild check (drop, rebuild, rollback, rebuild), in each PostgreSQL `-db` repository
- Liquibase changesets: the equivalent rebuild check for `synkro-sales-db` — see "CI pipeline per repository type" below

### What NOT to test here

- Domain logic (covered by unit tests)
- Other services: the outgoing client is a port, so it is replaced by a fake

### Folder structure

**Java:**

```
auth-adapters/src/test/java/co/edu/corhuila/synkro/auth/adapter/out/persistence/
└── JdbcUserRepositoryIntegrationTest.java

auth-app/src/test/java/co/edu/corhuila/synkro/auth/app/
└── AuthHttpTest.java
```

**Go (PostgreSQL domains):**

```
internal/adapter/
├── in/httpapi/handler_test.go                       // httptest.NewServer + in-memory repository
└── out/persistence/postgres_integration_test.go     // skipped when TEST_DATABASE_URL is not set
```

**Go, `synkro-sales-api` (MongoDB):**

```
internal/adapter/
├── in/httpapi/handler_test.go                       // identical pattern — httptest + in-memory repository
└── out/persistence/mongo_integration_test.go         // skipped when TEST_MONGODB_URI is not set
```

### Example — repository integration test

**Java:**

```java
// auth-adapters/src/test/java/…/adapter/out/persistence/JdbcUserRepositoryIntegrationTest.java
@EnabledIfEnvironmentVariable(named = "TEST_DATABASE_URL", matches = ".+")
class JdbcUserRepositoryIntegrationTest {

    @Test
    void should_persist_and_find_by_email() {
        var repository = repositoryOver(System.getenv("TEST_DATABASE_URL")); // JdbcTemplate over that URL (helper omitted)
        var user = SystemUser.create("Alice", uniqueEmail(), "hash", "ADMIN");
        repository.save(user);

        var found = repository.findByEmail(user.email());
        assertThat(found).isPresent();
        assertThat(found.get().role()).isEqualTo("ADMIN");
    }
}
```

**Go (PostgreSQL):**

```go
// internal/adapter/out/persistence/postgres_integration_test.go
func TestCreateOnce_ReturnsTheOriginalProductWhenTheKeyIsRepeated(t *testing.T) {
    url := os.Getenv("TEST_DATABASE_URL")
    if url == "" {
        t.Skip("TEST_DATABASE_URL is not set")
    }
    repo := NewPostgres(openDB(t, url)) // the schema comes from synkro-products-db (helper omitted)

    first, created, err := repo.CreateOnce(context.Background(), "key-12345", newProduct(t))
    require.NoError(t, err)
    require.True(t, created)

    again, created, err := repo.CreateOnce(context.Background(), "key-12345", newProduct(t))
    require.NoError(t, err)
    assert.False(t, created)
    assert.Equal(t, first, again)
}
```

**Go, `synkro-sales-api` (MongoDB):**

```go
// internal/adapter/out/persistence/mongo_integration_test.go
func TestCreateOnce_ReturnsTheOriginalSaleWhenTheKeyIsRepeated(t *testing.T) {
    uri := os.Getenv("TEST_MONGODB_URI")
    if uri == "" {
        t.Skip("TEST_MONGODB_URI is not set")
    }
    repo := NewMongo(openDatabase(t, uri)) // the "sales" database comes from synkro-sales-db (helper omitted)

    first, created, err := repo.CreateOnce(context.Background(), "key-12345", newSale(t))
    require.NoError(t, err)
    require.True(t, created)

    again, created, err := repo.CreateOnce(context.Background(), "key-12345", newSale(t))
    require.NoError(t, err)
    assert.False(t, created)
    assert.Equal(t, first, again)
}

func TestCreateOnce_RejectsADocumentTheValidatorDoesNotAccept(t *testing.T) {
    uri := os.Getenv("TEST_MONGODB_URI")
    if uri == "" {
        t.Skip("TEST_MONGODB_URI is not set")
    }
    repo := NewMongo(openDatabase(t, uri))

    sale := newSale(t)
    sale.Details = append(sale.Details, make([]SaleDetail, 100)...) // 101 lines total

    _, _, err := repo.CreateOnce(context.Background(), "key-67890", sale)
    require.Error(t, err) // rejected by the collection's $jsonSchema validator (ADR-010 Decision 2)
}
```

---

## Tier 3 — Contract tests

**Objective:** verify that the service's HTTP responses match its
OpenAPI contract in `07-api/contracts/openapi/`.

| Aspect | Java (Spring Boot) | Go |
|--------|-------------------|-----|
| Tool | Spring MockMvc + openapi-diff or Schemathesis | Schemathesis (runs against the test server) |
| What they verify | Response shapes, status codes, required fields match the YAML | Same |
| When they run | In CI on every PR | Same |

### What to verify

- Every endpoint declared in the contract returns the documented status codes
- Response bodies match the declared schemas (required fields, types)
- Error responses use the closed catalog from `_shared.yaml`

### What NOT to verify here

- Business logic (covered by unit tests)
- Database or document-store state (covered by integration tests)

---

## Tier 4 — E2E tests

**Status:** real-service/process/browser acceptance has reported execution evidence, with exact scope and limits in the index below. This is not an all-green composed-platform certification; HU-INF-02/#239 remains open. The Worker low-stock business flow is still missing.

**Priority E2E flows:**

| # | Flow | Services involved |
|---|------|------------------|
| 1 | Login and token refresh | `synkro-auth-api` |
| 2 | Sale registration saga (happy path) | `synkro-workflow` → `synkro-customers-api` → `synkro-products-api` → `synkro-sales-api` |
| 3 | Sale registration saga (compensation) | Same — stock released after sales-api failure |
| 4 | Low-stock alert job | `synkro-worker` → `synkro-products-api` |

---

## Frontend tests

**Objective:** the same test-first discipline as the backend, applied to
`synkro-front` (the host) and the four portals — three React remotes and
one Angular custom element (`synkro-customers-portal`, ADR-011). A
portal's component/unit tests never make a real network call and never start the
backend: they run against a double of the contract the host hands the
portal (`ShellContract`), the same type whether the portal is React or
Angular. Separate browser acceptance may use real services and is labeled by its actual scope.

| Aspect | React (host, 3 portals) | Angular (Customers portal) |
|---|---|---|
| Test runner | Vitest | Vitest with Angular TestBed; canonical tooling guidance is in `_stacks/frontend.md` |
| Component tests | Testing Library | Angular's component testing utilities |
| Doubles | A hand-written fake implementing `ShellContract`'s `api.request`, `session.user()` and `navigate()` | Same fake, injected as the Angular service that wraps the contract |
| Speed | < 50 ms per test | < 50 ms per test |
| When they run | On every push and in CI | Same |

### What a portal's tests cover

- **The four view states** of every screen that lists or shows data: loading, error with retry, empty, and data — exercised by resolving or rejecting the `ShellContract` fake's `api.request` call differently per test.
- **Form behavior:** a required field left empty blocks submission without calling `api.request`; a field-level error returned in `details` renders next to its field; the idempotency key sent on the first attempt is reused verbatim on a retry after a failure (never regenerated).
- **Money conversion:** typed text (`"0.07"`) becomes integer minor-unit cents (`7`) before it reaches `api.request` — never a multiplied decimal.
- **Newest-request-wins:** two calls fired in quick succession (e.g. a fast search) resolve with the UI reflecting only the result of the one started last.

### What the host's tests cover

- **`apiClient`:** every outgoing call carries a fresh `X-Correlation-Id` and the configured gateway address; protected calls use the session gate and a protected `401` closes the session without refresh-and-replay. Public login/refresh use operation-specific outcomes. Requests past their timeout fail as `TIMEOUT`; errors are mapped in one place.
- **Session:** no session redirects a protected route to sign-in and returns to the original route afterward; in `develop`, pasting a valid development token starts a session with its `sub` and `role`, and an expired or malformed one is rejected with no session created.
- **Remote isolation:** a portal whose remote entry file fails to load renders `RemoteBoundary`'s "not available" notice, and the rest of the host's layout keeps working — exercised with a mocked failing `import()`, not a real stopped portal server.

Both the host's and a portal's component/unit tests run against **simulated HTTP
responses** (constructed in the test, or through a fetch double), never
against a running backend service and never against the `develop`
mock-server profile described in `deployment.md` §10 — that profile is
for manual and visual verification while a backend doesn't exist yet,
not for the automated test suite.

### Planned production login and session scenarios

These scenario definitions guide production login/session verification. HU-FE-08/#248 and the [E-HOST](#e-host)/[E-AUTH-PORTAL](#e-auth-portal) families now provide actual tests; no one-to-one fully real coverage is inferred from those families. The renewal margin and coordination policy remain [proposed guidance](../07-api/authentication.md#renewal-policy-proposal-and-decision-status) pending TD-010. Use simulated HTTP, a controllable clock and visibility/focus events for component tests; distinguish real browser acceptance.

| ID | Required verification |
|---|---|
| FE-AUTH-01 | Auth portal login success uses the host operation; tokens stay in the host; navigation returns to an authorized local destination. |
| FE-AUTH-02 | Login `401` shows generic credentials error and clears the password; refresh count is zero, login is not retried, no session is created. |
| FE-SESSION-01 | More than 60 seconds before `exp`: no refresh. At or below 60 seconds: renew before protected dispatch; success replaces both tokens and schedules the next check. |
| FE-SESSION-02 | Simulate a suppressed background timer and return to visible/focus with near-expired or expired access token: refresh gates protected work. Without refresh credentials, require login. |
| FE-SESSION-03 | Concurrent timer, focus and request triggers share exactly one refresh; successful waiters use the replacement token. No cross-tab guarantee is claimed. |
| FE-SESSION-04 | Refresh `401` clears session/storage and requires login without recursion; protected `401` ends the session without refresh or request replay; `403` keeps the session and shows access denied. |
| FE-SESSION-05 | Logout during login/refresh invalidates the old generation; late responses do not restore tokens, session or timers. |
| FE-SESSION-06 | Login network/`5xx`/`429` is not a credential error; respect rate-limit wait. Indeterminate refresh failure gates protected work and does not blindly reuse a rotating token. |
| FE-SESSION-07 | Reload with only a refresh credential restores through the host; absent/rejected credentials require login. Browser `exp` checks do not replace service-side validation. |

### Planned backend Auth credential lifecycle scenarios

These complement the frontend scenarios. [E-AUTH-SESSION](#e-auth-session) and [E-AUTH](#e-auth) now identify implementation tests and exact run evidence; the scenario definitions alone are not execution evidence. Follow the Auth contract and accepted security constraints; [TD-011](../15-project-control/technical-backlog.md#td-011--clarify-reuse-detection-and-stateless-jwt-revocation) must settle any broader revocation assumption before a test asserts that behavior.

| ID | Required verification / boundary |
|---|---|
| AUTH-REFRESH-01 | Successful public refresh consumes the old credential and issues one new pair; only a credential hash is persisted. No authorization header is required for refresh. |
| AUTH-REFRESH-02 | Invalid, expired or already-used refresh credentials return 401 UNAUTHORIZED without exposing account existence or raw persistence details. |
| AUTH-REFRESH-03 | A used credential is recognized as reuse, not physically discarded before security lookup. Assert the accepted server-side invalidation scope once TD-011 is resolved; do not invent token families or an access-JWT denylist. |
| AUTH-LOGOUT-01 | An authenticated human logs out with the submitted refresh credential: 200, invalidation and repeat no-op; the access JWT expires normally rather than being immediately revoked. |
| AUTH-LOGOUT-02 | Missing/invalid caller authentication gives 401; a SERVICE caller gives 403. Preserve the existing contract's allowed human roles. |
| AUTH-SECRETS-01 | Credential hashes never appear in response bodies; passwords, hashes and tokens do not appear in logs/audit evidence. Token strings may appear only in the documented successful token-issuance response fields. |

Auth implementation stories provide test-file and run references in the evidence index. Real persistence checks belong to the Auth database integration tier; HU-DOCS-94 changes no migrations or production code.

### No fixed data in production code

Exactly one place is allowed to hold data that stands in for a real
answer: the test file itself. A portal's or the host's non-test source
never contains a hardcoded list of customers, products or sales "to make
the screen show something" — if a screen needs to render during manual
development before its backend or the mock profile is up, that is what
the empty state is for.

### Frontend coverage thresholds

| Scope | Minimum coverage | Measured by |
|---|---|---|
| Host's `apiClient` and session modules | ≥ 85% statements | Vitest coverage (`v8` provider) |
| A portal's components and hooks/services | ≥ 70% statements | Same tool as the portal's framework |
| Global, per frontend repository | ≥ 70% statements | Same |

These are proposed defaults, set lower than the backend's line-coverage
thresholds because UI composition code has a different risk profile than
domain logic; the host's client and session code get the backend-like
85% because a bug there affects every portal at once. The ratification/revision decision remains TD-008; existing tests do not silently lower a threshold.

---

## Development-time verification against simulated services

While a backend service does not exist yet, a portal is exercised two
ways, and neither substitutes for the tiers above:

1. **Its own tests** (this section), against a `ShellContract` fake — these are what CI runs and what the coverage gate measures.
2. **Manual, visual verification in `develop`**, against the contract-driven mock servers behind the gateway (`deployment.md` §10) — useful to see a screen with realistic-shaped data before the real service exists, but not automated and not part of this strategy's coverage or CI gates.

A mock server is removed from `develop` in the same pull request that
delivers the real service (`deployment.md` §10); nothing here changes
when that happens, since the portal's tests never depended on the mock
in the first place.

---

## CI pipeline per repository type

> CI is configured. The examples below describe required check families;
> actual repository workflows and the dated run results are the evidence.
> A failed, cancelled or unstarted run is not a passing test suite.

### Domain services (`-api`, `synkro-workflow`, `synkro-worker`)

```yaml
# .github/workflows/ci.yml (simplified)
jobs:
  unit-and-http-tests:
    steps:
      - run: mvn -B test        # Java; the integration tests are skipped
      # or: go test ./...       # Go; the integration tests are skipped

  integration-tests:
    services:
      postgres:
        image: postgres:16-alpine
    steps:
      - run: # apply the migrations of the -db repository to the CI instance (Flyway runner)
      - run: TEST_DATABASE_URL=… mvn -B test      # Java
      # or: TEST_DATABASE_URL=… go test ./...     # Go

  contract-tests:
    steps:
      - run: schemathesis run --validate-schema synkro-products-api.yaml

  coverage-gate:
    steps:
      - run: # fail if global < 80% or domain < 90%
```

`synkro-sales-api`'s pipeline follows the same four jobs, with one
difference in `integration-tests`: the service container is `mongo`
instead of `postgres`, the migrations applied beforehand are
`synkro-sales-db`'s Liquibase changesets instead of Flyway, and the test
run exports `TEST_MONGODB_URI` instead of `TEST_DATABASE_URL`. The
`unit-and-http-tests`, `contract-tests` and `coverage-gate` jobs are
unchanged, because they never touch the database engine.

### Database repositories (`-db`) — Auth, Customers, Products (Flyway)

```yaml
jobs:
  migration-rebuild:
    services:
      postgres:
        image: postgres:16-alpine
    steps:
      - run: flyway migrate          # build from V001
      - run: flyway undo -target=0   # rollback all U scripts
      - run: flyway migrate          # rebuild — must apply zero changes
      - run: flyway validate         # checksums match
```

### Database repository (`-db`) — Sales (Liquibase, MongoDB)

```yaml
jobs:
  migration-rebuild:
    services:
      mongo:
        image: mongo:7.0
    steps:
      - run: liquibase update                       # build from empty
      - run: liquibase update                        # apply again — must apply zero changesets
      - run: liquibase rollback-count --count=999999  # roll back every changeset
      - run: liquibase update                        # rebuild from empty again
      - run: # insert one document matching the sale schema — must succeed
      - run: # attempt an undeclared-field document, an out-of-range value, and a 101-line sale — each must be rejected by the $jsonSchema validator
```

The same "build, repeat with zero changes, roll back, rebuild" shape as
the Flyway check above; the last two steps are specific to Sales,
because only a document store has a validator to confirm, separately
from the migration tool itself (ADR-010 Decision 5).

### Gateway (`synkro-api-gateway`)

```yaml
jobs:
  smoke-test:
    steps:
      - run: docker compose up -d synkro-api-gateway
      - run: curl -f http://localhost:8000/health
      - run: # verify route config syntax (nginx -t)
```

### Frontend (`synkro-front`, portals)

```yaml
jobs:
  build-and-lint:
    steps:
      - run: npm ci
      - run: npm run lint
      - run: npm run build           # a portal that doesn't build is a broken portal

  unit-tests:
    steps:
      - run: npm run test -- --coverage   # required — see "Frontend tests" above

  coverage-gate:
    steps:
      - run: # fail if the repository's coverage is below the thresholds in "Frontend tests"
```

A frontend repository's pipeline is no longer allowed to skip its test
job: every portal and the host carry real tests from their first
implementation story, per "Frontend tests" above.

---

## Test data conventions

### Fixtures

Each service keeps its test fixtures alongside the tests:

**Java:**

```
auth-core/src/test/java/co/edu/corhuila/synkro/auth/application/usecase/
├── FakeUserRepository.java              // implements the UserRepository port
├── FakePasswordHasher.java              // implements the PasswordHasher port
└── SystemUserFixture.java               // builder for test SystemUser instances
```

**Go:**

```
internal/application/usecase/products_test.go   // fakeProducts and sequentialIDs, next to the test
internal/adapter/out/persistence/memory.go      // in-memory repository, shared by the HTTP tests
```

`synkro-sales-api` follows the identical Go pattern: its fake and its
`memory.go` hold `Sale` structs in a map, with no reference to MongoDB.

**Frontend:**

```
src/apiClient.ts                                 // production code — no fixed data
src/apiClient.test.ts                            // the ShellContract fake lives here, next to its test
src/__fixtures__/customers.ts                     // sample data shapes, imported only by *.test.ts files
```

### Test database

Integration tests against the PostgreSQL domains run against the
instance that `TEST_DATABASE_URL` points to, with the schema created by
the migrations of the `-db` repository. Integration tests against Sales
run against the MongoDB replica set that `TEST_MONGODB_URI` points to,
with the `sale` collection and its validator created by
`synkro-sales-db`'s migrations. In CI both are service containers to
which the migrations are applied before the tests; locally they can be
the instances `synkro-infra-postgres` and `synkro-infra-mongo` start
(`deployment.md` §9) or any instance with that schema or collection.
Each test uses its own data (unique values) and no test uses the `qa` or
`main` instances. Without the matching environment variable, the
integration tests are skipped instead of failing.

### Money in tests

All money values in tests use minor units (`priceCents = 4599_00` in
PostgreSQL domains, `unitPriceCents` as a `long` in Sales), consistent
with ADR-005 Decision 3. The underscore separator improves readability
without changing the value.

---

## Correlations

- TDD process (red → green → refactor), including the frontend → `11-quality/tdd-guide.md`
- Hexagonal layer structure → `05-architecture/hexagonal-architecture.md`
- OpenAPI contracts to validate → `07-api/contracts/openapi/`
- Coverage threshold in the DoD → `00-governance/definition-of-done.md`
- CI pipeline configuration → `10-devops/ci-cd.md`
- Migration rebuild check (PostgreSQL) → `05-architecture/deployment.md` §5; Sales' Liquibase check → ADR-010 Decision 5
- Development-time mock servers → `05-architecture/deployment.md` §10
- Gateway and frontend stack → ADR-008
- Sales domain on MongoDB → ADR-010
- Angular Customers portal → ADR-011
- Frontend structure and tooling (once written) → `_stacks/frontend.md`

## ADR-014 verification plan — not executed evidence

> The table defines verification scenarios. AUTH-12/13/14 implementations
> now have linked E-AUTH-DB, E-AUTH and E-AUTH-INIT evidence below.
> AUTH-UI-01's administration screen remains pending. This plan is not itself a test run.

| Planned test ID | Level / required verification |
|---|---|
| AUTH-REG-01 | Unit: ADMIN creates each allowed role; trusted caller required; password hashed |
| AUTH-REG-02 | HTTP: missing, expired, malformed or invalid-signature token gives 401; valid non-ADMIN/SERVICE gives 403 |
| AUTH-REG-03 | HTTP/contract: authorized first request 201 + Location; replay 200; duplicate email 422; SERVICE role rejected |
| AUTH-REG-04 | Real PostgreSQL: user/key/audit atomic commit, audit-write failure rollback and no duplicated audit on replay |
| AUTH-INIT-01 | Real PostgreSQL: two synchronized connections compete for the same lock; one creates and the other observes the committed ADMIN |
| AUTH-INIT-02 | Real PostgreSQL: existing active/inactive ADMIN produces a no-op without changing password or audit count |
| AUTH-INIT-03 | Real PostgreSQL: failure after user insertion rolls back user/audit; authorized retry succeeds; timeout never inserts unlocked |
| AUTH-AUDIT-01 | Database effective privileges: auth_app may SELECT/INSERT audit but not UPDATE/DELETE/TRUNCATE; other domains have no access |
| AUTH-UI-01 | Portal: ADMIN form, shared client/token, reused key on retry; 401 ends session; 403 does not retry automatically |

Write the tests before production implementation. For AUTH-INIT-01 use barriers
to ensure overlapping attempts, separate connections, explicit READ COMMITTED,
the centrally reserved key and a separate existence query after lock acquisition.
Do not count mock serialization or sequential calls as concurrency evidence.
Verify no password/hash/token/raw payload leaks and operator attribution for
initialization. Historical-role loss is an operational recovery boundary, not a
lifetime lock guarantee. Existing coverage thresholds remain unchanged.

---

## Evidence levels and interpretation

| Level | What it proves | Boundary |
|---|---|---|
| Unit / capability fake | Domain/use-case or UI behavior in isolation | No real persistence or participant acceptance |
| HTTP adapter / signed token | Boundary, envelope, validation, authorization | A fake repository does not prove database behavior |
| Real-engine integration | Actual migrations, mapping, concurrency and permissions | Uses the recorded disposable engine/credentials |
| Controlled process recovery | Real JVM/process/store under controlled participant faults | Controlled participants are identified |
| Real-service integration | Actual participants and recorded effects | Identify revisions and any fault interception/fixture |
| Browser acceptance | User flow through delivered UI/Host and services | Identify real transactions versus transport doubles/faults |
| Root composition | Specific locked revisions together | Partial runs and reused results do not prove every final-head criterion |

A run reference must include its revision, level, outcome and limitations. A matching source tree can justify source equivalence only; dependency/configuration/artifact equality must also be established before execution evidence is reused.

Remote CI success, local reported success, failure, cancellation, skip, startup_failure and NOT EXECUTED are distinct outcomes. Billing-locked jobs with zero steps prove neither passing nor failing tests. Prior successful implementation CI remains valid for that original revision when a later release cannot run.

## Scenario-to-implementation reconciliation

| Design scenario family | Delivered evidence family | Remaining boundary |
|---|---|---|
| AUTH-REG-01…04; AUTH-AUDIT-01 | E-AUTH, E-AUTH-DB | Exact test families below; central audit/contract details are coordinated with HU-DOCS-95 |
| AUTH-INIT-01…03 | E-AUTH-INIT | CLI and PostgreSQL tests exist; this is initialization, not recovery |
| AUTH-REFRESH-01…03; AUTH-LOGOUT-01/02; AUTH-SECRETS-01 | E-AUTH-SESSION, E-AUTH | Scope is the implemented token classes; TD-011 remains explicit |
| FE-AUTH-01/02 | E-HOST, E-AUTH-PORTAL | Unit/capability tests and separate real login reports |
| FE-SESSION-01…07 | E-HOST | Families exist; no one-to-one fully real coverage or accepted renewal ADR is invented |
| WKF-01…15 | E-WORKFLOW | Core/JDBC/HTTP/process scopes remain separate; central contract decisions need HU-ARQ-28/HU-DOCS-95 |
| WKF-16 | E-WORKFLOW and root candidate evidence | Later real acceptance supersedes old Customers 501; retain exact revisions and review boundaries |
| CLI-DB-01…14 | E-CUSTOMERS-DB | Component migration/permission evidence, not automatic root acceptance |
| Sales registration/history/detail | E-SALES-PORTAL | Reported 18/18 browser checks differ from the root harness whose E2E-02 is NOT EXECUTED |
| Low-stock automation | E-WORKER plus E-ALERTS | Scheduler and alert API do not implement the missing Worker job |
| Reporting UI | E-REPORTS | Aggregation APIs exist; dashboard/reporting screens remain pending |

The scenario definitions in the preceding sections guide verification; use this index for current implementation evidence. AUTH-UI-01's user-administration screen is still pending; real login delivery must not be used as its completion.

## Domain and contract reconciliation checks (HU-DOCS-95)

These are document/contract conformance checks and links to existing implementation tests. HU-DOCS-95 does not rerun the production suites or claim missing regression tests passed. The OpenAPI review candidate for Workflow remains dependent on HU-ARQ-28.

| Check family | Existing source/test evidence | Required or checked boundary |
|---|---|---|
| PRO-RECEIPT | [internal/adapter/out/persistence/postgres_stock_integration_test.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/adapter/out/persistence/postgres_stock_integration_test.go); [internal/application/usecase/stock_test.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/application/usecase/stock_test.go) | Existing tests distinguish original resulting-stock replay after later movement and pre-V017 NULL fallback; schema example checks do not rerun PostgreSQL |
| SALES-REPRESENTATION | [internal/adapter/out/persistence/sale_document.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/out/persistence/sale_document.go); [internal/adapter/out/persistence/mongo_integration_test.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/out/persistence/mongo_integration_test.go) | Current decoder rejects inactive roots/details; TD-012's faithful-restoration regressions remain closing evidence to supply after the domain decision |
| SALES-HTTP-ERRORS | [internal/adapter/in/httpapi/handler.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/in/httpapi/handler.go); [internal/adapter/in/httpapi/persistence_errors_test.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/in/httpapi/persistence_errors_test.go) | Generic 500, canonical UUID/no detail query 400; no driver data in response |
| SALES-REPORTS | [internal/adapter/out/persistence/reports.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/out/persistence/reports.go); [internal/adapter/in/httpapi/reports_test.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/in/httpapi/reports_test.go) | UTC inclusive days/exclusive next midnight, owner filter from token, grouped pagination, stable ties, int64 range/overflow handling |
| WKF-REPLAY / REJECTION | [workflow-app/src/test/java/co/edu/corhuila/synkro/workflow/app/WorkflowHttpTest.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-app/src/test/java/co/edu/corhuila/synkro/workflow/app/WorkflowHttpTest.java); [workflow-adapters/src/test/java/co/edu/corhuila/synkro/workflow/adapter/out/http/HttpSaleParticipantsTest.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-adapters/src/test/java/co/edu/corhuila/synkro/workflow/adapter/out/http/HttpSaleParticipantsTest.java) | Existing caller/conflicting/equivalent replay and malformed-rejection cases remain distinct from accepted architectural authority |
| LOCK / RETRY / CIRCUIT | [workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/out/persistence/JdbcSagaStore.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/out/persistence/JdbcSagaStore.java); [workflow-app/src/main/java/co/edu/corhuila/synkro/workflow/app/WorkflowComposition.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-app/src/main/java/co/edu/corhuila/synkro/workflow/app/WorkflowComposition.java); [workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/out/http/HttpSaleParticipants.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/out/http/HttpSaleParticipants.java) | TD-014/015/016 need their own closing concurrency/classifier/jitter/state-machine evidence; never infer them from generic timeout tests |
| HEALTH-URL / PRODUCTS-STORE | Five health operation servers; [internal/adapter/in/httpapi/handler.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/adapter/in/httpapi/handler.go) | Static effective URL must be /health, with security []; Products store-sensitive 503 remains unimplemented (TD-017) |
| PHYSICAL-SCHEMA | [Delivered migration inventory and literal excerpts](../06-data/models.md#domain-products); [TD-018](../15-project-control/technical-backlog.md#td-018--products-physical-schema-conventions) | Source comparison identifies missing reader, naming/enforcement and explicit-FK differences; closure requires reviewed forward migrations and fresh/upgrade/permission execution evidence, which this documentation review did not run |

A new verification run records its actual revision/outcome. Until then, an example passing schema validation means only that it conforms to the schema, not that a service emitted it, a workflow is fully accepted or an ADR is ratified.

## Executable evidence index

Each link pins a source commit. The [verification snapshot](../15-project-control/documentation-verification.md#repository-revision-snapshot) freezes all 18 main/develop/tag revisions and latest observed CI. Existence of a test file is not an assertion that it was executed by this preparation. Implementation references identify the behavior exercised by the test. A cross-component or process test can list several participating files; it is not presented as a direct unit test of every listed file.

<a id="e-customers"></a>

### E-CUSTOMERS — Customers lifecycle

**Trace:** FR-001; NFR-002/004/006/007/009 → HU-CLI-01, HU-CLI-02, HU-CLI-03, HU-CLI-04 → tests → implementation.

| Test file | Implementation reference |
|---|---|
| [customers-core/src/test/java/co/edu/corhuila/synkro/customers/application/usecase/CustomerServiceTest.java](https://github.com/code-corhuila/synkro-customers-api/blob/28caff4f2d4c5e562416db7400061675e4b6766b/customers-core/src/test/java/co/edu/corhuila/synkro/customers/application/usecase/CustomerServiceTest.java) | [customers-core/src/main/java/co/edu/corhuila/synkro/customers/application/usecase/CustomerService.java](https://github.com/code-corhuila/synkro-customers-api/blob/28caff4f2d4c5e562416db7400061675e4b6766b/customers-core/src/main/java/co/edu/corhuila/synkro/customers/application/usecase/CustomerService.java) |
| [customers-app/src/test/java/co/edu/corhuila/synkro/customers/app/LifecycleHttpTest.java](https://github.com/code-corhuila/synkro-customers-api/blob/28caff4f2d4c5e562416db7400061675e4b6766b/customers-app/src/test/java/co/edu/corhuila/synkro/customers/app/LifecycleHttpTest.java) | [customers-adapters/src/main/java/co/edu/corhuila/synkro/customers/adapter/in/http/CustomerController.java](https://github.com/code-corhuila/synkro-customers-api/blob/28caff4f2d4c5e562416db7400061675e4b6766b/customers-adapters/src/main/java/co/edu/corhuila/synkro/customers/adapter/in/http/CustomerController.java); [customers-adapters/src/main/java/co/edu/corhuila/synkro/customers/adapter/in/http/HealthController.java](https://github.com/code-corhuila/synkro-customers-api/blob/28caff4f2d4c5e562416db7400061675e4b6766b/customers-adapters/src/main/java/co/edu/corhuila/synkro/customers/adapter/in/http/HealthController.java) |
| [customers-app/src/test/java/co/edu/corhuila/synkro/customers/app/JdbcPersistenceTest.java](https://github.com/code-corhuila/synkro-customers-api/blob/28caff4f2d4c5e562416db7400061675e4b6766b/customers-app/src/test/java/co/edu/corhuila/synkro/customers/app/JdbcPersistenceTest.java) | [customers-adapters/src/main/java/co/edu/corhuila/synkro/customers/adapter/out/persistence/JdbcCustomerRepository.java](https://github.com/code-corhuila/synkro-customers-api/blob/28caff4f2d4c5e562416db7400061675e4b6766b/customers-adapters/src/main/java/co/edu/corhuila/synkro/customers/adapter/out/persistence/JdbcCustomerRepository.java) |

LifecycleHttpTest also checks public GET /health with a real disposable PostgreSQL database. Its HTTP lifecycle reaches the controller and persistence adapters; JdbcPersistenceTest supplies direct repository evidence. These current tests supersede the initial scaffold checks without retroactively claiming execution at the original scaffold revision. Current main CI success; joint real-service acceptance is reported by the owner. Component delivery does not close root HU-INF-02.

<a id="e-products"></a>

### E-PRODUCTS — Products and categories

**Trace:** FR-002/003; NFR-002/004/006/009 → HU-PRO-03, HU-PRO-05, HU-PRO-08, HU-PRO-01, HU-PRO-02, HU-PRO-11, HU-PRO-12, HU-PRO-13 → tests → implementation.

| Test file | Implementation reference |
|---|---|
| [internal/adapter/in/httpapi/handler_test.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/adapter/in/httpapi/handler_test.go) | [internal/adapter/in/httpapi/handler.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/adapter/in/httpapi/handler.go) |
| [internal/application/usecase/products_test.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/application/usecase/products_test.go) | [internal/application/usecase/products.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/application/usecase/products.go) |
| [internal/application/usecase/categories_test.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/application/usecase/categories_test.go) | [internal/application/usecase/categories.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/application/usecase/categories.go) |
| [internal/adapter/in/httpapi/catalog_test.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/adapter/in/httpapi/catalog_test.go) | [internal/adapter/in/httpapi/products.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/adapter/in/httpapi/products.go); [internal/adapter/in/httpapi/categories.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/adapter/in/httpapi/categories.go) |

handler_test.go checks public GET /health through the current router fixture. The inspected production handler has no store probe and always returns liveness 200; required store-sensitive 503 is an open TD-017 discrepancy, not a delivered availability check. catalog_test.go exercises both product and category HTTP adapters. Source and tests are published. Current main CI is failure; this preparation does not report a new successful run.

<a id="e-stock"></a>

### E-STOCK — Stock and reservations

**Trace:** FR-004/007; NFR-009 → HU-PRO-06, HU-PRO-09, HU-WKF-03 → tests → implementation.

| Test file | Implementation reference |
|---|---|
| [internal/application/usecase/stock_test.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/application/usecase/stock_test.go) | [internal/application/usecase/stock.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/application/usecase/stock.go) |
| [internal/adapter/out/persistence/postgres_stock_concurrency_test.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/adapter/out/persistence/postgres_stock_concurrency_test.go) | [internal/adapter/out/persistence/postgres_stock.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/adapter/out/persistence/postgres_stock.go) |

Real sale/compensation effects are reported by Workflow/Sales Portal. Keep those combinations separate from a new owner CI run.

<a id="e-alerts"></a>

### E-ALERTS — Stock-alert API

**Trace:** FR-004; NFR-004/009 → HU-PRO-07, HU-PRO-10, HU-PRO-12 → tests → implementation.

| Test file | Implementation reference |
|---|---|
| [internal/application/usecase/stock_alert_test.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/application/usecase/stock_alert_test.go) | [internal/application/usecase/stock_alert.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/application/usecase/stock_alert.go) |
| [internal/adapter/in/httpapi/stock_alert_test.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/adapter/in/httpapi/stock_alert_test.go) | [internal/adapter/in/httpapi/stock_alert.go](https://github.com/code-corhuila/synkro-products-api/blob/8c04934ae5b51417a0b88506cc59734ef7913e64/internal/adapter/in/httpapi/stock_alert.go) |

Alert endpoints and the portal exist. The Worker low-stock job remains missing; scheduler tests do not prove alert production.

<a id="e-sale"></a>

### E-SALE — Sale aggregate and registration

**Trace:** FR-005/006; NFR-002/004/006/009 → HU-VEN-03, HU-VEN-06, HU-VEN-07, HU-VEN-09 → tests → implementation.

| Test file | Implementation reference |
|---|---|
| [internal/domain/model/sale_test.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/domain/model/sale_test.go) | [internal/domain/model/sale.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/domain/model/sale.go) |
| [internal/application/usecase/register_sale_test.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/application/usecase/register_sale_test.go) | [internal/application/usecase/register_sale.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/application/usecase/register_sale.go) |
| [internal/adapter/in/httpapi/sales_test.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/in/httpapi/sales_test.go) | [internal/adapter/in/httpapi/handler.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/in/httpapi/handler.go) |

sales_test.go includes TestRouterErrorsAndHealth: it checks GET /health with the fixture and the unhealthy-store response; this is separate from real MongoDB integration. Published source and later real-service reports cover integer totals, bounds, idempotency and authorization. Current main CI is failure.

<a id="e-mongo"></a>

### E-MONGO — MongoDB sale persistence

**Trace:** FR-005/006; NFR-004/007/009 → HU-VEN-04, HU-VEN-05, HU-VEN-08, HU-VEN-16, HU-VEN-17 → tests → implementation.

| Test file | Implementation reference |
|---|---|
| [internal/adapter/out/persistence/mongo_integration_test.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/out/persistence/mongo_integration_test.go) | [internal/adapter/out/persistence/mongo.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/out/persistence/mongo.go) |
| [internal/adapter/out/persistence/mongo_test.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/out/persistence/mongo_test.go) | [internal/adapter/out/persistence/mongo.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/out/persistence/mongo.go) |

Implementation CI 38177342236 passed with mandatory real MongoDB migrations/integration. Its exact head was 60f9ea64, not today's main. Inactive restoration remains a documented model/decoder gap for HU-DOCS-95.

<a id="e-reports"></a>

### E-REPORTS — Sales daily, monthly and top-products reports

**Trace:** FR-008/009; NFR-004/009 → HU-VEN-10 → tests → implementation.

| Test file | Implementation reference |
|---|---|
| [internal/application/usecase/query_reports_test.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/application/usecase/query_reports_test.go) | [internal/application/usecase/query_reports.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/application/usecase/query_reports.go) |
| [internal/adapter/out/persistence/reports_integration_test.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/out/persistence/reports_integration_test.go) | [internal/adapter/out/persistence/reports.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/out/persistence/reports.go) |
| [internal/adapter/in/httpapi/reports_test.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/in/httpapi/reports_test.go) | [internal/adapter/in/httpapi/report_handler.go](https://github.com/code-corhuila/synkro-sales-api/blob/f163eb60552e72f2f5716dd91cb227ae23aa8683/internal/adapter/in/httpapi/report_handler.go) |

Endpoints and aggregation tests exist under HU-VEN-10/#245. The release reports 34 required Mongo scenarios; that is reported local acceptance, not green release CI. Reporting/dashboard screens remain pending.

<a id="e-workflow"></a>

### E-WORKFLOW — Durable saga and recovery

**Trace:** FR-005/007; NFR-002/004/005/006/007/009 → HU-WKF-01, HU-WKF-02, HU-WKF-03 → tests → implementation.

| Test file | Implementation reference |
|---|---|
| [workflow-core/src/test/java/co/edu/corhuila/synkro/workflow/RegisterSaleTest.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-core/src/test/java/co/edu/corhuila/synkro/workflow/RegisterSaleTest.java) | [workflow-core/src/main/java/co/edu/corhuila/synkro/workflow/application/usecase/RegisterSale.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-core/src/main/java/co/edu/corhuila/synkro/workflow/application/usecase/RegisterSale.java) |
| [workflow-adapters/src/test/java/co/edu/corhuila/synkro/workflow/adapter/out/persistence/JdbcSagaStoreTest.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-adapters/src/test/java/co/edu/corhuila/synkro/workflow/adapter/out/persistence/JdbcSagaStoreTest.java) | [workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/out/persistence/JdbcSagaStore.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/out/persistence/JdbcSagaStore.java) |
| [workflow-app/src/test/java/co/edu/corhuila/synkro/workflow/app/WorkflowHttpTest.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-app/src/test/java/co/edu/corhuila/synkro/workflow/app/WorkflowHttpTest.java) | [workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/in/http/SagaController.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/in/http/SagaController.java); [workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/in/http/SecurityConfiguration.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/in/http/SecurityConfiguration.java); [workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/in/http/StoreHealthController.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/in/http/StoreHealthController.java) |
| [tests/process_acceptance.py](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/tests/process_acceptance.py) | [workflow-core/src/main/java/co/edu/corhuila/synkro/workflow/application/usecase/RegisterSale.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-core/src/main/java/co/edu/corhuila/synkro/workflow/application/usecase/RegisterSale.java); [workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/out/persistence/JdbcSagaStore.java](https://github.com/code-corhuila/synkro-workflow/blob/09a193866f05f3714a0bd1ad6d7559a9f00b3611/workflow-adapters/src/main/java/co/edu/corhuila/synkro/workflow/adapter/out/persistence/JdbcSagaStore.java) |

WorkflowHttpTest checks public GET /health, store-health transitions and UTF-8 unauthorized responses using its in-memory SagaStore fixture; it does not exercise JdbcSagaStore. HU-WKF-02's dependency-output guard is a CI source check in .github/workflows/ci.yml, alongside that UTF-8 regression test. process_acceptance.py instead drives real Workflow/PostgreSQL processes through RegisterSale and JdbcSagaStore with controlled HTTP participants. Implementation CI 38301267198 passed. Release PR #13 and later root S4 report actual participant acceptance; historical Customers 501 is superseded. Review/policy/contract follow-ups remain.

<a id="e-auth"></a>

### E-AUTH — Real Auth and role enforcement

LoginUseCaseTest covers credential authentication with fake ports; TokenAuthenticationHttpTest exercises signature validation and role restrictions through the filter/security configuration with in-memory persistence. HealthHttpTest checks GET /health using a switchable database probe. These are distinct from JdbcUserRegistrationAuditIntegrationTest's real PostgreSQL transaction/audit checks and must not be described as a single real-database suite.

**Trace:** FR-010; NFR-002/004/005/006/009 → HU-AUTH-01, HU-AUTH-03, HU-AUTH-07, HU-AUTH-08, HU-AUTH-09, HU-AUTH-11, HU-AUTH-13 → tests → implementation.

| Test file | Implementation reference |
|---|---|
| [auth-core/src/test/java/co/edu/corhuila/synkro/auth/application/usecase/LoginUseCaseTest.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-core/src/test/java/co/edu/corhuila/synkro/auth/application/usecase/LoginUseCaseTest.java) | [auth-core/src/main/java/co/edu/corhuila/synkro/auth/application/usecase/LoginUseCase.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-core/src/main/java/co/edu/corhuila/synkro/auth/application/usecase/LoginUseCase.java) |
| [auth-app/src/test/java/co/edu/corhuila/synkro/auth/app/HealthHttpTest.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-app/src/test/java/co/edu/corhuila/synkro/auth/app/HealthHttpTest.java) | [auth-app/src/main/java/co/edu/corhuila/synkro/auth/app/HealthController.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-app/src/main/java/co/edu/corhuila/synkro/auth/app/HealthController.java) |
| [auth-app/src/test/java/co/edu/corhuila/synkro/auth/app/RegisterAdminOnlyHttpIntegrationTest.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-app/src/test/java/co/edu/corhuila/synkro/auth/app/RegisterAdminOnlyHttpIntegrationTest.java) | [auth-core/src/main/java/co/edu/corhuila/synkro/auth/application/usecase/RegisterUserUseCase.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-core/src/main/java/co/edu/corhuila/synkro/auth/application/usecase/RegisterUserUseCase.java) |
| [auth-adapters/src/test/java/co/edu/corhuila/synkro/auth/adapter/out/persistence/jdbc/JdbcUserRegistrationAuditIntegrationTest.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-adapters/src/test/java/co/edu/corhuila/synkro/auth/adapter/out/persistence/jdbc/JdbcUserRegistrationAuditIntegrationTest.java) | [auth-adapters/src/main/java/co/edu/corhuila/synkro/auth/adapter/out/persistence/jdbc/JdbcUserRegistrationStore.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-adapters/src/main/java/co/edu/corhuila/synkro/auth/adapter/out/persistence/jdbc/JdbcUserRegistrationStore.java) |
| [auth-app/src/test/java/co/edu/corhuila/synkro/auth/app/TokenAuthenticationHttpTest.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-app/src/test/java/co/edu/corhuila/synkro/auth/app/TokenAuthenticationHttpTest.java) | [auth-app/src/main/java/co/edu/corhuila/synkro/auth/app/AccessTokenAuthenticationFilter.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-app/src/main/java/co/edu/corhuila/synkro/auth/app/AccessTokenAuthenticationFilter.java); [auth-adapters/src/main/java/co/edu/corhuila/synkro/auth/adapter/out/crypto/Rs256TokenVerifier.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-adapters/src/main/java/co/edu/corhuila/synkro/auth/adapter/out/crypto/Rs256TokenVerifier.java); [auth-app/src/main/java/co/edu/corhuila/synkro/auth/app/SecurityConfig.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-app/src/main/java/co/edu/corhuila/synkro/auth/app/SecurityConfig.java) |

AUTH-REG-01…04 and AUTH-AUDIT-01 map to these real tests. Rejected/failed security logging is covered by [auth-app/src/test/java/co/edu/corhuila/synkro/auth/app/RegisterRejectionLogHttpIntegrationTest.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-app/src/test/java/co/edu/corhuila/synkro/auth/app/RegisterRejectionLogHttpIntegrationTest.java). Implementation CI 38179239451 and release CI 38474187886 passed; release #37 has ariel5253 approval on 8ed48096. Gateway acceptance is reported separately.

<a id="e-auth-session"></a>

### E-AUTH-SESSION — Refresh, logout and credential boundaries

**Trace:** FR-010; NFR-004/009 → HU-AUTH-10, HU-FE-08 → tests → implementation.

| Test file | Implementation reference |
|---|---|
| [auth-app/src/test/java/co/edu/corhuila/synkro/auth/app/SessionHttpIntegrationTest.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-app/src/test/java/co/edu/corhuila/synkro/auth/app/SessionHttpIntegrationTest.java) | [auth-core/src/main/java/co/edu/corhuila/synkro/auth/application/usecase/RefreshUseCase.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-core/src/main/java/co/edu/corhuila/synkro/auth/application/usecase/RefreshUseCase.java) |
| [auth-app/src/test/java/co/edu/corhuila/synkro/auth/app/LogoutHttpTest.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-app/src/test/java/co/edu/corhuila/synkro/auth/app/LogoutHttpTest.java) | [auth-core/src/main/java/co/edu/corhuila/synkro/auth/application/usecase/LogoutUseCase.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-core/src/main/java/co/edu/corhuila/synkro/auth/application/usecase/LogoutUseCase.java) |
| [auth-adapters/src/test/java/co/edu/corhuila/synkro/auth/adapter/out/persistence/jdbc/RefreshRotationVersusReuseRevocationIntegrationTest.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-adapters/src/test/java/co/edu/corhuila/synkro/auth/adapter/out/persistence/jdbc/RefreshRotationVersusReuseRevocationIntegrationTest.java) | [auth-core/src/main/java/co/edu/corhuila/synkro/auth/application/usecase/RefreshUseCase.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-core/src/main/java/co/edu/corhuila/synkro/auth/application/usecase/RefreshUseCase.java); [auth-adapters/src/main/java/co/edu/corhuila/synkro/auth/adapter/out/persistence/jdbc/JdbcRefreshTokenStore.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-adapters/src/main/java/co/edu/corhuila/synkro/auth/adapter/out/persistence/jdbc/JdbcRefreshTokenStore.java) |

AUTH-REFRESH-01…03 and AUTH-LOGOUT-01/02 have implementation files and real database CI. Refresh invalidation does not establish immediate stateless access-JWT revocation; TD-011 remains a decision boundary.

<a id="e-auth-init"></a>

### E-AUTH-INIT — Initial administrator

**Trace:** FR-010; NFR-004/009 → HU-AUTH-12, HU-AUTH-14 → tests → implementation.

| Test file | Implementation reference |
|---|---|
| [auth-app/src/test/java/co/edu/corhuila/synkro/auth/app/InitialAdminCliIntegrationTest.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-app/src/test/java/co/edu/corhuila/synkro/auth/app/InitialAdminCliIntegrationTest.java) | [auth-app/src/main/java/co/edu/corhuila/synkro/auth/app/InitialAdminCli.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-app/src/main/java/co/edu/corhuila/synkro/auth/app/InitialAdminCli.java) |
| [auth-adapters/src/test/java/co/edu/corhuila/synkro/auth/adapter/out/persistence/jdbc/JdbcInitialAdminStoreIntegrationTest.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-adapters/src/test/java/co/edu/corhuila/synkro/auth/adapter/out/persistence/jdbc/JdbcInitialAdminStoreIntegrationTest.java) | [auth-adapters/src/main/java/co/edu/corhuila/synkro/auth/adapter/out/persistence/jdbc/JdbcInitialAdminStore.java](https://github.com/code-corhuila/synkro-auth-api/blob/e3ea374fbfb428e2583b5a776295e60b0e229696/auth-adapters/src/main/java/co/edu/corhuila/synkro/auth/adapter/out/persistence/jdbc/JdbcInitialAdminStore.java) |

AUTH-INIT-01…03 have delivered tests and packaged CLI CI. The initialization runbook is updated by HU-DOCS-96, not invented here. Initialization is not administrator recovery.

<a id="e-host"></a>

### E-HOST — Host authentication and session

**Trace:** FR-010; NFR-004/005/006 → HU-FE-03, HU-FE-04, HU-FE-05, HU-FE-06, HU-FE-07, HU-FE-08 → tests → implementation.

| Test file | Implementation reference |
|---|---|
| [src/core/http/apiClient.test.ts](https://github.com/code-corhuila/synkro-front/blob/c43d024790cfc6345ddd266c04e6967a8d035702/src/core/http/apiClient.test.ts) | [src/core/http/apiClient.ts](https://github.com/code-corhuila/synkro-front/blob/c43d024790cfc6345ddd266c04e6967a8d035702/src/core/http/apiClient.ts) |
| [src/core/auth/authentication.test.ts](https://github.com/code-corhuila/synkro-front/blob/c43d024790cfc6345ddd266c04e6967a8d035702/src/core/auth/authentication.test.ts) | [src/core/auth/authentication.ts](https://github.com/code-corhuila/synkro-front/blob/c43d024790cfc6345ddd266c04e6967a8d035702/src/core/auth/authentication.ts) |
| [src/core/auth/session.test.ts](https://github.com/code-corhuila/synkro-front/blob/c43d024790cfc6345ddd266c04e6967a8d035702/src/core/auth/session.test.ts) | [src/core/auth/session.ts](https://github.com/code-corhuila/synkro-front/blob/c43d024790cfc6345ddd266c04e6967a8d035702/src/core/auth/session.ts) |
| [src/core/auth/sessionRecovery.test.ts](https://github.com/code-corhuila/synkro-front/blob/c43d024790cfc6345ddd266c04e6967a8d035702/src/core/auth/sessionRecovery.test.ts) | [src/core/auth/authentication.ts](https://github.com/code-corhuila/synkro-front/blob/c43d024790cfc6345ddd266c04e6967a8d035702/src/core/auth/authentication.ts); [src/core/auth/session.ts](https://github.com/code-corhuila/synkro-front/blob/c43d024790cfc6345ddd266c04e6967a8d035702/src/core/auth/session.ts); [src/shell/session.ts](https://github.com/code-corhuila/synkro-front/blob/c43d024790cfc6345ddd266c04e6967a8d035702/src/shell/session.ts) |

FE-AUTH-01/02 and FE-SESSION-01…07 have corresponding Host test families. No per-ID fully real coverage is inferred from unit tests. Current main CI is failure. TD-010 and architecture/session formalization remain assigned to HU-ARQ-28.

<a id="e-sales-portal"></a>

### E-SALES-PORTAL — Registration, detail and recovery UI

**Trace:** FR-005/006/007/010; NFR-005/006 → HU-VEN-11, HU-VEN-12 → tests → implementation.

| Test file | Implementation reference |
|---|---|
| [src/sales/pages/useRegisterSale.test.tsx](https://github.com/code-corhuila/synkro-sales-portal/blob/179dcf5a3bdd2b5fa09d4602cacf61c3a41e761e/src/sales/pages/useRegisterSale.test.tsx) | [src/sales/pages/useRegisterSale.ts](https://github.com/code-corhuila/synkro-sales-portal/blob/179dcf5a3bdd2b5fa09d4602cacf61c3a41e761e/src/sales/pages/useRegisterSale.ts) |
| [src/sales/pages/registrationRecovery.test.tsx](https://github.com/code-corhuila/synkro-sales-portal/blob/179dcf5a3bdd2b5fa09d4602cacf61c3a41e761e/src/sales/pages/registrationRecovery.test.tsx) | [src/sales/pages/useRegisterSale.ts](https://github.com/code-corhuila/synkro-sales-portal/blob/179dcf5a3bdd2b5fa09d4602cacf61c3a41e761e/src/sales/pages/useRegisterSale.ts) |
| [src/sales/pages/SaleDetailPage.test.tsx](https://github.com/code-corhuila/synkro-sales-portal/blob/179dcf5a3bdd2b5fa09d4602cacf61c3a41e761e/src/sales/pages/SaleDetailPage.test.tsx) | [src/sales/pages/SaleDetailPage.tsx](https://github.com/code-corhuila/synkro-sales-portal/blob/179dcf5a3bdd2b5fa09d4602cacf61c3a41e761e/src/sales/pages/SaleDetailPage.tsx) |

The owner reports 242 tests, 96.43% statements and 18/18 browser scenarios with real services, distinguishing controlled faults. Raw local artifacts were not rerun here. Exact release CI failed before steps. Peer review and QA/Pact follow-ups remain.

<a id="e-customers-portal"></a>

### E-CUSTOMERS-PORTAL — Angular portal and Host boundary

**Trace:** FR-001/010; NFR-004/006 → HU-CLI-04 → tests → implementation.

| Test file | Implementation reference |
|---|---|
| [src/app/customers/data/customers-api.service.spec.ts](https://github.com/code-corhuila/synkro-customers-portal/blob/4208839dad3ccfb8eeb4feff12352c8e4cd73695/src/app/customers/data/customers-api.service.spec.ts) | [src/app/customers/data/customers-api.service.ts](https://github.com/code-corhuila/synkro-customers-portal/blob/4208839dad3ccfb8eeb4feff12352c8e4cd73695/src/app/customers/data/customers-api.service.ts) |
| [src/app/customers/pages/customers-page.component.spec.ts](https://github.com/code-corhuila/synkro-customers-portal/blob/4208839dad3ccfb8eeb4feff12352c8e4cd73695/src/app/customers/pages/customers-page.component.spec.ts) | [src/app/customers/pages/customers-page.component.ts](https://github.com/code-corhuila/synkro-customers-portal/blob/4208839dad3ccfb8eeb4feff12352c8e4cd73695/src/app/customers/pages/customers-page.component.ts) |
| [src/bootstrap.spec.ts](https://github.com/code-corhuila/synkro-customers-portal/blob/4208839dad3ccfb8eeb4feff12352c8e4cd73695/src/bootstrap.spec.ts) | [src/bootstrap.ts](https://github.com/code-corhuila/synkro-customers-portal/blob/4208839dad3ccfb8eeb4feff12352c8e4cd73695/src/bootstrap.ts) |

Current main CI success includes coverage, build and compiled boundary checks. Real navigation acceptance is reported separately; R-008 reassessment belongs to HU-DOCS-97.

<a id="e-gateway"></a>

### E-GATEWAY — Public authentication routes and gateway controls

**Trace:** FR-010; NFR-002/004/005 → HU-GTW-01, HU-GTW-02 → tests → implementation.

| Test file | Implementation reference |
|---|---|
| [tests/smoke.sh](https://github.com/code-corhuila/synkro-api-gateway/blob/5e5c3d5bdd67ae8c20eba2eaf2241d43a67adcde/tests/smoke.sh) | [templates/default.conf.template](https://github.com/code-corhuila/synkro-api-gateway/blob/5e5c3d5bdd67ae8c20eba2eaf2241d43a67adcde/templates/default.conf.template) |

CI 38159346276 and release CI 38395897492 passed. Release #10 reports smoke 24/24, controlled contract 77/77 and real Auth 42/42; these are distinct levels.

<a id="e-customers-db"></a>

### E-CUSTOMERS-DB — Customers migrations and permissions

**Trace:** FR-001; NFR-004/007/009 → HU-CLI-02 → tests → implementation.

| Test file | Implementation reference |
|---|---|
| [tests/contract.test.mjs](https://github.com/code-corhuila/synkro-customers-db/blob/4c24733e812adec24658fbce9d8518eda52adda4/tests/contract.test.mjs) | [flyway.toml](https://github.com/code-corhuila/synkro-customers-db/blob/4c24733e812adec24658fbce9d8518eda52adda4/flyway.toml) |
| [tests/integration.test.mjs](https://github.com/code-corhuila/synkro-customers-db/blob/4c24733e812adec24658fbce9d8518eda52adda4/tests/integration.test.mjs) | [deploy/compose.yml](https://github.com/code-corhuila/synkro-customers-db/blob/4c24733e812adec24658fbce9d8518eda52adda4/deploy/compose.yml) |

Implementation CI 38301108306 and release CI 38438397365 passed. CLI-DB-01…14 map to docs/acceptance.md. Reported 6 static / 17 integration groups. Initial SQL functional red was blocked by Docker; later regressions do not retrospectively supply it.

<a id="e-mongo-infra"></a>

### E-MONGO-INFRA — Mongo lifecycle and Sales integration

**Trace:** NFR-004/007/009 → HU-INF-04 → tests → implementation.

| Test file | Implementation reference |
|---|---|
| [tests/configuration.test.mjs](https://github.com/code-corhuila/synkro-infra-mongo/blob/89a1a80bfd28c3c3d5287e3be2e52a7ac57e167f/tests/configuration.test.mjs) | [compose.yml](https://github.com/code-corhuila/synkro-infra-mongo/blob/89a1a80bfd28c3c3d5287e3be2e52a7ac57e167f/compose.yml) |
| [tests/integration.mjs](https://github.com/code-corhuila/synkro-infra-mongo/blob/89a1a80bfd28c3c3d5287e3be2e52a7ac57e167f/tests/integration.mjs) | [compose.yml](https://github.com/code-corhuila/synkro-infra-mongo/blob/89a1a80bfd28c3c3d5287e3be2e52a7ac57e167f/compose.yml) |
| [tests/sales-integration.mjs](https://github.com/code-corhuila/synkro-infra-mongo/blob/89a1a80bfd28c3c3d5287e3be2e52a7ac57e167f/tests/sales-integration.mjs) | [compose.yml](https://github.com/code-corhuila/synkro-infra-mongo/blob/89a1a80bfd28c3c3d5287e3be2e52a7ac57e167f/compose.yml) |

Current main CI success and owner verification exist. Root agreement still conflicts between Sales DB revisions 9cfbde7 and 344d917 despite equal source trees; HU-INF-02 remains open.

<a id="e-worker"></a>

### E-WORKER — Scheduler-only delivery

**Trace:** NFR-005/006; planned FR-004 automation → HU-WRK-01 → tests → implementation.

| Test file | Implementation reference |
|---|---|
| [internal/adapter/in/scheduler/scheduler_test.go](https://github.com/code-corhuila/synkro-worker/blob/8ccef730f849807809c5b11c135bc7cae3ce6342/internal/adapter/in/scheduler/scheduler_test.go) | [internal/adapter/in/scheduler/scheduler.go](https://github.com/code-corhuila/synkro-worker/blob/8ccef730f849807809c5b11c135bc7cae3ce6342/internal/adapter/in/scheduler/scheduler.go) |
| [internal/config/config_test.go](https://github.com/code-corhuila/synkro-worker/blob/8ccef730f849807809c5b11c135bc7cae3ce6342/internal/config/config_test.go) | [internal/config/config.go](https://github.com/code-corhuila/synkro-worker/blob/8ccef730f849807809c5b11c135bc7cae3ce6342/internal/config/config.go) |

Scheduler/configuration exist; the job remains a placeholder and there is no observed v2.0.0 tag. Do not report the low-stock business job as tested or complete.

<a id="e-auth-db"></a>

### E-AUTH-DB — Auth schema and audit migrations

**Trace:** NFR-004/007/009 → HU-AUTH-02, HU-AUTH-04, HU-AUTH-05, HU-AUTH-06, HU-AUTH-12 → tests → implementation.

| Test file | Implementation reference |
|---|---|
| [tests/test_history_contract.py](https://github.com/code-corhuila/synkro-auth-db/blob/6c8270372adca0bee7a6e62a8f14c33a9d8977e5/tests/test_history_contract.py) | [flyway.toml](https://github.com/code-corhuila/synkro-auth-db/blob/6c8270372adca0bee7a6e62a8f14c33a9d8977e5/flyway.toml); [.github/workflows/db-ci.yml](https://github.com/code-corhuila/synkro-auth-db/blob/6c8270372adca0bee7a6e62a8f14c33a9d8977e5/.github/workflows/db-ci.yml) |

History contract and migration rebuild are owned by this repository. Real audit/user constraints are also exercised by Auth API's database CI. Latest main CI is failure; no new reconstruction was executed in this preparation.

<a id="e-products-db"></a>

### E-PRODUCTS-DB — Products schema and stock migrations

**Trace:** FR-002/003/004/007; NFR-007/009 → HU-PRO-04, HU-PRO-05, HU-PRO-06, HU-PRO-07 → tests → implementation.

| Test file | Implementation reference |
|---|---|
| [tests/test_history_contract.py](https://github.com/code-corhuila/synkro-products-db/blob/043e485d16018fc4fb4cd60694c2a521c143ddbd/tests/test_history_contract.py) | [flyway.toml](https://github.com/code-corhuila/synkro-products-db/blob/043e485d16018fc4fb4cd60694c2a521c143ddbd/flyway.toml); [.github/workflows/db-ci.yml](https://github.com/code-corhuila/synkro-products-db/blob/043e485d16018fc4fb4cd60694c2a521c143ddbd/.github/workflows/db-ci.yml) |

History/migrations support the real Products persistence tests, including stock concurrency. Latest main CI is failure; migration success is not inferred from a tag.

<a id="e-sales-db"></a>

### E-SALES-DB — Sales validator and credential-safe runner

**Trace:** FR-005/006; NFR-004/007/009 → HU-VEN-04, HU-VEN-05, HU-VEN-16, HU-VEN-17 → tests → implementation.

| Test file | Implementation reference |
|---|---|
| [deploy/ci/tests/credential-runner.test.js](https://github.com/code-corhuila/synkro-sales-db/blob/344d917264bbb5ad17c40d2e3a33f217a7a94866/deploy/ci/tests/credential-runner.test.js) | [deploy/migrate.sh](https://github.com/code-corhuila/synkro-sales-db/blob/344d917264bbb5ad17c40d2e3a33f217a7a94866/deploy/migrate.sh) |
| [deploy/ci/tests/secret-output.test.js](https://github.com/code-corhuila/synkro-sales-db/blob/344d917264bbb5ad17c40d2e3a33f217a7a94866/deploy/ci/tests/secret-output.test.js) | [deploy/secret-output.js](https://github.com/code-corhuila/synkro-sales-db/blob/344d917264bbb5ad17c40d2e3a33f217a7a94866/deploy/secret-output.js); [deploy/validate-state.js](https://github.com/code-corhuila/synkro-sales-db/blob/344d917264bbb5ad17c40d2e3a33f217a7a94866/deploy/validate-state.js) |
| [deploy/ci/sale/cases.js](https://github.com/code-corhuila/synkro-sales-db/blob/344d917264bbb5ad17c40d2e3a33f217a7a94866/deploy/ci/sale/cases.js) | [01_ddl/00_collections/changelog.yaml](https://github.com/code-corhuila/synkro-sales-db/blob/344d917264bbb5ad17c40d2e3a33f217a7a94866/01_ddl/00_collections/changelog.yaml); [01_ddl/02_indexes/changelog.yaml](https://github.com/code-corhuila/synkro-sales-db/blob/344d917264bbb5ad17c40d2e3a33f217a7a94866/01_ddl/02_indexes/changelog.yaml); [03_dcl/00_roles/changelog.yaml](https://github.com/code-corhuila/synkro-sales-db/blob/344d917264bbb5ad17c40d2e3a33f217a7a94866/03_dcl/00_roles/changelog.yaml) |

Latest main CI success. #230 retains VEN-16 and #237 is VEN-17. Historical handoff filenames and their old #237 alias are resolved by the central ID register rather than globally rewritten.

<a id="e-products-portal"></a>

### E-PRODUCTS-PORTAL — Products portal lifecycle and stock views

**Trace:** FR-002/003/004; NFR-004/006 → HU-PRO-01, HU-PRO-02, HU-PRO-11, HU-PRO-12, HU-PRO-13 → tests → implementation.

| Test file | Implementation reference |
|---|---|
| [src/products/components/ProductRegistrationForm.test.tsx](https://github.com/code-corhuila/synkro-products-portal/blob/b407475b363b9ac7fcbe3512852ba4146a96dadf/src/products/components/ProductRegistrationForm.test.tsx) | [src/products/components/ProductRegistrationForm.tsx](https://github.com/code-corhuila/synkro-products-portal/blob/b407475b363b9ac7fcbe3512852ba4146a96dadf/src/products/components/ProductRegistrationForm.tsx) |
| [src/products/components/CategoriesSection.test.tsx](https://github.com/code-corhuila/synkro-products-portal/blob/b407475b363b9ac7fcbe3512852ba4146a96dadf/src/products/components/CategoriesSection.test.tsx) | [src/products/components/CategoriesSection.tsx](https://github.com/code-corhuila/synkro-products-portal/blob/b407475b363b9ac7fcbe3512852ba4146a96dadf/src/products/components/CategoriesSection.tsx) |
| [src/products/components/StockAdjustmentForm.test.tsx](https://github.com/code-corhuila/synkro-products-portal/blob/b407475b363b9ac7fcbe3512852ba4146a96dadf/src/products/components/StockAdjustmentForm.test.tsx) | [src/products/components/StockAdjustmentForm.tsx](https://github.com/code-corhuila/synkro-products-portal/blob/b407475b363b9ac7fcbe3512852ba4146a96dadf/src/products/components/StockAdjustmentForm.tsx) |

Latest main CI success. Internal root delivery fixes differ in develop; do not attribute those seven changed files to v2.0.0.

<a id="e-auth-portal"></a>

### E-AUTH-PORTAL — Host-owned real login remote

**Trace:** FR-010; NFR-004/006 → HU-FE-08 → tests → implementation.

| Test file | Implementation reference |
|---|---|
| [src/Login.test.tsx](https://github.com/code-corhuila/synkro-auth-portal/blob/15f7334d33822aaace24ec25fa1a1097a43dbad0/src/Login.test.tsx) | [src/Login.tsx](https://github.com/code-corhuila/synkro-auth-portal/blob/15f7334d33822aaace24ec25fa1a1097a43dbad0/src/Login.tsx) |

Latest main CI success. Login is delivered; users/service-token administration UI is not delivered by this login story.

<a id="e-infra"></a>

### E-INFRA — Root composition and common controls

**Trace:** NFR-002/004/006/007/009 → HU-INF-01, HU-INF-02, HU-INF-04 → common controls and the separately scoped root/Mongo evidence; HU-INF-02 remains open. E-MONGO-INFRA provides INF-04's component tests, while the root references below belong to INF-02.

HU-INF-01/#164 delivered the historical common-file baseline in 17 repositories. The current inventory has 18 after Mongo infrastructure was added. Current .github workflows were inspected as source; their actual runs remain separate evidence.

HU-INF-02/#239 is OPEN. Root main/qa have only the original scaffold; use the exact develop/candidate references, never a main link pretending the composition is delivered there. Source entry points:

- [tests/full_composition.py](https://github.com/code-corhuila/synkro-infra-postgres/blob/f49117420038f35aedbb3aba3538b82af9078812/tests/full_composition.py)
- [tests/workflow_acceptance.py](https://github.com/code-corhuila/synkro-infra-postgres/blob/f49117420038f35aedbb3aba3538b82af9078812/tests/workflow_acceptance.py)

[Root PR #13](https://github.com/code-corhuila/synkro-infra-postgres/pull/13) records A6 18/20, later S4 Workflow acceptance and R7/R8 revalidations at their own roots. The Sales DB revision conflict keeps sales-runtime failed. [Root PR #14](https://github.com/code-corhuila/synkro-infra-postgres/pull/14) records six browser cases passed and E2E-02 RUNNING NOT EXECUTED. Do not combine those reports into a fictional all-green full final-head execution.


### Reported coverage versus the preserved thresholds

These measurements belong to their cited historical runs/reports. No coverage suite was executed by HU-DOCS-94.

| Source / revision | Domain lines | Application lines | Infrastructure lines | Global lines / statements | Interpretation |
|---|---|---|---|---|---|
| [Auth implementation PR #9](https://github.com/code-corhuila/synkro-auth-api/pull/9), reported c7ccacb2 code | 100% | Use cases 99.6%; ports 100% | Crypto 92.5%; JDBC 96.7% | 97.0% lines | Reported local real-PostgreSQL run; later exact-head CI passed; layer minima satisfied in the reported measurement |
| [Sales adapter PR #5](https://github.com/code-corhuila/synkro-sales-api/pull/5), 60f9ea64 | 100% | 100% | 85.38% | 90.87% lines | Real MongoDB/race report; original CI also passed |
| [Sales release PR #23](https://github.com/code-corhuila/synkro-sales-api/pull/23), 1bdd32dc | 100% | 95.71% | 88.30% | 89.72% lines | Reported local required-scenario run; remote release jobs did not start |
| [Workflow release PR #13](https://github.com/code-corhuila/synkro-workflow/pull/13), b3ecbe6c | 100% | 98.25% | 89.00% | 91.30% lines | Reported Maven/real-store run; controlled and real participant reports stay separate |
| [Sales Portal verification](https://github.com/code-corhuila/synkro-sales-portal/blob/179dcf5a3bdd2b5fa09d4602cacf61c3a41e761e/docs/verification-hu-ven-12.md) | Not backend scope | Not backend scope | Not backend scope | 96.43% statements | Reported 242-test run exceeds the 70% frontend global floor; it does not prove Host's separate 85% gate |
| Other components | Not remeasured here | Not remeasured here | Not remeasured here | Use exact owner artifact | A green CI label or test-file presence is not a newly measured percentage |

Do not lower thresholds to fit an observed result, compare a statement percentage to a line threshold, or infer unchanged coverage from an unchanged tag alone.

### Execution references checked during reconciliation

| Component / scope | Exact source head | Evidence | Result and attribution |
|---|---|---|---|
| Sales skeleton QA | 3e293c2ae2ae537e15f05a5ca05f4a66e45ec03b | [38479252573](https://github.com/code-corhuila/synkro-sales-api/actions/runs/38479252573) | CI success after the original cancelled jobs; initial JWT shape check did not prove rejection of a well-formed invalid signature |
| Gateway Auth routing | 1ad3b442ebd023c384941cbc9eea22df4b9bf70d | [38159346276](https://github.com/code-corhuila/synkro-api-gateway/actions/runs/38159346276) | CI success, smoke job |
| Auth registration/audit | de3b0faf3b12f73cda7c725b77be45f845914803 | [38179239451](https://github.com/code-corhuila/synkro-auth-api/actions/runs/38179239451) | CI success, database integration and no-skips guard |
| Sales Mongo adapter | 60f9ea64262f84ff07556ab081f445e7e1941474 | [38177342236](https://github.com/code-corhuila/synkro-sales-api/actions/runs/38177342236) | CI success, mandatory real-Mongo migration/integration |
| Workflow owner scope | 0afd8cde592c3efa5b44b63026001b0c262d4b2b | [38301267198](https://github.com/code-corhuila/synkro-workflow/actions/runs/38301267198) | CI success; real Workflow/PostgreSQL process suite with controlled participants |
| Customers DB | 9d6cc0dc85a3b3cd43eb07e355c500ecb7fc2811 | [38301108306](https://github.com/code-corhuila/synkro-customers-db/actions/runs/38301108306) | CI success; reported 6 static / 17 integration groups |
| Auth release | 8ed4809670c63d4608158ab89891ef9b4e04ec41 | [38474187886](https://github.com/code-corhuila/synkro-auth-api/actions/runs/38474187886) | CI success |
| Customers DB release | 4ec33c3d0899536d37c88b81a60741df715d77df | [38438397365](https://github.com/code-corhuila/synkro-customers-db/actions/runs/38438397365) | CI success |
| Sales API release | 1bdd32dce7921fdfebc82cc28fd8864ae3697477 | [39167717426](https://github.com/code-corhuila/synkro-sales-api/actions/runs/39167717426) | Failure before steps; checked annotation reports billing lock |
| Workflow release | b3ecbe6c75f7038b9b8fe11d396bf00a642bb248 | [39172939455](https://github.com/code-corhuila/synkro-workflow/actions/runs/39172939455) | Failure before steps; billing lock; later real acceptance is reported in release PR #13 |
| Sales Portal release | 040e261bd05c61e4175942ce97b9a6d7188440a7 | [39850308582](https://github.com/code-corhuila/synkro-sales-portal/actions/runs/39850308582) | Failure before steps; billing lock; local reports remain local |

### Current E2E and CI boundaries

Real sale/browser evidence exists now; E2E is not uniformly deferred to post-MVP. The low-stock job and unfinished screens remain unimplemented. Component tests against Host fakes and browser acceptance against real services are both useful and must retain different labels.

CI files now exist across implemented code repositories; root infrastructure CI lives on its actual develop/candidate revisions. Do not copy the historical “no pipeline exists” assumption. Required gates are unchanged; the missing portal QA/Pact decision is not silently waived.

This index supplies traceability, not a new SLA, load result, architectural acceptance, operational recovery permission or whole-codebase SOLID certificate.
