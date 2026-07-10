package com.telcox.billing.service;

import com.telcox.billing.domain.BillingAccount;
import com.telcox.billing.domain.BillingCycle;
import com.telcox.billing.domain.BillingOutboxEvent;
import com.telcox.billing.domain.CycleType;
import com.telcox.billing.domain.Invoice;
import com.telcox.billing.projection.subscription.SubscriptionProjection;
import com.telcox.billing.projection.subscription.SubscriptionProjectionRepository;
import com.telcox.billing.repository.BillingCycleRepository;
import com.telcox.billing.repository.BillingOutboxEventRepository;
import com.telcox.billing.repository.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * BILL-01 / FR-21: Aylik bill-run job'un cekirdek is mantigi.
 * MonthlyBillRunJob (Quartz) tarafindan cagrilir; her aktif abonelik icin
 * (billing_service'in kendi subscription_projection tablosundan okunur):
 *   1) Musterinin BillingAccount'u yoksa acar
 *   2) O donem icin BillingCycle acar (yoksa)
 *   3) DRAFT bir Invoice olusturur (idempotent: subscription+cycle basina tek fatura)
 *
 * NOT: Fatura kalemleri (base plan/addon/VAS/overage/tax) BILL-02 kapsaminda
 * InvoiceLineService tarafindan doldurulur; bu servis sadece "iskeleti" acar.
 */
@Service
public class BillRunService {

    private static final Logger log = LoggerFactory.getLogger(BillRunService.class);
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String AGGREGATE_TYPE = "INVOICE";

    private final SubscriptionProjectionRepository subscriptionProjectionRepository;
    private final BillingAccountService billingAccountService;
    private final BillingCycleRepository billingCycleRepository;
    private final InvoiceRepository invoiceRepository;
    private final BillingOutboxEventRepository outboxEventRepository;

    public BillRunService(SubscriptionProjectionRepository subscriptionProjectionRepository,
                          BillingAccountService billingAccountService,
                          BillingCycleRepository billingCycleRepository,
                          InvoiceRepository invoiceRepository,
                          BillingOutboxEventRepository outboxEventRepository) {
        this.subscriptionProjectionRepository = subscriptionProjectionRepository;
        this.billingAccountService = billingAccountService;
        this.billingCycleRepository = billingCycleRepository;
        this.invoiceRepository = invoiceRepository;
        this.outboxEventRepository = outboxEventRepository;
    }

    /**
     * FR-21: Verilen ay icin bill-run calistirir. MonthlyBillRunJob tarafindan
     * her ayin 1'inde otomatik, ya da BillRunController uzerinden manuel tetiklenir.
     */
    @Transactional
    public BillRunSummary runForPeriod(YearMonth period) {
        LocalDate periodStart = period.atDay(1);
        LocalDate periodEnd = period.atEndOfMonth();

        List<SubscriptionProjection> activeSubscriptions = subscriptionProjectionRepository.findBySubscriptionStatus(ACTIVE_STATUS);
        int created = 0;
        int skipped = 0;

        for (SubscriptionProjection subscription : activeSubscriptions) {
            BillingAccount account = billingAccountService.getOrCreateForCustomer(subscription.getCustomerId());
            BillingCycle cycle = getOrOpenCycle(account.getId(), periodStart, periodEnd);

            boolean alreadyInvoiced = invoiceRepository
                    .findBySubscriptionIdAndBillingCycleId(subscription.getSubscriptionId(), cycle.getId())
                    .isPresent();
            if (alreadyInvoiced) {
                skipped++;
                continue;
            }

            Invoice invoice = new Invoice(
                    generateInvoiceNumber(),
                    account.getId(),
                    cycle.getId(),
                    subscription.getSubscriptionId(),
                    periodStart,
                    periodEnd,
                    account.getCurrency()
            );
            invoice = invoiceRepository.save(invoice);
            publishEvent(invoice.getId(), "InvoiceDraftCreated", Map.of(
                    "invoiceNumber", invoice.getInvoiceNumber(),
                    "subscriptionId", subscription.getSubscriptionId().toString(),
                    "periodStart", periodStart.toString(),
                    "periodEnd", periodEnd.toString()
            ));
            created++;
        }

        log.info("Bill-run completed for period {}: {} draft invoices created, {} skipped (already invoiced)",
                period, created, skipped);
        return new BillRunSummary(period, activeSubscriptions.size(), created, skipped);
    }

    private BillingCycle getOrOpenCycle(UUID billingAccountId, LocalDate periodStart, LocalDate periodEnd) {
        return billingCycleRepository.findByBillingAccountIdAndPeriodStartAndPeriodEnd(billingAccountId, periodStart, periodEnd)
                .orElseGet(() -> billingCycleRepository.save(new BillingCycle(billingAccountId, CycleType.MONTHLY, periodStart, periodEnd)));
    }

    private String generateInvoiceNumber() {
        String candidate;
        do {
            candidate = "INV-" + YearMonth.now().toString().replace("-", "") + "-" + (100000 + RANDOM.nextInt(900000));
        } while (invoiceRepository.existsByInvoiceNumber(candidate));
        return candidate;
    }

    private void publishEvent(UUID aggregateId, String eventType, Map<String, Object> payloadExtra) {
        Map<String, Object> payload = new LinkedHashMap<>(payloadExtra);
        payload.put("invoiceId", aggregateId.toString());
        BillingOutboxEvent event = new BillingOutboxEvent(aggregateId, AGGREGATE_TYPE, eventType, payload, "bill-run-" + UUID.randomUUID());
        outboxEventRepository.save(event);
    }

    /** Bill-run sonucunun ozeti (manuel tetikleme endpoint'i icin de kullanilir). */
    public record BillRunSummary(YearMonth period, int totalActiveSubscriptions, int invoicesCreated, int alreadyInvoiced) {
    }

    /** Bir tarihin ait oldugu aydan bir onceki ayi doner. Quartz job, "bugun" 1'i iken bir onceki tam ayi faturalamak icin bunu kullanir. */
    public static YearMonth previousMonth(LocalDate reference) {
        return YearMonth.from(reference).minusMonths(1);
    }
}
