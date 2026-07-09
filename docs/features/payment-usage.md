# Payment and Usage Features

## PAY-01 / FR-25

`payment-service` exposes a mock credit card payment endpoint. The mock gateway validates card
numbers with Luhn, approves normal test cards, declines `4000000000000002`, and declines payments
above the mock limit.

```text
POST /api/v1/payments
Idempotency-Key: {client-generated-key}
```

Approved payments are persisted as `CAPTURED`; declined payments are persisted as `FAILED` with a
failure reason.

## PAY-02 / FR-26

Payment processing is idempotent with a Redis lease and a unique database `idempotency_key`.
Completed duplicate requests return the stored payment with `idempotentReplay=true`; concurrent
requests with the same key return `409 Conflict` until the first request finishes.

## PAY-03 / FR-27

The payment retry worker polls durable `READY` schedules in bounded batches. Retry attempts run at
24, 72 and 168 hours from the first failure. Successful retries complete the schedule; the third
failed attempt exhausts it. `PaymentRetryPaymentExecutor` retries failed mock-card payments through
the same gateway boundary and marks successful retries as `CAPTURED`.

## USG-01 / FR-17

`usage-service` consumes `telcox.cdr.cdr-recorded.v1` with consumer group `usage-service`. Event ID and
external CDR ID uniqueness make processing idempotent. Usage record persistence, processed-event
registration and quota consumption share one database transaction.

## USG-02 / FR-18

Current quota state is available from:

```text
GET /api/v1/usage/subscriptions/{subscriptionId}/quotas?at={ISO-8601 instant}
```

The response reports total, used, remaining and usage percentage for every active quota period.

## USG-03 / FR-19

Quota consumption emits threshold events through the usage outbox when usage crosses 80, 90 and
100 percent. Each threshold is emitted once per quota period and tracked with
`last_threshold_percent`.

```text
telcox.usage.quota-threshold-reached.v1
```

## USG-04 / FR-20

When a CDR pushes a quota above its allowance, usage emits an overage aggregation event with the
current total overage and the delta introduced by the latest CDR. These events share the same CDR
transaction as quota consumption and processed-event registration.

```text
telcox.usage.quota-overage-aggregated.v1
```
