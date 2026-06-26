package com.telcox.notification.projection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_customer_preference_projection")
public class CustomerPreferenceProjection {

    @Id
    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    @Column(length = 320)
    private String email;

    @Column(name = "phone_number", length = 32)
    private String phoneNumber;

    @Column(name = "preferred_channel", nullable = false, length = 20)
    private String preferredChannel;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled;

    @Column(name = "sms_enabled", nullable = false)
    private boolean smsEnabled;

    @Column(name = "push_enabled", nullable = false)
    private boolean pushEnabled;

    @Column(name = "marketing_consent", nullable = false)
    private boolean marketingConsent;

    @Column(name = "transactional_consent", nullable = false)
    private boolean transactionalConsent;

    @Column(name = "last_event_id", nullable = false)
    private UUID lastEventId;

    @Column(name = "source_updated_at", nullable = false)
    private OffsetDateTime sourceUpdatedAt;

    @Column(name = "projected_at", nullable = false)
    private OffsetDateTime projectedAt;

    protected CustomerPreferenceProjection() {
    }

    public CustomerPreferenceProjection(CustomerPreferenceChanged event, OffsetDateTime projectedAt) {
        this.customerId = event.customerId();
        apply(event, projectedAt);
    }

    public void apply(CustomerPreferenceChanged event, OffsetDateTime projectedAt) {
        this.email = event.email();
        this.phoneNumber = event.phoneNumber();
        this.preferredChannel = event.preferredChannel();
        this.emailEnabled = event.emailEnabled();
        this.smsEnabled = event.smsEnabled();
        this.pushEnabled = event.pushEnabled();
        this.marketingConsent = event.marketingConsent();
        this.transactionalConsent = event.transactionalConsent();
        this.lastEventId = event.eventId();
        this.sourceUpdatedAt = event.sourceUpdatedAt();
        this.projectedAt = projectedAt;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getPreferredChannel() {
        return preferredChannel;
    }

    public UUID getLastEventId() {
        return lastEventId;
    }

    public OffsetDateTime getSourceUpdatedAt() {
        return sourceUpdatedAt;
    }
}
