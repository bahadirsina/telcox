package com.telcox.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * FR-01/02: Bireysel musteri kaydi ve KYC state flow.
 * ER: db.sql -> CUSTOMER_SERVICE_CUSTOMER
 */
@Entity
@Table(name = "customer_service_customer")
public class Customer {

    @Id
    private UUID id;

    @Column(name = "identity_user_id", unique = true)
    private UUID identityUserId;

    @Column(name = "customer_number", unique = true, nullable = false, length = 50)
    private String customerNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false, length = 20)
    private CustomerType customerType;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "tax_number", length = 50)
    private String taxNumber;

    /** Encrypted TCKN/VKN - sifreleme uygulama katmaninda (converter) yapilmalidir. */
    @Column(name = "national_id", nullable = false, length = 255)
    private String nationalId;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CustomerSegment segment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CustomerStatus status;

    @Column(name = "status_reason", length = 500)
    private String statusReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Soft delete (KVKK/GDPR) - CUST-03 kapsaminda kullanilacak, burada alan olarak aciliyor. */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected Customer() {
    }

    private Customer(UUID identityUserId, String customerNumber, String firstName, String lastName,
                     String nationalId, LocalDate dateOfBirth, CustomerSegment segment) {
        LocalDateTime now = LocalDateTime.now();
        this.id = UUID.randomUUID();
        this.identityUserId = identityUserId;
        this.customerNumber = customerNumber;
        this.customerType = CustomerType.INDIVIDUAL;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nationalId = nationalId;
        this.dateOfBirth = dateOfBirth;
        this.segment = segment == null ? CustomerSegment.MASS : segment;
        this.status = CustomerStatus.PROSPECT;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** FR-01: Bireysel musteri kaydi. Kayit aninda daima PROSPECT durumunda acilir. */
    public static Customer registerIndividual(UUID identityUserId, String customerNumber, String firstName,
                                               String lastName, String nationalId, LocalDate dateOfBirth,
                                               CustomerSegment segment) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("firstName must not be blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("lastName must not be blank");
        }
        if (nationalId == null || nationalId.isBlank()) {
            throw new IllegalArgumentException("nationalId must not be blank");
        }
        return new Customer(identityUserId, customerNumber, firstName, lastName, nationalId, dateOfBirth, segment);
    }

    @PreUpdate
    void refreshUpdatedAt() {
        updatedAt = LocalDateTime.now();
    }

    /** FR-02: KYC onayi. Sadece PROSPECT durumundaki musteriler onaylanabilir. */
    public void approveKyc(String reason) {
        requireStatus(CustomerStatus.PROSPECT, "Only prospect customers can be KYC-approved");
        this.status = CustomerStatus.ACTIVE;
        this.statusReason = reason;
    }

    /** FR-02: KYC reddi. Terminal durumdur, musteri CLOSED olur. */
    public void rejectKyc(String reason) {
        requireStatus(CustomerStatus.PROSPECT, "Only prospect customers can be KYC-rejected");
        this.status = CustomerStatus.CLOSED;
        this.statusReason = reason;
    }

    public void suspend(String reason) {
        requireStatus(CustomerStatus.ACTIVE, "Only active customers can be suspended");
        this.status = CustomerStatus.SUSPENDED;
        this.statusReason = reason;
    }

    public void reactivate(String reason) {
        requireStatus(CustomerStatus.SUSPENDED, "Only suspended customers can be reactivated");
        this.status = CustomerStatus.ACTIVE;
        this.statusReason = reason;
    }

    public void close(String reason) {
        if (status == CustomerStatus.CLOSED) {
            throw new IllegalStateException("Customer is already closed");
        }
        this.status = CustomerStatus.CLOSED;
        this.statusReason = reason;
    }

    /** CUST-03: KVKK/GDPR soft-delete. Hard-delete asla yapilmaz. */
    public void softDelete() {
        if (this.deletedAt != null) {
            throw new IllegalStateException("Customer is already deleted");
        }
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    private void requireStatus(CustomerStatus expected, String message) {
        if (status != expected) {
            throw new IllegalStateException(message);
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getIdentityUserId() {
        return identityUserId;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getTaxNumber() {
        return taxNumber;
    }

    public String getNationalId() {
        return nationalId;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public CustomerSegment getSegment() {
        return segment;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
