package com.telcox.productcatalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * CAT-01 / FR-06: Urun fiyati, effective-date (validFrom/validTo) modeliyle.
 * Bir urunun ayni anda sadece bir gecerli (validTo = null veya bugunden ileri) fiyati olabilir;
 * yeni fiyat eklenirken eskisinin validTo'su kapatilir (bkz. ProductCatalogService.setPrice).
 * ER: db.sql -> PRODUCT_SERVICE_PRICE
 */
@Entity
@Table(name = "product_service_price")
public class ProductPrice {

    @Id
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "tax_included", nullable = false)
    private boolean taxIncluded;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    protected ProductPrice() {
    }

    public ProductPrice(UUID productId, BigDecimal price, String currency, boolean taxIncluded, LocalDate validFrom) {
        this.id = UUID.randomUUID();
        this.productId = productId;
        this.price = price;
        this.currency = currency;
        this.taxIncluded = taxIncluded;
        this.validFrom = validFrom;
    }

    /** FR-06: Bu fiyat kaydini, yeni bir fiyatin baslangicindan bir gun once kapatir. */
    public void closeAt(LocalDate closingDate) {
        this.validTo = closingDate;
    }

    public boolean isEffectiveOn(LocalDate date) {
        boolean afterStart = !date.isBefore(validFrom);
        boolean beforeEnd = validTo == null || !date.isAfter(validTo);
        return afterStart && beforeEnd;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public boolean isTaxIncluded() {
        return taxIncluded;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }
}
