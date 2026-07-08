package com.telcox.productcatalog.api;

import com.telcox.productcatalog.domain.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductRequest(
        @NotBlank String code,
        @NotBlank String name,
        String description,
        @NotNull ProductType productType
) {
}
