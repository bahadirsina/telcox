package com.telcox.billing.api;

import com.telcox.billing.domain.InvoiceItem;

import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceItemResponse(
        UUID id,
        UUID invoiceId,
        String productCode,
        String description,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal taxRate,
        BigDecimal totalPrice
) {
    public static InvoiceItemResponse from(InvoiceItem item) {
        return new InvoiceItemResponse(item.getId(), item.getInvoiceId(), item.getProductCode(), item.getDescription(),
                item.getQuantity(), item.getUnitPrice(), item.getTaxRate(), item.getTotalPrice());
    }
}
