# Incident Drill and Blameless Postmortem

## 1. Drill scenario

The hop marked in CP1 is synkro-workflow -> synkro-customers-api,
`GET /api/v1/customers/{id}` (step `validate-customer`).
In this drill, Customers answers in 3 seconds for 25% of requests for 40 minutes,
from 10:00 to 10:40 (UTC-5), during the morning peak of 30 sales per hour (`05-slo.md`).

This is a simulated incident. Times, IDs and the root cause are invented for practice.

## 2. Trace of one slow request

trace_id `7d3f0a9c1b2e4d5f8a6b9c0d1e2f3a4b`, same span tree as CP1:

- synkro-front: submit sale [CLIENT, root] 3,312 ms
  - synkro-api-gateway: receive request [SERVER] 3,305 ms
    - synkro-api-gateway: call Workflow [CLIENT] 3,301 ms
      - synkro-workflow: register sale [SERVER] 3,296 ms
        - synkro-workflow: call Customers [CLIENT] 3,021 ms  <- slow
          - synkro-customers-api: get customer [SERVER] 3,012 ms
        - synkro-workflow: call Products [CLIENT] 118 ms
          - synkro-products-api: reserve stock [SERVER] 109 ms
        - synkro-workflow: call Sales [CLIENT] 96 ms
          - synkro-sales-api: register sale [SERVER] 88 ms

The other 61 ms are the saga state writes in `workflow_schema` after each step.
The request does not fail: 3 s is below the 5 s `HTTP_TIMEOUT`, so there is no retry and no error.

## 3. Four log lines of that trace (CP2 format)

```json
{"timestamp":"2026-10-27T15:12:04.210Z","level":"INFO","service":"synkro-workflow","version":"1.2.0","environment":"qa","trace_id":"7d3f0a9c1b2e4d5f8a6b9c0d1e2f3a4b","span_id":"a1b2c3d4e5f60718","correlation_id":"qa-sale-4471","event":"customer.lookup.started","message":"Customer lookup started","duration_ms":0,"customer_id":"5b0e2c1a-7d44-4c3e-9a10-2f6d8e1b3c55","product_count":2}
{"timestamp":"2026-10-27T15:12:07.226Z","level":"INFO","service":"synkro-customers-api","version":"1.4.0","environment":"qa","trace_id":"7d3f0a9c1b2e4d5f8a6b9c0d1e2f3a4b","span_id":"0f1e2d3c4b5a6978","correlation_id":"qa-sale-4471","event":"customer.query.completed","message":"Customer found and active","duration_ms":3012,"customer_id":"5b0e2c1a-7d44-4c3e-9a10-2f6d8e1b3c55"}
{"timestamp":"2026-10-27T15:12:07.231Z","level":"WARN","service":"synkro-workflow","version":"1.2.0","environment":"qa","trace_id":"7d3f0a9c1b2e4d5f8a6b9c0d1e2f3a4b","span_id":"a1b2c3d4e5f60718","correlation_id":"qa-sale-4471","event":"customer.lookup.slow","message":"Customer lookup took more than 1 s","duration_ms":3021,"customer_id":"5b0e2c1a-7d44-4c3e-9a10-2f6d8e1b3c55"}
{"timestamp":"2026-10-27T15:12:07.475Z","level":"INFO","service":"synkro-workflow","version":"1.2.0","environment":"qa","trace_id":"7d3f0a9c1b2e4d5f8a6b9c0d1e2f3a4b","span_id":"9e8d7c6b5a4f3e2d","correlation_id":"qa-sale-4471","event":"sale.registration.completed","message":"Saga completed","duration_ms":3296,"saga_id":"c3a9f1d2-6b7e-4f80-91a2-4d5e6f708192","requested_quantity":3}
```

The `version` field of the Customers line (1.4.0) is the first clue.

## 4. What the SLIs show

| SLI | Before | Minute 20 | Minute 40 |
|---|---|---|---|
| Availability, 5 min | 100% | 100% | 100% |
| Availability, 1 h | 100% | 100% | 100% |
| Latency <= 1 s, 5 min | 100% | about 75% | about 75% |
| Latency <= 1 s, 1 h | 100% | 91.7% (8.3% slow) | 83.3% (16.7% slow) |
| Stock reservation availability | 100% | 100% | 100% |

