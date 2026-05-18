-- =============================================================================
-- customer-service initial schema
-- Detayli ER (CUSTOMER, ADDRESS, CONTACT, DOCUMENT, NOTE, CONSENT, AUDIT_LOG)
-- ER diagrami: docs/microservice-er-diagrams/customer-service.dbml
-- TODO: ER tablolarini sirayla bu migration'a tasi.
-- =============================================================================

CREATE TABLE IF NOT EXISTS customer_service_outbox_event (
    id              UUID PRIMARY KEY,
    aggregate_id    UUID,
    aggregate_type  VARCHAR(100),
    event_type      VARCHAR(100) NOT NULL,
    payload_json    JSONB NOT NULL,
    status          VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at    TIMESTAMP
);
CREATE INDEX idx_customer_outbox_status_created ON customer_service_outbox_event(status, created_at);

CREATE TABLE IF NOT EXISTS customer_service_processed_event (
    id              UUID PRIMARY KEY,
    event_id        UUID NOT NULL UNIQUE,
    event_type      VARCHAR(100),
    source_service  VARCHAR(100),
    aggregate_id    UUID,
    processed_at    TIMESTAMP,
    status          VARCHAR(50),
    error_message   VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
