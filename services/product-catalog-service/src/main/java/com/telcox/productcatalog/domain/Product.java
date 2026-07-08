package com.telcox.productcatalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * CAT-01 / FR-05: Tum urun tipleri (PLAN, ADDON, DEVICE, SERVICE/VAS) icin ortak entity.
 * ER: db.sql -> PRODUCT_SERVICE_PRODUCT
 */
@Entity
@Table(name = "product_service_product")
public class Product {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 20)
    private ProductType productType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Product() {
    }

    private Product(String code, String name, String description, ProductType productType) {
        LocalDateTime now = LocalDateTime.now();
        this.id = UUID.randomUUID();
        this.code = code;
        this.name = name;
        this.description = description;
        this.productType = productType;
        this.status = ProductStatus.DRAFT;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** FR-05: Yeni urun tanimi (tarife, addon, cihaz veya VAS/SERVICE). Daima DRAFT olarak acilir. */
    public static Product create(String code, String name, String description, ProductType productType) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return new Product(code, name, description, productType);
    }

    @PreUpdate
    void refreshUpdatedAt() {
        updatedAt = LocalDateTime.now();
    }

    public void activate() {
        requireStatus(ProductStatus.DRAFT, "Only draft products can be activated");
        this.status = ProductStatus.ACTIVE;
    }

    public void retire() {
        if (status == ProductStatus.RETIRED) {
            throw new IllegalStateException("Product is already retired");
        }
        this.status = ProductStatus.RETIRED;
    }

    public void updateDetails(String name, String description) {
        this.name = name;
        this.description = description;
    }

    private void requireStatus(ProductStatus expected, String message) {
        if (status != expected) {
            throw new IllegalStateException(message);
        }
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

    public String getDescription() {
        return description;
    }

    public ProductType getProductType() {
        return productType;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
