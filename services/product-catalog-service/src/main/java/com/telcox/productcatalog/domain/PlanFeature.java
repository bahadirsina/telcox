package com.telcox.productcatalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * CAT-02 / FR-07: Bir tarifenin (Plan) icerdigi kotalar (dakika, SMS, veri vb.).
 * ER: db.sql -> PRODUCT_SERVICE_PLAN_FEATURE
 */
@Entity
@Table(name = "product_service_plan_feature")
public class PlanFeature {

    @Id
    private UUID id;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Enumerated(EnumType.STRING)
    @Column(name = "feature_type", nullable = false, length = 20)
    private FeatureType featureType;

    @Column(precision = 19, scale = 4)
    private BigDecimal allowance;

    @Column(length = 20)
    private String unit;

    @Column(name = "is_unlimited", nullable = false)
    private boolean isUnlimited;

    protected PlanFeature() {
    }

    public PlanFeature(UUID planId, FeatureType featureType, BigDecimal allowance, String unit, boolean isUnlimited) {
        this.id = UUID.randomUUID();
        this.planId = planId;
        this.featureType = featureType;
        this.allowance = allowance;
        this.unit = unit;
        this.isUnlimited = isUnlimited;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPlanId() {
        return planId;
    }

    public FeatureType getFeatureType() {
        return featureType;
    }

    public BigDecimal getAllowance() {
        return allowance;
    }

    public String getUnit() {
        return unit;
    }

    public boolean isUnlimited() {
        return isUnlimited;
    }
}
