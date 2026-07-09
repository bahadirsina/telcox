package com.telcox.productcatalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * CAT-02 / FR-07/08: PLAN tipindeki bir Product'in tarife-spesifik detaylari.
 *
 * VERSIONING NOTU: db.sql seması productId'yi UNIQUE olarak tanimliyor (1-1 iliski),
 * yani "versiyonlama" burada satir coğaltarak degil, ProductPrice'taki gibi
 * validFrom/validTo araligiyla YAPILAMAZ - tek satir var. Bu yuzden versiyonlama
 * burada "yeni Product.code + yeni Plan" (ör. PLAN_4G_V2) seklinde, product
 * seviyesinde ele alinmistir. Ekip/hoca ile teyit edilmesi gereken nokta budur
 * (bkz. PlanVersioningService yorumu).
 * ER: db.sql -> PRODUCT_SERVICE_PLAN
 */
@Entity
@Table(name = "product_service_plan")
public class Plan {

    @Id
    private UUID id;

    @Column(name = "product_id", nullable = false, unique = true)
    private UUID productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false, length = 20)
    private PlanType planType;

    @Column(name = "commitment_months")
    private Integer commitmentMonths;

    @Column(name = "monthly_price", precision = 19, scale = 4)
    private BigDecimal monthlyPrice;

    @Column(length = 3)
    private String currency;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    protected Plan() {
    }

    public Plan(UUID productId, PlanType planType, Integer commitmentMonths, BigDecimal monthlyPrice,
               String currency, LocalDate validFrom) {
        this.id = UUID.randomUUID();
        this.productId = productId;
        this.planType = planType;
        this.commitmentMonths = commitmentMonths;
        this.monthlyPrice = monthlyPrice;
        this.currency = currency;
        this.validFrom = validFrom;
    }

    /** CAT-02: Bu tarife versiyonunu belirli bir tarihte kapatir (yeni versiyona geçiş). */
    public void closeAt(LocalDate closingDate) {
        this.validTo = closingDate;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public PlanType getPlanType() {
        return planType;
    }

    public Integer getCommitmentMonths() {
        return commitmentMonths;
    }

    public BigDecimal getMonthlyPrice() {
        return monthlyPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }
}
