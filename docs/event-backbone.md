# TelcoX Event Backbone Standard

This document covers EVT-01, EVT-02, EVT-07 and EVT-08.

## EVT-01: Event envelope

Every Kafka domain event must be wrapped in `EventEnvelope<T>` from
`telco-common`.

Required envelope fields:

| Field | Type | Rule |
|---|---|---|
| `eventId` | UUID | Globally unique idempotency key. Must be provided by callers or created through `EventEnvelope.withGeneratedId(...)`. |
| `type` | string | Business event type. Mirrors the outbox `event_type` column. |
| `aggregateId` | UUID | Aggregate instance id. Mirrors the outbox `aggregate_id` column. |
| `correlationId` | string | Request or saga correlation id. |
| `schemaVersion` | integer | Major schema version, starting at `1`. |

Recommended fields:

| Field | Type | Rule |
|---|---|---|
| `sourceService` | string | Producing service name. |
| `aggregateType` | string | Aggregate category such as `CUSTOMER` or `ORDER`. Mirrors the outbox `aggregate_type` column. |
| `occurredAt` | timestamp | UTC event creation time. Mirrors the outbox `created_at` column. |
| `payload` | object | Versioned business payload. Mirrors the outbox `payload_json` column. |
| `metadata` | object | String key/value technical metadata. |

## EVT-02: Topic naming

PR #4 Debezium connectors are the source of truth for routed outbox topics.
Each connector uses Debezium `EventRouter` with:

```text
transforms.outbox.route.by.field=aggregate_type
transforms.outbox.route.topic.replacement=telcox.events.${routedByValue}
```

Canonical routed topic format:

```text
telcox.events.<aggregate_type>
```

Examples that match the connector output:

```text
telcox.events.CUSTOMER
telcox.events.ORDER
telcox.events.SUBSCRIPTION
telcox.events.INVOICE
telcox.events.PAYMENT
```

Connector source prefixes remain service-scoped, for example
`telcox.customer`, `telcox.order`, and `telcox.billing`; routed business event
topics use `telcox.events.<aggregate_type>`.

## EVT-07: Retry and DLQ strategy

Consumer retry topics:

```text
<source-topic>.retry.0
<source-topic>.retry.1
<source-topic>.dlq
```

Default policy:

| Step | Target topic | Delay intent |
|---|---|---|
| First failure | `<topic>.retry.0` | Short retry, transient errors. |
| Second failure | `<topic>.retry.1` | Longer retry, downstream instability. |
| Final failure | `<topic>.dlq` | Manual inspection and replay. |

Rules:

- `eventId` must stay unchanged across retries and DLQ.
- `correlationId` must stay unchanged across retries and DLQ.
- Retry metadata should include `retryAttempt`, `lastErrorCode`, and
  `lastErrorMessage`.
- DLQ retention is longer than retry retention.

## EVT-08: Schema versioning and compatibility

Compatibility rules:

- Additive optional fields are backward compatible.
- Removing fields, renaming fields, changing field types, or changing required
  semantics requires a new major schema version.
- Consumers must ignore unknown fields.
- Producers must keep publishing the previous major version until all consumers
  migrate.
- Payload defaults must be documented in the event producer module.

Schema version example:

```json
{
  "eventId": "18fd563d-8c42-4718-a2df-8b87edce866e",
  "type": "customer-created",
  "aggregateId": "280e7ec9-643f-4307-b05c-13c47d937edc",
  "aggregateType": "CUSTOMER",
  "correlationId": "corr-20260622-001",
  "schemaVersion": 1
}
```
