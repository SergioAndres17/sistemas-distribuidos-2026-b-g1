# Workflow instrumentation: first checkpoint push

Requires Java 17+ and Maven 3.9+. Run from this module directory.
Spring Boot 3.5.0 matches the project's framework version.
The workshop targets Java 17; the real Workflow project targets Java 21.

```shell
mvn -DskipTests package
java -jar target/synkro-workflow-workshop-0.1.0.jar
```

In another terminal:
```shell
curl.exe -i -X POST http://127.0.0.1:8085/api/v1/sagas/register-sale -H "traceparent: 00-1234567890abcdef1234567890abcdef-1234567890abcdef-01" -H "X-Correlation-Id: workshop-sale-001"
```

Local execution check: HTTP 200. Response body:
`{"message":"Trace filter checked; no sale was created"}`
Observed response traceparent: `00-1234567890abcdef1234567890abcdef-74b6c151aa8b474b-01`.
Observed request.completed duration_ms: 134. Your span ID and duration will differ.
The response and both JSON logs preserved the incoming trace_id and correlation_id.
Opening this URL in a browser sends GET and returns 405; this endpoint requires POST.
These are HTTP smoke checks; the automated downstream test comes in part two.
This module supports traceparent version 00 and emits JSON logs, not exported spans.
It has no authentication, persistence or sale processing and binds only to localhost.
Stop with Ctrl+C. Never commit target/. CP3 is not complete until part two passes.