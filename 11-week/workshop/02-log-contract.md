# Structured Log Contract

## 1. Purpose

Synkro services will write one JSON object per log line.
The same field names will help us follow a sale request across
synkro-api-gateway, synkro-workflow and synkro-customers-api.

This is the proposed workshop contract.
The examples below are fictional, not logs from an executed test.

## 2. Required fields

| Field | Type | Required | Example | Purpose |
|---|---|---|---|---|
| timestamp | string | Yes | 2026-10-20T23:10:00.000Z | Event time in UTC |
| level | string | Yes | INFO | Event severity |
| service | string | Yes | synkro-workflow | Service producing the log |
| version | string | Yes | workshop-cp2 | Application version or build |
| environment | string | Yes | workshop | Execution environment |
| trace_id | string | Yes | 1234567890abcdef1234567890abcdef | Connect spans in one trace |
| span_id | string | Yes | 2222222222222222 | Identify the current operation |
| correlation_id | string | Yes | workshop-sale-001 | Preserve application correlation |
| event | string | Yes | customer.lookup.timeout | Stable event name |
| message | string | Yes | Customer lookup timed out | Short explanation |
| duration_ms | number | Yes | 1000 | Elapsed milliseconds for this event's operation |

trace_id contains 32 hexadecimal characters.
span_id contains 16 hexadecimal characters.
Different spans have different span IDs.
Multiple logs from the same span may repeat its span ID.

duration_ms is nonnegative. Start events use zero.
Completion events record elapsed time for their operation.
Durations from nested operations must not be added together.

## 3. Domain fields

These fields are optional because not every event has all the data.

| Field | Type | Example | Purpose |
|---|---|---|---|
| saga_id | string | 11111111-1111-4111-8111-111111111111 | Identify the sale-registration saga |
| customer_id | string | 22222222-2222-4222-8222-222222222222 | Identify the customer being validated |
| product_count | integer | 2 | Number of different products requested |
| requested_quantity | integer | 3 | Total units requested across all lines |

Internal identifiers are restricted operational data.
They must not be used as metric labels or exposed in public logs.
A sale_id is not available when registration fails before Sales.

## 4. Data protection

| Data | Logging rule |
|---|---|
| Authorization header, JWT and refresh tokens | Omit completely; use [REDACTED] if a diagnostic field must exist |
| Passwords, private keys and database connection secrets | Omit completely |
| Customer name, identity document, email, phone and address | Omit; use the internal customer_id only when needed |
| Full request and response bodies | Do not log; select approved fields |
| Payment credentials or card details | Never log; they are not required for this journey |
| Raw exception text or downstream response bodies | Replace with a controlled error code and safe message |

Messages must not copy user input.
Serialize logs with a JSON library instead of joining strings.
Limit log access and retention according to the team's security policy.

## 5. Example of a failed request

All seven lines belong to the same trace.
The first customer lookup times out. A bounded retry receives
the result that the customer is inactive, so Workflow rejects
the sale before calling Products or Sales.

For this example only, the lookup timeout is 1000 ms.
This is an illustrative value, not a claim about deployed settings.
The IDs and timestamps are synthetic.

```json
{"timestamp":"2026-10-20T23:10:00.000Z","level":"INFO","service":"synkro-api-gateway","version":"workshop-cp2","environment":"workshop","trace_id":"1234567890abcdef1234567890abcdef","span_id":"1111111111111111","correlation_id":"workshop-sale-001","event":"sale.request.received","message":"Sale request received","duration_ms":0,"product_count":2,"requested_quantity":3}
{"timestamp":"2026-10-20T23:10:00.005Z","level":"INFO","service":"synkro-workflow","version":"workshop-cp2","environment":"workshop","trace_id":"1234567890abcdef1234567890abcdef","span_id":"2222222222222222","correlation_id":"workshop-sale-001","event":"customer.lookup.started","message":"Customer lookup started","duration_ms":0,"customer_id":"22222222-2222-4222-8222-222222222222"}
{"timestamp":"2026-10-20T23:10:01.005Z","level":"WARN","service":"synkro-workflow","version":"workshop-cp2","environment":"workshop","trace_id":"1234567890abcdef1234567890abcdef","span_id":"2222222222222222","correlation_id":"workshop-sale-001","event":"customer.lookup.timeout","message":"Customer lookup timed out","duration_ms":1000,"customer_id":"22222222-2222-4222-8222-222222222222"}
{"timestamp":"2026-10-20T23:10:01.105Z","level":"INFO","service":"synkro-workflow","version":"workshop-cp2","environment":"workshop","trace_id":"1234567890abcdef1234567890abcdef","span_id":"3333333333333333","correlation_id":"workshop-sale-001","event":"customer.lookup.retry","message":"Bounded customer lookup retry started","duration_ms":0,"customer_id":"22222222-2222-4222-8222-222222222222"}
{"timestamp":"2026-10-20T23:10:01.120Z","level":"INFO","service":"synkro-customers-api","version":"workshop-cp2","environment":"workshop","trace_id":"1234567890abcdef1234567890abcdef","span_id":"4444444444444444","correlation_id":"workshop-sale-001","event":"customer.validation.rejected","message":"Customer is inactive","duration_ms":10,"customer_id":"22222222-2222-4222-8222-222222222222"}
{"timestamp":"2026-10-20T23:10:01.122Z","level":"INFO","service":"synkro-workflow","version":"workshop-cp2","environment":"workshop","trace_id":"1234567890abcdef1234567890abcdef","span_id":"5555555555555555","correlation_id":"workshop-sale-001","event":"sale.registration.rejected","message":"Customer validation rejected the sale; stock and sale steps were not started","duration_ms":1120,"saga_id":"11111111-1111-4111-8111-111111111111"}
{"timestamp":"2026-10-20T23:10:01.125Z","level":"INFO","service":"synkro-api-gateway","version":"workshop-cp2","environment":"workshop","trace_id":"1234567890abcdef1234567890abcdef","span_id":"1111111111111111","correlation_id":"workshop-sale-001","event":"sale.request.completed","message":"Business rejection returned to caller","duration_ms":1125}
```

The two failure events are customer.lookup.timeout and
customer.validation.rejected.
A business rejection is an expected rule outcome, not necessarily
an infrastructure error. Log severity reflects that difference.

## 6. Log levels

| Level | When to use it | Synkro example |
|---|---|---|
| ERROR | A technical failure prevents the operation from completing | Customers remains unavailable after the allowed lookup attempts |
| WARN | A technical problem occurs but handling can continue | A customer lookup times out and one allowed retry remains |
| INFO | A normal lifecycle event or expected business outcome occurs | Sale request received or inactive customer rejected |
| DEBUG | Extra diagnostic detail is needed during development | Workflow records which step it is about to execute |

DEBUG does not permit logging secrets or full payloads.
ERROR must not be used for every expected business rejection.

## 7. Review checklist

- All seven examples use the same trace_id and correlation_id.
- Timestamps advance in chronological order.
- Each line is a complete JSON object.
- Every line contains the required fields.
- The timeout happens before the retry.
- The inactive customer prevents stock reservation and sale creation.
- No personal contact details, credentials or tokens are included.