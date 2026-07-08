package com.telcox.productcatalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * CAT-02 / FR-08: Urun kategorisi (self-referencing hiyerarsi: parentCategoryId).
 * ER: db.sql -> PRODUCT_SERVICE_CATEGORY
 */
@Entity
@Table(name = "product_service_category")
public class Category {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "parent_category_id")
    private UUID parentCategoryId;

    protected Category() {
    }

    public Category(String code, String name, UUID parentCategoryId) {
        this.id = UUID.randomUUID();
        this.code = code;
        this.name = name;
        this.parentCategoryId = parentCategoryId;
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

    public UUID getParentCategoryId() {
        return parentCategoryId;
    }
}
