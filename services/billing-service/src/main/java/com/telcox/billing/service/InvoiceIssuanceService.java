package com.telcox.billing.service;

import com.telcox.billing.domain.BillingOutboxEvent;
import com.telcox.billing.domain.Invoice;
import com.telcox.billing.repository.BillingOutboxEventRepository;
import com.telcox.billing.repository.InvoiceRepository;
import com.telcox.common.exception.BusinessException;
import com.telcox.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * BILL-03 / FR-23: Fatura kalemleri tamamlandiktan sonra faturayi ISSUED durumuna
 * gecirir ve notification-service'in tuketecegi "InvoiceIssued" event'ini
 * outbox'a yazar. PDF'in kendisi event payload'ina gomulmez (buyuk veri) -
 * notification-service, invoiceId ile InvoiceController.downloadPdf endpoint'ini
 * cagirarak PDF'i ayrica cekmelidir.
 */
@Service
public class InvoiceIssuanceService {

    private static final int DEFAULT_DUE_DAYS = 14;
    private static final String AGGREGATE_TYPE = "INVOICE";

    private final InvoiceRepository invoiceRepository;
    private final BillingOutboxEventRepository outboxEventRepository;

    public InvoiceIssuanceService(InvoiceRepository invoiceRepository, BillingOutboxEventRepository outboxEventRepository) {
        this.invoiceRepository = invoiceRepository;
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    public Invoice issue(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Invoice not found: " + invoiceId));

        LocalDate dueDate = LocalDate.now().plusDays(DEFAULT_DUE_DAYS);
        invoice.issue(dueDate);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("invoiceId", invoice.getId().toString());
        payload.put("invoiceNumber", invoice.getInvoiceNumber());
        payload.put("billingAccountId", invoice.getBillingAccountId().toString());
        payload.put("totalAmount", invoice.getTotalAmount().toString());
        payload.put("currency", invoice.getCurrency());
        payload.put("dueDate", dueDate.toString());

        BillingOutboxEvent event = new BillingOutboxEvent(invoice.getId(), AGGREGATE_TYPE, "InvoiceIssued",
                payload, "invoice-issue-" + UUID.randomUUID());
        outboxEventRepository.save(event);

        return invoice;
    }
}
