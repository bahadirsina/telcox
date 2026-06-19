# TelcoX Event Backbone Standard

This document covers EVT-01, EVT-02, EVT-07 and EVT-08.

## EVT-01: Event envelope

Every Kafka domain event must be wrapped in `EventEnvelope<T>` from
`telco-common`.

Required envelope fields:

| Field | Type | Rule |
|---|---|---|
| `eventId` | UUID | Globally unique idempotency key. |
| `type` | string | Business event type in kebab-case. |
| `aggregateId` | UUID | Aggregate instance id. |
| `correlationId` | string | Request or saga correlation id. |
| `schemaVersion` | integer | Major schema version, starting at `1`. |

Recommended fields:

| Field | Type | Rule |
|---|---|---|
| `sourceService` | string | Producing service name. |
| `aggregateType` | string | Aggregate category such as `CUSTOMER` or `ORDER`. |
| `occurredAt` | timestamp | UTC event creation time. |
| `payload` | object | Versioned business payload. |
| `metadata` | object | String key/value technical metadata. |

## EVT-02: Topic naming

Canonical domain event topic format:

```text
telcox.<bounded-context>.<event-name>.v<major>
```

Examples:

```text
telcox.customer.customer-created.v1
telcox.order.order-created.v1
telcox.billing.invoice-issued.v1
```

Rules:

- Use lowercase kebab-case for bounded context and event name.
- The topic major version must match `schemaVersion`.
- CDC raw topics produced by Debezium may use `telcox.cdc.<service>.*`, but
  routed business topics must use the canonical format.

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
  semantics requires a new major version and a new topic suffix.
- Consumers must ignore unknown fields.
- Producers must keep publishing the previous major version until all consumers
  migrate.
- Payload defaults must be documented in the event producer module.

Version example:

```text
telcox.customer.customer-created.v1
telcox.customer.customer-created.v2
```
