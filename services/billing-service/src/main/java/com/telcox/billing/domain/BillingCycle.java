package com.telcox.billing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

/**
 * BILL-01 / FR-21: Bir fatura hesabinin donemsel (ayilk/ceyreklik) fatura donemi.
 * Bill-run job, her donem icin bir BillingCycle acar (OPEN), islem tamamlaninca
 * INVOICED yapar. ER: db.sql -> BILLING_SERVICE_BILLING_CYCLE
 */
@Entity
@Table(name = "billing_service_billing_cycle")
public class BillingCycle {

    @Id
    private UUID id;

    @Column(name = "billing_account_id", nullable = false)
    private UUID billingAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "cycle_type", nullable = false, length = 20)
    private CycleType cycleType;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CycleStatus status;

    protected BillingCycle() {
    }

    public BillingCycle(UUID billingAccountId, CycleType cycleType, LocalDate periodStart, LocalDate periodEnd) {
        this.id = UUID.randomUUID();
        this.billingAccountId = billingAccountId;
        this.cycleType = cycleType;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.status = CycleStatus.OPEN;
    }

    public void close() {
        requireStatus(CycleStatus.OPEN, "Only open cycles can be closed");
        this.status = CycleStatus.CLOSED;
    }

    /** BILL-01: Bu donem icin fatura(lar) basariyla olusturulunca cagrilir. */
    public void markInvoiced() {
        if (status != CycleStatus.OPEN && status != CycleStatus.CLOSED) {
            throw new IllegalStateException("Cycle must be open or closed before it can be marked as invoiced");
        }
        this.status = CycleStatus.INVOICED;
    }

    private void requireStatus(CycleStatus expected, String message) {
        if (status != expected) {
            throw new IllegalStateException(message);
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getBillingAccountId() {
        return billingAccountId;
    }

    public CycleType getCycleType() {
        return cycleType;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public CycleStatus getStatus() {
        return status;
    }
}
