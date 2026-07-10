package com.telcox.billing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * BILL-02 / FR-22: Fatura kalemi (base plan, addon, VAS veya overage).
 * ER: db.sql -> BILLING_SERVICE_INVOICE_ITEM
 */
@Entity
@Table(name = "billing_service_invoice_item")
public class InvoiceItem {

    @Id
    private UUID id;

    @Column(name = "invoice_id", nullable = false)
    private UUID invoiceId;

    @Column(name = "product_code", length = 100)
    private String productCode;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "tax_rate", nullable = false, precision = 9, scale = 4)
    private BigDecimal taxRate;

    @Column(name = "total_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalPrice;

    protected InvoiceItem() {
    }

    public InvoiceItem(UUID invoiceId, String productCode, String description, BigDecimal quantity,
                       BigDecimal unitPrice, BigDecimal taxRatePercentage) {
        this.id = UUID.randomUUID();
        this.invoiceId = invoiceId;
        this.productCode = productCode;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.taxRate = taxRatePercentage;
        // FR-22: totalPrice vergi haric net tutar (quantity * unitPrice); vergi ayrica
        // Invoice.taxAmount icinde toplu olarak tutulur (bkz. InvoiceLineService).
        this.totalPrice = quantity.multiply(unitPrice);
    }

    public BigDecimal getTaxAmount() {
        return totalPrice.multiply(taxRate).divide(BigDecimal.valueOf(100));
    }

    public UUID getId() {
        return id;
    }

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
}
