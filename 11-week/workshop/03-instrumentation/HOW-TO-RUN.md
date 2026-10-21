# Workflow instrumentation workshop

Requires Java 17+ and Maven 3.9+. Run from this module directory.
Spring Boot 3.5.0 matches the project's framework version.
The workshop targets Java 17; the real Workflow project targets Java 21.

```shell
mvn verify
java -jar target/synkro-workflow-workshop-0.1.0.jar
```

In another terminal:
```shell
curl.exe -i -X POST http://127.0.0.1:8085/api/v1/sagas/register-sale -H "Content-Type: application/json" -d "{\"customerId\":\"22222222-2222-4222-8222-222222222222\"}" -H "traceparent: 00-1234567890abcdef1234567890abcdef-1234567890abcdef-01" -H "X-Correlation-Id: workshop-sale-001"
```

Historical first-push execution: HTTP 200. Response body:
`{"message":"Trace filter checked; no sale was created"}`
Observed response traceparent: `00-1234567890abcdef1234567890abcdef-74b6c151aa8b474b-01`.
Observed request.completed duration_ms: 134. Your span ID and duration will differ.
The response and both JSON logs preserved the incoming trace_id and correlation_id.
Opening this URL in a browser sends GET and returns 405; this endpoint requires POST.
The first-push results above are historical; current tests include the downstream call.
This module supports traceparent version 00 and emits JSON logs, not exported spans.
It has no authentication, persistence or sale processing and binds only to localhost.
Stop with Ctrl+C. Never commit target/. Run the current tests before publishing.
## Second-push verification
`mvn verify` starts Workflow and a controlled Customers HTTP server on temporary ports.
Tests check trace propagation, different spans, HTTP metrics, and a downstream 503 mapped to 502 with an error count.
Observed local preparation output:
```text
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```
The first run found /metrics unavailable in tests; AutoConfigureObservability fixed test export.
GET /metrics exposes workflow_requests_total and workflow_request_duration_seconds_bucket.
The outcome label is success, rejected or error; no customer or trace IDs are labels.
Histogram bounds are 0.1, 0.3, 1 and 3 seconds, plus +Inf. Scrapes are excluded.
Logs also include optional parent_span_id to connect child and parent operations.
Manual POST needs a local Customers stub at port 8091; without it the response is 503.
This module only checks downstream HTTP 200; it does not validate customers or execute the saga.
