package com.telcox.productcatalog.api;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CategoryRequest(
        @NotBlank String code,
        @NotBlank String name,
        UUID parentCategoryId
) {
}
