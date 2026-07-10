package com.telcox.billing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * BILL-02 / FR-22: Vergi orani tanimi (KDV/OTV vb.), effective-date destekli.
 * ER: db.sql -> BILLING_SERVICE_TAX_RATE
 */
@Entity
@Table(name = "billing_service_tax_rate")
public class TaxRate {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(length = 100)
    private String name;

    @Column(nullable = false, precision = 9, scale = 4)
    private BigDecimal percentage;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    protected TaxRate() {
    }

    public TaxRate(String code, String name, BigDecimal percentage, LocalDate validFrom) {
        this.id = UUID.randomUUID();
        this.code = code;
        this.name = name;
        this.percentage = percentage;
        this.validFrom = validFrom;
    }

    public void closeAt(LocalDate closingDate) {
        this.validTo = closingDate;
    }

    public boolean isEffectiveOn(LocalDate date) {
        boolean afterStart = validFrom == null || !date.isBefore(validFrom);
        boolean beforeEnd = validTo == null || !date.isAfter(validTo);
        return afterStart && beforeEnd;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }
}
