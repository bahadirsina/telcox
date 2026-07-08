package com.telcox.productcatalog.api;

import com.telcox.productcatalog.domain.FeatureType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PlanFeatureRequest(
        @NotNull FeatureType featureType,
        BigDecimal allowance,
        String unit,
        boolean isUnlimited
) {
}
