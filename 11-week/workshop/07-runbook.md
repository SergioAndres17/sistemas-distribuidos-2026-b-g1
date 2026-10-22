# Runbook: Register-Sale Fast Burn

Alert: `RegisterSaleAvailabilityFastBurn` (severity=page, team=synkro-tech).
The latency variant `RegisterSaleLatencyFastBurn` uses the same steps, see the end.
Severity: P1 in `13-operations/incident-management.md`, because salespeople
cannot register sales, the core transaction of SynkroTech.

## 1. Symptoms

- Salespeople see an error when they confirm a sale in the sales portal.
- `POST /api/v1/sagas/register-sale` answers 5xx through synkro-api-gateway.
- More sagas end in `COMPENSATED` or `FAILED` (ADR-007), visible with `GET /api/v1/sagas/{id}`.
- The error ratio in the last hour is above 7.2%.

## 2. Confirm (first 5 minutes)

1. Acknowledge the alert and write in `#incidents`: "Investigating register-sale errors - <name>".
2. Check that the alert is real and still burning (Prometheus or Grafana on port 3000):

```promql
synkro:register_sale_error_ratio:rate5m
synkro:register_sale_error_ratio:rate1h
sum by (outcome) (increase(workflow_requests_total[15m]))
```

3. Take one failed request. The error body has `traceId` (cross-cutting.md);
   in CP2 logs it is `trace_id`. Get every line of that trace from the journey:

```shell
docker compose logs --since 30m synkro-workflow synkro-customers-api synkro-products-api synkro-sales-api \
  | grep '"trace_id":"<trace_id>"'
```

4. Read the failing step from the events: `customer.lookup.*`,
   stock reservation or sale registration. The `service` and `version`
   fields say which participant failed and which build it runs.
5. Ask the key question: what changed in the last 2 hours?
   (deploy, env file in `synkro-infra-postgres/env/`, traffic, migration).

## 3. Mitigations, from lower to higher risk

Announce each change in the war room and wait for the Incident Commander.

| # | Mitigation | When | How | Risk |
|---|---|---|---|---|
| 1 | Roll back the last deploy of the failing service | A deploy or env change happened before the alert | Set the previous image tag in that service's `deploy/compose.yml` or revert the env file, then `docker compose up -d <service>` | Low. Sagas are persisted and resumed, and steps are idempotent (ADR-007), so a restart does not create duplicate sales or reservations. |
| 2 | Scale the failing participant | No recent change, the participant is overloaded | `docker compose up -d --scale synkro-customers-api=2` (no `ports:` in -api compose files, so replicas do not collide) | Medium. Not tested in our environment; the JVM DNS cache in synkro-workflow may keep sending to one replica. All replicas share the same PostgreSQL pool limits. |
| 3 | Stop new sales at the gateway | Errors continue and leave stock reserved or sagas in `FAILED` | Return 503 with `Retry-After` for `/api/v1/sagas/` in synkro-api-gateway, reload NGINX | High. Nobody can sell. Only with the IC's approval and a message to the Product Owner. |

Not available today, so not in this runbook:

- **Feature flag:** Synkro has no feature-flag system.
- **Circuit breaker:** synkro-workflow only has bounded retries (5 s timeout,
  3 attempts, `HTTP_TIMEOUT` and `HTTP_ATTEMPTS`). A circuit breaker is a backlog item.

Never do during the incident: delete `RUNNING` sagas from `workflow_schema`
or change stock by hand. A stuck saga is resumed or compensated by the workflow.

## 4. Escalation

| Role | Who | When |
|---|---|---|
| On-call engineer | Person on rotation: Jordan Ramirez Gallego, Sergio Andrés Ordóñez Díaz, Fredman Santiago Plazas Artunduaga | Receives the page, acts in the first 15 minutes |
| Tech Lead and Incident Commander | Angel Gustavo Solano Trujillo | P1 always; if not mitigated in 15 minutes; before mitigation 3 |
| Owner of the failing participant | The developer of that -api repository | When the trace points to one participant |
| Comms Lead | The on-call engineer not handling the fix | Updates the Product Owner (course instructor) every 30 minutes |

## 5. Verify recovery

1. `synkro:register_sale_error_ratio:rate5m` is back near 0 for 15 minutes.
2. `docker compose ps` shows every journey service healthy and `GET /health` answers 200.
3. A test sale through `POST /api/v1/sagas/register-sale` ends `COMPLETED`.
4. Sagas left in `RUNNING` during the incident ended `COMPLETED` or `COMPENSATED`;
   any `FAILED` saga is listed for a person to decide.
5. The alert resolves by itself. Write the resolution message and schedule the
   postmortem within 2 working days.

## Latency variant

For `RegisterSaleLatencyFastBurn` there are no 5xx: requests are slow but answer.

```promql
synkro:register_sale_slow_ratio:rate5m
histogram_quantile(0.95, sum by (le) (rate(workflow_request_duration_seconds_bucket[5m])))
histogram_quantile(0.95, sum by (le) (rate(customers_lookup_duration_seconds_bucket[5m])))
```

Find a slow trace in the logs (`duration_ms` above 1000 in `request.completed`),
and compare the `duration_ms` of each step to see which participant is slow.
Then follow the same mitigations, starting with the rollback.
