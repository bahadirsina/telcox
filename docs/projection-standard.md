# TelcoX Projection Naming and Migration Standard

This document defines PRJ-01 for every service that owns a local projection.

## Table naming

Projection tables use:

```text
<consumer>_<source>_<purpose>_projection
```

Rules:

- Names are lowercase `snake_case`.
- `consumer` is the service that owns the table.
- `source` is the bounded context that owns the source-of-truth data.
- `purpose` describes the minimal local read model.
- A projection table never has a foreign key to another service database.

Examples:

```text
notification_customer_preference_projection
payment_billing_invoice_projection
order_product_catalog_projection
```

## Migration naming

Projection migrations use the next available Flyway version in the owning
service:

```text
V<next>__create_<table_name>.sql
```

Example:

```text
services/notification-service/src/main/resources/db/migration/
  V3__create_notification_customer_preference_projection.sql
```

Rules:

- Never edit an applied migration; add a new migration.
- Use `CREATE TABLE IF NOT EXISTS` and deterministic index names.
- Name primary keys after the projected aggregate, such as `customer_id`.
- Include `last_event_id`, `source_updated_at`, and `projected_at`.
- Keep only fields required by the consuming service.
- Use UTC timestamps and explicit `NOT NULL` constraints.
- Consumer processing remains idempotent through the service
  `*_processed_event` table.

## Index naming

Projection indexes use:

```text
idx_<table_name_without_projection_suffix>_<columns>
```

Example:

```text
idx_notification_customer_preference_channel
```

## Update ordering

Consumers must ignore an event when its source timestamp is not newer than the
stored `source_updated_at`. This prevents delayed Kafka delivery from replacing
newer projection state.