Availability does not move, so `RegisterSaleAvailabilityFastBurn` never fires.
Latency burn rate is 25% / 0.5% = 50 in the short window.
The 1 h window passes 7.2% at minute 17.3 (25% x 17.3 / 60), plus `for: 2m`:
**`RegisterSaleLatencyFastBurn` fires first, at minute 20 (10:20)**.
`06-alerts.test.yml` checks this with promtool: silent at minute 19, firing at minute 20.

## 5. Runbook step that stops the damage

Runbook section 3, **mitigation 1: roll back the last deploy** of synkro-customers-api.
The trace and the `version` field point to the build deployed at 09:52.
Restarting is safe because sagas are persisted and steps are idempotent (ADR-007).

---

# Postmortem INC-W11-01: slow customer validation in register-sale

Blameless: the goal is to fix the system, not to find a person to blame.

| Key data | Value |
|---|---|
| Start / end | 10:00 / 10:40 (UTC-5), 40 minutes |
| Severity | P2: sales were slow, not blocked |
| Affected | 20 register-sale requests, 5 of them slower than 3 s |

## Timeline

| Time | Event | Action |
|---|---|---|
| 09:52 | synkro-customers-api 1.4.0 deployed with a changed env file | Normal deploy, no alert expected |
| 10:00 | Morning peak starts; 25% of lookups take 3 s | No alert: no errors |
| 10:20 | `RegisterSaleLatencyFastBurn` fires | Page to the on-call engineer |
| 10:22 | Alert acknowledged | Message in `#incidents` |
| 10:26 | Slow trace found; Customers span 3,012 ms, version 1.4.0 | Tech Lead joins as IC |
| 10:29 | IC approves runbook mitigation 1 | Rollback to 1.3.2 started |
| 10:38 | 1.3.2 running and healthy | Test sale ends `COMPLETED` |
| 10:40 | Last slow request | Latency back under 1 s |
| 10:46 | Alert resolved (5 min window clean) | Resolution message to the Product Owner |

## Impact

- Requests: 20 in 40 minutes, 5 slow. Latency budget is 30 slow requests, so **5 = 16.7% of the monthly budget**.
- Minutes: 40 min x 25% = **10 bad minutes of 108 = 9.3%**. The request number is higher
  because the incident happened at peak, with 1.8 times the average traffic.
- Availability budget and stock reservation budget: 0 used. No sale was lost or duplicated.

## Root cause (5 whys)

1. Why were sales slow? The Customers lookup took 3 s for one request in four.
2. Why? Lookups waited about 3 s for a database connection in synkro-customers-api.
3. Why? The pool had 2 connections instead of the decided 10 (cross-cutting.md section 8).
4. Why? The env file used for 1.4.0 had `SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=2`, copied from a local test.
5. Why was it not caught? Env files are not reviewed like code, and no check runs after a deploy.

## Contributing factors

- The 5 s connection wait and the 5 s `HTTP_TIMEOUT` are above the 1 s latency SLO,
  so slowness never became an error and availability alerts stayed quiet.
- Pool metrics are not exported, so the dashboard could not show waiting connections.
- With 2.5 requests per 5 minutes the short alert window is unstable at low traffic.
- The drill needed 20 minutes to page because the 1 h window must fill first.

## Action items as user stories

**HU-OBS-01. As the on-call engineer, I want HikariCP pool metrics from synkro-customers-api on the dashboard, so that I can see waiting connections during an incident.**
- Given the service is running, when I call `GET /metrics`, then `hikaricp_connections_active` and `hikaricp_connections_pending` are present.
- Given 20 concurrent lookups with a pool of 2, when I look at Grafana, then the pending panel shows a value above 0.

**HU-OBS-02. As the Tech Lead, I want a post-deploy smoke test for each journey service, so that a slow build is found before the morning peak.**
- Given a deploy of synkro-customers-api, when the pipeline finishes, then a k6 script sends 50 lookups and fails if P95 is above 300 ms.
- Given the env file sets a pool size below 10, when the test runs, then the deploy is marked as failed.

**HU-OBS-03. As a salesperson, I want the workflow to give up on a slow Customers lookup quickly, so that I am not left waiting at the counter.**
- Given Customers answers in 3 s once and fast on the next call, when the workflow validates the customer, then the first attempt times out at 1 s, the second attempt succeeds and the saga ends `COMPLETED`.
- Given Customers always answers in 3 s, when `mvn verify` runs with a delayed stub, then the request ends in under 2.5 s, the saga ends `COMPENSATED` with failed step `validate-customer`, and `workflow_requests_total{outcome="error"}` increases by 1.
