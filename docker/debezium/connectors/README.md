# Debezium connector templates

Kafka Connect runs the Debezium PostgreSQL connector plugin. Connector
definitions are kept here as templates and can be registered through:

```bash
curl -X POST http://localhost:18084/connectors \
  -H "Content-Type: application/json" \
  --data @docker/debezium/connectors/customer-outbox-connector.json
```

Outbox connectors should publish records to the canonical topic format defined
in `architecture/event-backbone.md`: `telcox.<bounded-context>.<event-name>.v<major>`.
