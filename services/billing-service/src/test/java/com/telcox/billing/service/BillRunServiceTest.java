package com.telcox.billing.service;

import com.telcox.billing.domain.BillingAccount;
import com.telcox.billing.domain.BillingCycle;
import com.telcox.billing.domain.BillingOutboxEvent;
import com.telcox.billing.domain.Invoice;
import com.telcox.billing.projection.subscription.SubscriptionProjection;
import com.telcox.billing.projection.subscription.SubscriptionProjectionRepository;
import com.telcox.billing.repository.BillingCycleRepository;
import com.telcox.billing.repository.BillingOutboxEventRepository;
import com.telcox.billing.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillRunServiceTest {

    @Mock
    private SubscriptionProjectionRepository subscriptionProjectionRepository;
    @Mock
    private BillingAccountService billingAccountService;
    @Mock
    private BillingCycleRepository billingCycleRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private BillingOutboxEventRepository outboxEventRepository;

    private BillRunService billRunService;

    @BeforeEach
    void setUp() {
        billRunService = new BillRunService(
                subscriptionProjectionRepository,
                billingAccountService,
                billingCycleRepository,
                invoiceRepository,
                outboxEventRepository
        );
    }

    @Test
    void shouldReturnPreviousMonth() {
        assertEquals(YearMonth.of(2026, 6), BillRunService.previousMonth(LocalDate.of(2026, 7, 17)));
        assertEquals(YearMonth.of(2025, 12), BillRunService.previousMonth(LocalDate.of(2026, 1, 1)));
    }

    @Test
    void shouldCreateDraftInvoiceAndOutboxEventForActiveSubscription() {
        YearMonth period = YearMonth.of(2026, 6);
        UUID customerId = UUID.randomUUID();
        UUID subscriptionId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID cycleId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();

        SubscriptionProjection subscription = mock(SubscriptionProjection.class);
        when(subscription.getCustomerId()).thenReturn(customerId);
        when(subscription.getSubscriptionId()).thenReturn(subscriptionId);

        BillingAccount account = mock(BillingAccount.class);
        when(account.getId()).thenReturn(accountId);
        when(account.getCurrency()).thenReturn("TRY");

        BillingCycle cycle = mock(BillingCycle.class);
        when(cycle.getId()).thenReturn(cycleId);

        when(subscriptionProjectionRepository.findBySubscriptionStatus("ACTIVE"))
                .thenReturn(List.of(subscription));
        when(billingAccountService.getOrCreateForCustomer(customerId)).thenReturn(account);
        when(billingCycleRepository.findByBillingAccountIdAndPeriodStartAndPeriodEnd(
                accountId, period.atDay(1), period.atEndOfMonth()))
                .thenReturn(Optional.of(cycle));
        when(invoiceRepository.findBySubscriptionIdAndBillingCycleId(subscriptionId, cycleId))
                .thenReturn(Optional.empty());
        when(invoiceRepository.existsByInvoiceNumber(any(String.class))).thenReturn(false);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            setField(invoice, "id", invoiceId);
            return invoice;
        });

        BillRunService.BillRunSummary result = billRunService.runForPeriod(period);

        assertEquals(period, result.period());
        assertEquals(1, result.totalActiveSubscriptions());
        assertEquals(1, result.invoicesCreated());
        assertEquals(0, result.alreadyInvoiced());
        verify(invoiceRepository).save(any(Invoice.class));
        verify(outboxEventRepository).save(any(BillingOutboxEvent.class));
    }

    @Test
    void shouldSkipSubscriptionWhenInvoiceAlreadyExists() {
        YearMonth period = YearMonth.of(2026, 6);
        UUID customerId = UUID.randomUUID();
        UUID subscriptionId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID cycleId = UUID.randomUUID();

        SubscriptionProjection subscription = mock(SubscriptionProjection.class);
        when(subscription.getCustomerId()).thenReturn(customerId);
        when(subscription.getSubscriptionId()).thenReturn(subscriptionId);

        BillingAccount account = mock(BillingAccount.class);
        when(account.getId()).thenReturn(accountId);

        BillingCycle cycle = mock(BillingCycle.class);
        when(cycle.getId()).thenReturn(cycleId);

        when(subscriptionProjectionRepository.findBySubscriptionStatus("ACTIVE"))
                .thenReturn(List.of(subscription));
        when(billingAccountService.getOrCreateForCustomer(customerId)).thenReturn(account);
        when(billingCycleRepository.findByBillingAccountIdAndPeriodStartAndPeriodEnd(
                accountId, period.atDay(1), period.atEndOfMonth()))
                .thenReturn(Optional.of(cycle));
        when(invoiceRepository.findBySubscriptionIdAndBillingCycleId(subscriptionId, cycleId))
                .thenReturn(Optional.of(mock(Invoice.class)));

        BillRunService.BillRunSummary result = billRunService.runForPeriod(period);

        assertEquals(1, result.totalActiveSubscriptions());
        assertEquals(0, result.invoicesCreated());
        assertEquals(1, result.alreadyInvoiced());
        verify(invoiceRepository, never()).save(any(Invoice.class));
        verify(outboxEventRepository, never()).save(any(BillingOutboxEvent.class));
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Test fixture could not set field: " + fieldName, exception);
        }
    }
}
