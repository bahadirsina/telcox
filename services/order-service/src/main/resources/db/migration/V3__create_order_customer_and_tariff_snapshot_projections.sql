-- PRJ-02: order-service local snapshots. Source-of-truth remains the source service.

CREATE TABLE IF NOT EXISTS order_customer_snapshot_projection (
    customer_id         UUID PRIMARY KEY,
    customer_number     VARCHAR(64) NOT NULL,
    full_name           VARCHAR(200) NOT NULL,
    customer_status     VARCHAR(32) NOT NULL,
    segment             VARCHAR(64),
    last_event_id       UUID NOT NULL,
    source_updated_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    projected_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_order_customer_snapshot_number
    ON order_customer_snapshot_projection(customer_number);
CREATE INDEX IF NOT EXISTS idx_order_customer_snapshot_status
    ON order_customer_snapshot_projection(customer_status);

CREATE TABLE IF NOT EXISTS order_product_catalog_tariff_projection (
    tariff_id           UUID PRIMARY KEY,
    tariff_code         VARCHAR(64) NOT NULL,
    tariff_name         VARCHAR(200) NOT NULL,
    amount              NUMERIC(19, 4) NOT NULL,
    currency            VARCHAR(3) NOT NULL,
    tariff_status       VARCHAR(32) NOT NULL,
    last_event_id       UUID NOT NULL,
    source_updated_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    projected_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_order_tariff_amount_non_negative CHECK (amount >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_order_product_catalog_tariff_code
    ON order_product_catalog_tariff_projection(tariff_code);
CREATE INDEX IF NOT EXISTS idx_order_product_catalog_tariff_status
    ON order_product_catalog_tariff_projection(tariff_status);
