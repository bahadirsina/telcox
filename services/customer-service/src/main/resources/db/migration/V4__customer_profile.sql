-- =============================================================================
-- FR-03: adres, iletisim, belge yonetimi tablolari
-- ER: db.sql -> CUSTOMER_SERVICE_ADDRESS, CUSTOMER_SERVICE_CONTACT, CUSTOMER_SERVICE_DOCUMENT
-- =============================================================================

CREATE TABLE IF NOT EXISTS customer_service_address (
    id           UUID PRIMARY KEY,
    customer_id  UUID NOT NULL,
    address_type VARCHAR(20) NOT NULL,
    country      VARCHAR(100),
    city         VARCHAR(100),
    district     VARCHAR(100),
    street       VARCHAR(255),
    building_no  VARCHAR(50),
    postal_code  VARCHAR(20),
    is_default   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_customer_address_customer_id
        FOREIGN KEY (customer_id) REFERENCES customer_service_customer(id)
);
CREATE INDEX IF NOT EXISTS idx_customer_address_customer_id ON customer_service_address(customer_id);

CREATE TABLE IF NOT EXISTS customer_service_contact (
    id            UUID PRIMARY KEY,
    customer_id   UUID NOT NULL,
    contact_type  VARCHAR(20) NOT NULL,
    contact_value VARCHAR(255) NOT NULL,
    is_verified   BOOLEAN NOT NULL DEFAULT FALSE,
    is_primary    BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at   TIMESTAMP,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_customer_contact_customer_id
        FOREIGN KEY (customer_id) REFERENCES customer_service_customer(id)
);
CREATE INDEX IF NOT EXISTS idx_customer_contact_customer_id ON customer_service_contact(customer_id);

CREATE TABLE IF NOT EXISTS customer_service_document (
    id                  UUID PRIMARY KEY,
    customer_id         UUID NOT NULL,
    document_type       VARCHAR(20) NOT NULL,
    document_number     VARCHAR(100),
    file_url            VARCHAR(500),
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    uploaded_by_user_id UUID,
    uploaded_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified_at         TIMESTAMP,
    CONSTRAINT fk_customer_document_customer_id
        FOREIGN KEY (customer_id) REFERENCES customer_service_customer(id)
);
CREATE INDEX IF NOT EXISTS idx_customer_document_customer_id ON customer_service_document(customer_id);
