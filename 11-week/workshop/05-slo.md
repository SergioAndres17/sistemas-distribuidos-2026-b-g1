# SLIs, SLOs and Error Budget

## 1. Scope

These SLOs cover the sale-registration journey from CP1:
`POST /api/v1/sagas/register-sale`, handled by synkro-workflow,
which calls synkro-customers-api, synkro-products-api and synkro-sales-api.

The queries use the metric names from CP4.
Only the Workflow counter and histogram exist in the CP3 module.
The Products metric used in SLO 3 is still a proposal.
Synkro has no Production environment yet (NFR-002), so these targets
are a proposal for the `develop` and `qa` environments, not a promise to users.

## 2. Traffic estimate

NFR-001 says no real traffic estimate exists, so I built one:

| Assumption | Value | Reason |
|---|---|---|
| SALESPERSON users | 8 | Medium-sized company with a single location (scope.md) |
| Sales per salesperson per day | 25 | About one sale every 30 minutes in a 12-hour day |
| Opening hours | 08:00 to 20:00, every day | Retail store schedule; no sales at night |
| Register-sale requests per day | 200 | 8 x 25 |
| Peak hours (09:00 to 12:00) | 30 requests per hour | 90 requests in the morning |
| Rest of the day (9 hours) | About 12 requests per hour | The other 110 requests |
| Requests in 30 days | 6,000 | 200 x 30 |

Idempotent repeats with the same `Idempotency-Key` are counted as requests too.
They are few, so I did not add them to the estimate.
Low traffic is important: one failed sale is already about 0.02% of a month.

## 3. Good and valid events

The outcome label from CP4 has three values: success, rejected and error.

- An inactive customer or insufficient stock gives `rejected` (HTTP 4xx).
  The system answered correctly, so a rejection is a good event.
- Only `error` (HTTP 5xx or unhandled failure) is a bad event for availability.
- A saga that answers `201` with status `RUNNING` (ADR-007) is counted as success.
  This is a known gap: the salesperson still has to wait for the result.

## 4. SLO 1 - Availability of register-sale

SLI = requests without technical error / all register-sale requests.

```promql
sum(increase(workflow_requests_total{outcome!="error"}[30d]))
/
sum(increase(workflow_requests_total[30d]))
```

| Item | Value |
|---|---|
| Target | 99.5% |
| Window | 30 days, rolling |
| Budget in requests | 0.5% of 6,000 = 30 failed requests |
| Budget in minutes | 0.5% of 21,600 open minutes = 108 minutes of total outage |

The minutes use opening hours only (30 x 12 x 60 = 21,600).
An outage at 23:00 has no requests, so it does not consume the request budget.
Using the full calendar (43,200 minutes) would give 216 minutes, but that number is misleading here.

Why 99.5% and not 99.9%: Synkro runs on Docker Compose with one instance
per service and one PostgreSQL instance. 99.9% would allow only 6 failed sales
per month, and one restart of workflow-db could spend all of it.

## 5. SLO 2 - Latency of register-sale

SLI = non-error requests answered in 1 second or less / all non-error requests.

```promql
sum(increase(workflow_request_duration_seconds_bucket{outcome!="error",le="1.0"}[30d]))
/
sum(increase(workflow_request_duration_seconds_count{outcome!="error"}[30d]))
```

| Item | Value |
|---|---|
| Target | 99.5% of requests in 1 s or less |
| Window | 30 days, rolling |
| Budget in requests | 0.5% of about 6,000 = 30 slow requests |
| Budget in minutes | 108 minutes with every request slow |

Micrometer writes the bucket as `le="1.0"`, not `le="1"`. Check this
format in GET /metrics before using the query.
Errors are excluded here because they already count in SLO 1.

Why 1 second: the salesperson registers the sale while the customer waits
at the counter. Under one second feels immediate. NFR-001 asks for P95 under
300 ms per critical endpoint, but this request calls three services and saves
the saga state after every step (ADR-007), so 300 ms for the whole saga is too strict.
The 1 s bound is also one of the CP4 histogram buckets, so it can be measured exactly.

## 6. SLO 3 - Stock reservation availability

The product promise in overview.md is real-time stock validation.
If `POST /api/v1/stock-reservations` fails, the sale cannot continue,
and a failed reservation after a timeout is where double stock changes can happen.

SLI = reservations without technical error / all reservation requests.

```promql
sum(increase(products_reservations_total{outcome!="error"}[30d]))
/
sum(increase(products_reservations_total[30d]))
```

| Item | Value |
|---|---|
| Target | 99.5% |
| Window | 30 days, rolling |
| Traffic | About 190 per day (about 5% of sales stop at customer validation) = 5,700 per 30 days |
| Budget in requests | 0.5% of 5,700 = 28 failed reservations |
| Budget in minutes | 108 open minutes |

Insufficient stock is `rejected`, not `error`. It is a business answer.

## 7. Error budget policy

The policy applies to each SLO separately and is checked in the daily meeting.

| Budget consumed | What the team does |
|---|---|
| 50% (15 failed sales) | Write it in the daily notes. Every deploy of workflow, customers, products or sales is watched on the dashboard for 30 minutes after it. |
| 75% (about 23 failed sales) | Stop new features on the four services of the journey. Only fixes and reliability HUs. Each deploy needs approval from the Tech Lead. |
| 100% (30 failed sales) | Feature freeze on the journey until the 30-day window is back under budget. A postmortem is mandatory and the first HU of the next sprint is a reliability item. |

Security fixes are always allowed, even with the budget spent.
If the latency budget is the one spent, the reliability work is about performance.

## 8. Operational notes

- Prometheus keeps 15 days by default. A 30-day SLO needs
  `--storage.tsdb.retention.time=35d`.
- `increase(...[30d])` is expensive. Recording rules should compute the ratios.
- If synkro-workflow is down, its metrics disappear and the ratio has no data.
  No data is not 100%; CP7 adds an alert for missing metrics.
