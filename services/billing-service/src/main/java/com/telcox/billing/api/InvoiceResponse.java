package com.telcox.billing.api;

import com.telcox.billing.domain.Invoice;
import com.telcox.billing.domain.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        String invoiceNumber,
        UUID billingAccountId,
        UUID subscriptionId,
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        String currency,
        InvoiceStatus status,
        LocalDateTime issuedAt,
        LocalDate dueDate
) {
    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(), invoice.getInvoiceNumber(), invoice.getBillingAccountId(), invoice.getSubscriptionId(),
                invoice.getPeriodStart(), invoice.getPeriodEnd(), invoice.getSubtotal(), invoice.getTaxAmount(),
                invoice.getTotalAmount(), invoice.getCurrency(), invoice.getStatus(), invoice.getIssuedAt(), invoice.getDueDate()
        );
    }
}
