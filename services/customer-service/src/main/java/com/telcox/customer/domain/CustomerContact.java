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
 * FR-03: Musteri iletisim bilgisi. ER: db.sql -> CUSTOMER_SERVICE_CONTACT
 */
@Entity
@Table(name = "customer_service_contact")
public class CustomerContact {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_type", nullable = false, length = 20)
    private ContactType contactType;

    @Column(name = "contact_value", nullable = false, length = 255)
    private String contactValue;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected CustomerContact() {
    }

    public CustomerContact(UUID customerId, ContactType contactType, String contactValue, boolean isPrimary) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.contactType = contactType;
        this.contactValue = contactValue;
        this.isVerified = false;
        this.isPrimary = isPrimary;
        this.createdAt = LocalDateTime.now();
    }

    public void markVerified() {
        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
    }

    public void markAsPrimary() {
        this.isPrimary = true;
    }

    public void unmarkAsPrimary() {
        this.isPrimary = false;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public ContactType getContactType() {
        return contactType;
    }

    public String getContactValue() {
        return contactValue;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
