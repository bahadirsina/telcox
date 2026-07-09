package com.telcox.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_delivery")
public class NotificationDelivery {

    @Id
    private UUID id;

    @Column(name = "customer_id")
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(nullable = false, length = 320)
    private String recipient;

    @Column(length = 255)
    private String subject;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "template_code", length = 100)
    private String templateCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryStatus status;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    protected NotificationDelivery() {
    }

    public NotificationDelivery(UUID customerId, NotificationChannel channel, String recipient,
                                String subject, String content, String correlationId,
                                OffsetDateTime createdAt) {
        this(customerId, channel, recipient, subject, content, null, correlationId, createdAt);
    }

    public NotificationDelivery(UUID customerId, NotificationChannel channel, String recipient,
                                String subject, String content, String templateCode, String correlationId,
                                OffsetDateTime createdAt) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.channel = channel;
        this.recipient = recipient;
        this.subject = subject;
        this.content = content;
        this.templateCode = templateCode;
        this.correlationId = correlationId;
        this.status = DeliveryStatus.PENDING;
        this.createdAt = createdAt;
    }

    public void markSent(OffsetDateTime sentAt) {
        this.status = DeliveryStatus.SENT;
        this.sentAt = sentAt;
        this.failureReason = null;
    }

    public void markFailed(String failureReason) {
        this.status = DeliveryStatus.FAILED;
        this.failureReason = failureReason;
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public NotificationChannel getChannel() { return channel; }
    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public String getContent() { return content; }
    public String getTemplateCode() { return templateCode; }
    public DeliveryStatus getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public String getCorrelationId() { return correlationId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getSentAt() { return sentAt; }
}
