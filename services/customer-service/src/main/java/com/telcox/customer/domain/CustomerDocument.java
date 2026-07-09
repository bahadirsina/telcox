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
 * FR-03: Musteri belgesi (KYC dokumanlari dahil). ER: db.sql -> CUSTOMER_SERVICE_DOCUMENT
 */
@Entity
@Table(name = "customer_service_document")
public class CustomerDocument {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 20)
    private DocumentType documentType;

    @Column(name = "document_number", length = 100)
    private String documentNumber;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private VerificationStatus verificationStatus;

    /** logical ref -> identity-service.USER.id */
    @Column(name = "uploaded_by_user_id")
    private UUID uploadedByUserId;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    protected CustomerDocument() {
    }

    public CustomerDocument(UUID customerId, DocumentType documentType, String documentNumber, String fileUrl,
                            UUID uploadedByUserId) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.fileUrl = fileUrl;
        this.verificationStatus = VerificationStatus.PENDING;
        this.uploadedByUserId = uploadedByUserId;
        this.uploadedAt = LocalDateTime.now();
    }

    public void verify() {
        requireStatus(VerificationStatus.PENDING, "Only pending documents can be verified");
        this.verificationStatus = VerificationStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
    }

    public void reject() {
        requireStatus(VerificationStatus.PENDING, "Only pending documents can be rejected");
        this.verificationStatus = VerificationStatus.REJECTED;
        this.verifiedAt = LocalDateTime.now();
    }

    private void requireStatus(VerificationStatus expected, String message) {
        if (verificationStatus != expected) {
            throw new IllegalStateException(message);
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public UUID getUploadedByUserId() {
        return uploadedByUserId;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }
}
