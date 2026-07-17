package com.telcox.billing.service;

import com.telcox.billing.domain.BillingOutboxEvent;
import com.telcox.billing.domain.Invoice;
import com.telcox.billing.repository.BillingOutboxEventRepository;
import com.telcox.billing.repository.InvoiceRepository;
import com.telcox.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceIssuanceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private BillingOutboxEventRepository outboxEventRepository;

    private InvoiceIssuanceService invoiceIssuanceService;

    @BeforeEach
    void setUp() {
        invoiceIssuanceService = new InvoiceIssuanceService(invoiceRepository, outboxEventRepository);
    }

    @Test
    void shouldIssueInvoiceAndCreateOutboxEvent() {
        UUID invoiceId = UUID.randomUUID();
        Invoice invoice = mock(Invoice.class);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(invoice.getId()).thenReturn(invoiceId);
        when(invoice.getInvoiceNumber()).thenReturn("INV-202606-100001");
        when(invoice.getBillingAccountId()).thenReturn(UUID.randomUUID());
        when(invoice.getTotalAmount()).thenReturn(new BigDecimal("125.50"));
        when(invoice.getCurrency()).thenReturn("TRY");

        Invoice result = invoiceIssuanceService.issue(invoiceId);

        assertSame(invoice, result);
        verify(invoice).issue(any(LocalDate.class));
        verify(outboxEventRepository).save(any(BillingOutboxEvent.class));
    }

    @Test
    void shouldThrowBusinessExceptionWhenInvoiceDoesNotExist() {
        UUID invoiceId = UUID.randomUUID();
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> invoiceIssuanceService.issue(invoiceId));

        verifyNoInteractions(outboxEventRepository);
    }
}
