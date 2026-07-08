package com.telcox.productcatalog.api;

import com.telcox.productcatalog.domain.Category;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String code,
        String name,
        UUID parentCategoryId
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getCode(), category.getName(), category.getParentCategoryId());
    }
}
