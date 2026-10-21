# Golden Signals and Metric Catalog
## 1. Scope
This catalog follows the sale-registration journey from CP1.
It covers synkro-api-gateway, synkro-workflow,
synkro-customers-api, synkro-products-api and synkro-sales-api.
The frontend starts the journey; these tables cover its server services.
CP3 implements only the Workflow request counter and duration histogram.
The other metrics below are proposals, including all saturation gauges.
They are not evidence of instrumentation already deployed in Synkro.
Queries assume Prometheus scrapes one workshop environment.
Prometheus and dashboards were not installed or tested in CP3.
## 2. Metric rules
Counters use the _total suffix. Durations use seconds.
Histograms expose _bucket, _sum and _count series.
Each service uses a fixed metric prefix, never a prefix built from user input.
The outcome label has three possible values:
- success: a response below HTTP 400.
- rejected: an HTTP 4xx response.
- error: an HTTP 5xx response or an unhandled technical failure.
A rejected request is not automatically an infrastructure failure.
However, 4xx responses still need investigation when unexpected.
The workshop's success label describes HTTP handling, not a completed sale.
The traffic and error queries use the same counter.
The error query selects only outcome="error".
Each request is counted once when it finishes.
## 3. synkro-api-gateway
Scope: incoming requests to /api/v1/sagas/register-sale.
All metrics in this table are proposed.
| Signal | Metric | Type | Labels | PromQL |
|---|---|---|---|---|
| Latency | gateway_request_duration_seconds | Histogram | outcome; generated le | `histogram_quantile(0.95, sum by (le) (rate(gateway_request_duration_seconds_bucket[5m])))` |
| Traffic | gateway_requests_total | Counter | outcome | `sum(rate(gateway_requests_total[5m]))` |
| Errors | gateway_requests_total | Counter | outcome | `sum(rate(gateway_requests_total{outcome="error"}[5m]))` |
| Saturation | gateway_requests_in_flight | Gauge | None | `sum(gateway_requests_in_flight)` |
## 4. synkro-workflow
Scope: requests to /api/v1/sagas/register-sale.
Latency, traffic and errors use the metrics implemented in CP3.
Saturation is proposed. GET /metrics is excluded from these measurements.
| Signal | Metric | Type | Labels | PromQL |
|---|---|---|---|---|
| Latency | workflow_request_duration_seconds | Histogram | outcome; generated le | `histogram_quantile(0.95, sum by (le) (rate(workflow_request_duration_seconds_bucket[5m])))` |
| Traffic | workflow_requests_total | Counter | outcome | `sum(rate(workflow_requests_total[5m]))` |
| Errors | workflow_requests_total | Counter | outcome | `sum(rate(workflow_requests_total{outcome="error"}[5m]))` |
| Saturation | workflow_requests_in_flight | Gauge | None | `sum(workflow_requests_in_flight)` |
## 5. synkro-customers-api
Scope: GET /api/v1/customers/{id}, used to validate the customer.
All metrics in this table are proposed.
| Signal | Metric | Type | Labels | PromQL |
|---|---|---|---|---|
| Latency | customers_lookup_duration_seconds | Histogram | outcome; generated le | `histogram_quantile(0.95, sum by (le) (rate(customers_lookup_duration_seconds_bucket[5m])))` |
| Traffic | customers_lookups_total | Counter | outcome | `sum(rate(customers_lookups_total[5m]))` |
| Errors | customers_lookups_total | Counter | outcome | `sum(rate(customers_lookups_total{outcome="error"}[5m]))` |
| Saturation | customers_lookups_in_flight | Gauge | None | `sum(customers_lookups_in_flight)` |
## 6. synkro-products-api
Scope: POST /api/v1/stock-reservations.
All metrics in this table are proposed.
| Signal | Metric | Type | Labels | PromQL |
|---|---|---|---|---|
| Latency | products_reservation_duration_seconds | Histogram | outcome; generated le | `histogram_quantile(0.95, sum by (le) (rate(products_reservation_duration_seconds_bucket[5m])))` |
| Traffic | products_reservations_total | Counter | outcome | `sum(rate(products_reservations_total[5m]))` |
| Errors | products_reservations_total | Counter | outcome | `sum(rate(products_reservations_total{outcome="error"}[5m]))` |
| Saturation | products_reservations_in_flight | Gauge | None | `sum(products_reservations_in_flight)` |
## 7. synkro-sales-api
Scope: POST /api/v1/sales.
All metrics in this table are proposed.
| Signal | Metric | Type | Labels | PromQL |
|---|---|---|---|---|
| Latency | sales_registration_duration_seconds | Histogram | outcome; generated le | `histogram_quantile(0.95, sum by (le) (rate(sales_registration_duration_seconds_bucket[5m])))` |
| Traffic | sales_registrations_total | Counter | outcome | `sum(rate(sales_registrations_total[5m]))` |
| Errors | sales_registrations_total | Counter | outcome | `sum(rate(sales_registrations_total{outcome="error"}[5m]))` |
| Saturation | sales_registrations_in_flight | Gauge | None | `sum(sales_registrations_in_flight)` |
## 8. Saturation interpretation
An in-flight gauge increases when an operation starts and decreases
when it finishes, including failures. It represents concurrent work.
Growing concurrency together with growing latency can indicate pressure.
This is a saturation proxy, not a percentage of capacity.
Load tests are still needed to determine safe concurrency.
Database pool usage and waiting requests would provide additional evidence.
No capacity limit or saturation threshold has been measured in this workshop.
## 9. Label cardinality
| Label | Maximum values in this design | Rule |
|---|---|---|
| outcome | 3 | success, rejected, error |
| le | 5 | Four finite histogram bounds and +Inf |
| job | 5 for this catalog | One configured scrape job per server service |
| instance | 1 per job in the proposed local setup | Add replicas deliberately |
job and instance are target labels added by Prometheus, not by CP3 code.
The gauges have no application labels.
The fixed scope of each metric avoids route and method labels.
Per service and instance: the counter has at most 3 series.
The histogram has 3 outcomes × (5 buckets + sum + count) = 21 series.
The gauge adds 1 series, giving 25 catalog series per service.
Five services would give 125 series with one instance each.
This excludes runtime metrics and extra series such as Micrometer's _max.
Never use customer_id, sale_id, saga_id, trace_id, email or raw URLs as labels.
Use approved identifiers in restricted logs instead.
If route labels are introduced later, use /customers/{id}, never the actual ID.
## 10. Latency buckets
CP3 uses 0.1, 0.3, 1 and 3 seconds, plus +Inf.
The proposed metrics use the same bounds for consistency.
100 ms identifies fast responses; 300 ms matches the project's
documented critical-endpoint target. One second shows a noticeable wait.
Three seconds identifies long waits near the workshop's downstream timeout.
+Inf includes slower requests instead of losing them.
These boundaries are design choices, not proof that the target is met.
Workflow coordinates several calls in the real system, so its complete
journey must be measured before accepting an end-to-end latency objective.
## 11. Reading the queries
rate(...[5m]) estimates events per second over the last five minutes.
sum combines instances and outcomes where appropriate.
histogram_quantile estimates P95 from the histogram buckets.
P95 means approximately 95% of measured requests were no slower than this value.
Low traffic makes short-window estimates unstable.
An absent error series is not proof of zero errors; first verify scraping.
No traffic or missing observations must not be reported as a healthy latency.
The tables show proposed dashboard queries, not executed dashboard results.
## 12. References
- [Prometheus naming](https://prometheus.io/docs/practices/naming/)
- [Prometheus histograms](https://prometheus.io/docs/practices/histograms/)
- [Synkro service catalog](https://github.com/code-corhuila/synkro-docs/blob/main/09-microservices/service-catalog.md)