package com.telcox.billing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * BILL-01/02/03 / FR-21/22/23: Fatura. Bill-run job (BILL-01) DRAFT olarak acar;
 * InvoiceLineService (BILL-02) kalemleri ekleyip toplamlari hesaplar;
 * InvoicePdfService (BILL-03) PDF uretip ISSUED durumuna gecirir.
 * ER: db.sql -> BILLING_SERVICE_INVOICE
 */
@Entity
@Table(name = "billing_service_invoice")
public class Invoice {

    @Id
    private UUID id;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @Column(name = "billing_account_id", nullable = false)
    private UUID billingAccountId;

    @Column(name = "billing_cycle_id")
    private UUID billingCycleId;

    /** logical ref -> subscription-service.SUBSCRIPTION.id */
    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxAmount;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Invoice() {
    }

    /** BILL-01: Bill-run job tarafindan DRAFT olarak acilir; toplamlar henuz 0'dir. */
    public Invoice(String invoiceNumber, UUID billingAccountId, UUID billingCycleId, UUID subscriptionId,
                   LocalDate periodStart, LocalDate periodEnd, String currency) {
        this.id = UUID.randomUUID();
        this.invoiceNumber = invoiceNumber;
        this.billingAccountId = billingAccountId;
        this.billingCycleId = billingCycleId;
        this.subscriptionId = subscriptionId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.subtotal = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
        this.currency = currency;
        this.status = InvoiceStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
    }

    /** BILL-02: Kalemler eklendikten sonra toplamlari gunceller. */
    public void applyTotals(BigDecimal subtotal, BigDecimal taxAmount) {
        requireStatus(InvoiceStatus.DRAFT, "Only draft invoices can have their totals recalculated");
        this.subtotal = subtotal;
        this.taxAmount = taxAmount;
        this.totalAmount = subtotal.add(taxAmount);
    }

    /** BILL-03: PDF uretilip musteriye bildirim gonderilirken cagrilir. */
    public void issue(LocalDate dueDate) {
        requireStatus(InvoiceStatus.DRAFT, "Only draft invoices can be issued");
        this.status = InvoiceStatus.ISSUED;
        this.issuedAt = LocalDateTime.now();
        this.dueDate = dueDate;
    }

    public void markPaid() {
        if (status != InvoiceStatus.ISSUED && status != InvoiceStatus.PARTIALLY_PAID && status != InvoiceStatus.OVERDUE) {
            throw new IllegalStateException("Invoice cannot be marked paid from status " + status);
        }
        this.status = InvoiceStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public void cancel() {
        if (status == InvoiceStatus.PAID) {
            throw new IllegalStateException("A paid invoice cannot be cancelled");
        }
        this.status = InvoiceStatus.CANCELLED;
    }

    private void requireStatus(InvoiceStatus expected, String message) {
        if (status != expected) {
            throw new IllegalStateException(message);
        }
    }

    public UUID getId() {
        return id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public UUID getBillingAccountId() {
        return billingAccountId;
    }

    public UUID getBillingCycleId() {
        return billingCycleId;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
