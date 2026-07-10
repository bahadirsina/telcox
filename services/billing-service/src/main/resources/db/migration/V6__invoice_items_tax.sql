-- =============================================================================
-- BILL-02 / FR-22: invoice_item ve tax_rate tablolari
-- ER: db.sql -> BILLING_SERVICE_INVOICE_ITEM, BILLING_SERVICE_TAX_RATE
-- =============================================================================

CREATE TABLE IF NOT EXISTS billing_service_invoice_item (
    id           UUID PRIMARY KEY,
    invoice_id   UUID NOT NULL,
    product_code VARCHAR(100),
    description  VARCHAR(255),
    quantity     NUMERIC(19, 4) NOT NULL,
    unit_price   NUMERIC(19, 4) NOT NULL,
    tax_rate     NUMERIC(9, 4) NOT NULL,
    total_price  NUMERIC(19, 4) NOT NULL,
    CONSTRAINT fk_invoice_item_invoice_id
        FOREIGN KEY (invoice_id) REFERENCES billing_service_invoice(id)
);
CREATE INDEX IF NOT EXISTS idx_invoice_item_invoice_id ON billing_service_invoice_item(invoice_id);

CREATE TABLE IF NOT EXISTS billing_service_tax_rate (
    id         UUID PRIMARY KEY,
    code       VARCHAR(50) NOT NULL UNIQUE,
    name       VARCHAR(100),
    percentage NUMERIC(9, 4) NOT NULL,
    valid_from DATE,
    valid_to   DATE
);

-- FR-22: varsayilan KDV orani - InvoiceLineService.DEFAULT_TAX_RATE_PERCENTAGE ile tutarli
INSERT INTO billing_service_tax_rate (id, code, name, percentage, valid_from)
VALUES (gen_random_uuid(), 'KDV_20', 'KDV %20', 20.0000, CURRENT_DATE)
ON CONFLICT (code) DO NOTHING;
