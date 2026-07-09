package com.telcox.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * CUST-03 / FR-04: KVKK/riza kaydi. ER: db.sql -> CUSTOMER_SERVICE_CONSENT
 */
@Entity
@Table(name = "customer_service_consent")
public class CustomerConsent {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "consent_type", nullable = false, length = 20)
    private ConsentType consentType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ConsentChannel channel;

    @Column(name = "is_granted", nullable = false)
    private boolean isGranted;

    @Column(name = "granted_at")
    private LocalDateTime grantedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    protected CustomerConsent() {
    }

    public CustomerConsent(UUID customerId, ConsentType consentType, ConsentChannel channel, boolean isGranted) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.consentType = consentType;
        this.channel = channel;
        this.isGranted = isGranted;
        if (isGranted) {
            this.grantedAt = LocalDateTime.now();
        }
    }

    public void grant() {
        this.isGranted = true;
        this.grantedAt = LocalDateTime.now();
        this.revokedAt = null;
    }

    public void revoke() {
        this.isGranted = false;
        this.revokedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public ConsentType getConsentType() {
        return consentType;
    }

    public ConsentChannel getChannel() {
        return channel;
    }

    public boolean isGranted() {
        return isGranted;
    }

    public LocalDateTime getGrantedAt() {
        return grantedAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }
}
