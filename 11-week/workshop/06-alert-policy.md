# Burn-Rate Alert Policy

## 1. What the alerts watch

The rules in `06-alerts.yml` watch SLO 1 (availability, 99.5%) of
`POST /api/v1/sagas/register-sale` in synkro-workflow, using
`workflow_requests_total` from CP3 and CP4. The error budget is 0.005,
which is 30 failed sales in 30 days (6,000 requests, see `05-slo.md`).

I also added a fast-burn rule for SLO 2 (latency) because CP9 showed
that a slow Customers dependency does not create errors at all.

Burn rate = observed error ratio / budget ratio.
A burn rate of 1 spends exactly the whole budget in 30 days (720 hours).

## 2. Fast burn: RegisterSaleAvailabilityFastBurn

| Item | Value |
|---|---|
| Condition | Error ratio over 1 h AND over 5 min above 14.4 x 0.5% = 7.2% |
| Extra guard | At least 2 errors in the last hour |
| for | 2 minutes |
| Labels | severity=page, team=synkro-tech |

Budget used before it fires: 14.4 x 1 h / 720 h = **2% of the budget**.
At our traffic 2% is only 0.6 failed sales, so in practice the guard decides:
in a peak hour (30 requests) it needs 3 errors (10%), and in a quiet hour
(12 requests) 2 errors (16.7%). That is 3 or 2 failed sales, 7% to 10% of the 30-sale budget.
If the burn continues at 14.4, the whole budget is gone in 720 / 14.4 = 50 hours,
about two days, so a person must act now.

The 5-minute window makes the alert stop soon after the problem is fixed.
The 2-error guard exists because one failed sale in a quiet hour already
gives 8.3%, and paging somebody for one sale is noise. promtool checks this
case in `06-alerts.test.yml`.

## 3. Slow burn: RegisterSaleAvailabilitySlowBurn

| Item | Value |
|---|---|
| Condition | Error ratio over 6 h AND over 30 min above 6 x 0.5% = 3% |
| Extra guard | At least 30 requests in the 6 hours |
| for | 5 minutes |
| Labels | severity=ticket, team=synkro-tech |

Budget used before it fires: 6 x 6 h / 720 h = **5% of the budget**, 1.5 failed sales.
From 09:00 to 15:00 we expect about 126 requests, so 3% means about 4 failed sales,
13% of the budget. At burn rate 6 the budget lasts 720 / 6 = 120 hours, five days,
so this is a ticket for working hours, not a page at night.

## 4. Latency and missing-metrics rules

- `RegisterSaleLatencyFastBurn`: same 14.4 rule, but the bad event is a
  non-error request slower than 1 s (bucket `le="1.0"`). It uses a traffic guard
  of 10 requests per hour, because a slow request is usually not alone.
- `RegisterSaleMetricsAbsent`: if synkro-workflow is down, its series disappear,
  the ratios have no data and the burn alerts cannot fire. This rule covers that hole.

## 5. Validation

```shell
promtool check rules 06-alerts.yml
promtool test rules 06-alerts.test.yml
```

Both commands returned SUCCESS with Prometheus 2.54.1 (10 rules found).
The test file also checks the CP9 drill: the latency alert fires at minute 20.

## 6. Cause-based alerts I will not create

| Alert | Why not |
|---|---|
| CPU above 80% in the synkro-workflow container | High CPU during a JVM garbage collection does not mean a failed sale. It would page people while salespeople see nothing, and it does not fire when Customers is slow. |
| HikariCP active connections above 8 of 10 in synkro-customers-api | A busy pool is a cause, not a symptom. A full pool with fast queries is fine. I will show it on the dashboard to diagnose (CP9), but the page comes from the burn-rate alert. |

Old rules in `13-operations/observability.md` such as `HighErrorRate` (5% for 5 min)
would page for a single failed sale at our traffic, so the burn-rate rules replace them
for this journey.
