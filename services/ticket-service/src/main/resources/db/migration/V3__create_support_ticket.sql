CREATE TABLE IF NOT EXISTS support_ticket (
    id                  UUID PRIMARY KEY,
    ticket_number       VARCHAR(40) NOT NULL UNIQUE,
    customer_id         UUID NOT NULL,
    category            VARCHAR(80) NOT NULL,
    priority            VARCHAR(20) NOT NULL,
    status              VARCHAR(30) NOT NULL,
    subject             VARCHAR(160) NOT NULL,
    description         TEXT NOT NULL,
    assigned_team       VARCHAR(80),
    assigned_agent_id   UUID,
    correlation_id      VARCHAR(100) NOT NULL,
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL,
    CONSTRAINT chk_support_ticket_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_support_ticket_status CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'))
);

CREATE INDEX IF NOT EXISTS idx_support_ticket_customer_created
    ON support_ticket(customer_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_support_ticket_status_priority
    ON support_ticket(status, priority);
