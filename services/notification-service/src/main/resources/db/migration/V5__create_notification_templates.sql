CREATE TABLE IF NOT EXISTS notification_template (
    id                UUID PRIMARY KEY,
    template_code     VARCHAR(100) NOT NULL,
    channel           VARCHAR(20) NOT NULL,
    locale            VARCHAR(10) NOT NULL,
    subject_template  VARCHAR(255),
    content_template  TEXT NOT NULL,
    active            BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_notification_template_channel CHECK (channel IN ('EMAIL', 'SMS')),
    CONSTRAINT uq_notification_template_code_channel_locale UNIQUE (template_code, channel, locale)
);

CREATE INDEX IF NOT EXISTS idx_notification_template_code
    ON notification_template(template_code);

ALTER TABLE notification_delivery
    ADD COLUMN IF NOT EXISTS template_code VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_notification_delivery_template_code
    ON notification_delivery(template_code);

INSERT INTO notification_template (
    id, template_code, channel, locale, subject_template, content_template, active, created_at, updated_at
)
VALUES
    ('11111111-1111-4111-8111-111111111101', 'ticket.opened', 'EMAIL', 'tr-TR',
     'Talebiniz alindi: {{ticketNumber}}',
     'Merhaba {{customerName}}, {{ticketNumber}} numarali destek talebiniz {{category}} kategorisinde olusturuldu.',
     TRUE, NOW(), NOW()),
    ('11111111-1111-4111-8111-111111111102', 'ticket.opened', 'SMS', 'tr-TR',
     NULL,
     'TelcoX: {{ticketNumber}} numarali destek talebiniz olusturuldu.',
     TRUE, NOW(), NOW()),
    ('11111111-1111-4111-8111-111111111103', 'payment.received', 'EMAIL', 'tr-TR',
     'Odemeniz alindi',
     'Merhaba {{customerName}}, {{amount}} TL tutarindaki odemeniz basariyla alindi.',
     TRUE, NOW(), NOW())
ON CONFLICT (template_code, channel, locale) DO NOTHING;
