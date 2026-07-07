ALTER TABLE support_ticket ADD COLUMN IF NOT EXISTS assigned_at TIMESTAMP;
ALTER TABLE support_ticket ADD COLUMN IF NOT EXISTS sla_due_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_support_ticket_sla_due
    ON support_ticket(status, sla_due_at);
