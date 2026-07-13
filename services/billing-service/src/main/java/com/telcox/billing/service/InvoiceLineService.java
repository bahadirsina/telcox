package com.telcox.billing.service;

import com.telcox.billing.api.AddonLineRequest;
import com.telcox.billing.api.BasePlanChargeRequest;
import com.telcox.billing.api.OverageRequest;
import com.telcox.billing.domain.Invoice;
import com.telcox.billing.domain.InvoiceItem;
import com.telcox.billing.domain.TaxRate;
import com.telcox.billing.projection.usage.UsageProjection;
import com.telcox.billing.projection.usage.UsageProjectionRepository;
import com.telcox.billing.repository.InvoiceItemRepository;
import com.telcox.billing.repository.InvoiceRepository;
import com.telcox.billing.repository.TaxRateRepository;
import com.telcox.common.exception.BusinessException;
import com.telcox.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * BILL-02 / FR-22: Bir DRAFT faturaya kalem ekleme ve toplamlarini guncelleme.
 *
 * Kalem tipleri:
 *   - BASE_PLAN : tarife aylik ucreti (bkz. BasePlanChargeRequest yorumu - ADR-0006)
 *   - ADDON/VAS : manuel/entegrasyon kalemleri (bkz. AddonLineRequest)
 *   - OVERAGE   : UsageProjection'daki gercek kullanim, allowance'i astiginda
 *                 asan kisim icin otomatik hesaplanan kalem
 *
 * Her kalem eklendiginde Invoice.subtotal/taxAmount/totalAmount yeniden hesaplanir.
 */
@Service
public class InvoiceLineService {

    private static final BigDecimal DEFAULT_TAX_RATE_PERCENTAGE = BigDecimal.valueOf(20); // KDV %20 varsayilan

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final TaxRateRepository taxRateRepository;
    private final UsageProjectionRepository usageProjectionRepository;

    public InvoiceLineService(InvoiceRepository invoiceRepository,
                              InvoiceItemRepository invoiceItemRepository,
                              TaxRateRepository taxRateRepository,
                              UsageProjectionRepository usageProjectionRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.taxRateRepository = taxRateRepository;
        this.usageProjectionRepository = usageProjectionRepository;
    }

    /** FR-22: Tarife (base plan) aylik ucretini fatura kalemi olarak ekler. */
    @Transactional
    public InvoiceItem addBasePlanCharge(UUID invoiceId, BasePlanChargeRequest request) {
        BigDecimal taxRate = resolveTaxRatePercentage(request.taxRateCode());
        InvoiceItem item = new InvoiceItem(invoiceId, request.tariffCode(),
                "Tarife ucreti: " + request.tariffCode(), BigDecimal.ONE, request.monthlyPrice(), taxRate);
        return persistItemAndRecalculate(invoiceId, item);
    }

    /** FR-22: Addon veya VAS kalemi ekler. */
    @Transactional
    public InvoiceItem addAddonOrVasLine(UUID invoiceId, AddonLineRequest request) {
        BigDecimal taxRate = resolveTaxRatePercentage(request.taxRateCode());
        InvoiceItem item = new InvoiceItem(invoiceId, request.productCode(), request.description(),
                request.quantity(), request.unitPrice(), taxRate);
        return persistItemAndRecalculate(invoiceId, item);
    }

    /**
     * FR-22: Kota asimini (overage) hesaplayip kalem olarak ekler.
     * Faturanin subscriptionId'sine ait UsageProjection kayitlari toplanir,
     * verilen allowance'i asan miktar overageUnitPrice ile carpilarak faturalanir.
     * Asim yoksa (kullanim <= allowance) hicbir kalem eklenmez ve null doner.
     */
    @Transactional
    public InvoiceItem addOverageLineIfAny(UUID invoiceId, OverageRequest request) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Invoice not found: " + invoiceId));
        if (invoice.getSubscriptionId() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Invoice has no subscriptionId, cannot compute overage");
        }

        BigDecimal totalUsage = usageProjectionRepository.findBySubscriptionId(invoice.getSubscriptionId()).stream()
                .filter(u -> u.getUsageType().equals(request.usageType()))
                .filter(u -> isWithinPeriod(u, invoice))
                .map(UsageProjection::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal overageQuantity = totalUsage.subtract(request.allowance());
        if (overageQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            return null; // asim yok, kalem eklenmez
        }

        BigDecimal taxRate = resolveTaxRatePercentage(request.taxRateCode());
        InvoiceItem item = new InvoiceItem(invoiceId, request.usageType(),
                "Kota asimi: " + request.usageType(), overageQuantity, request.overageUnitPrice(), taxRate);
        return persistItemAndRecalculate(invoiceId, item);
    }

    public List<InvoiceItem> listItems(UUID invoiceId) {
        return invoiceItemRepository.findByInvoiceId(invoiceId);
    }

    private boolean isWithinPeriod(UsageProjection usage, Invoice invoice) {
        var periodStart = invoice.getPeriodStart().atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
        var periodEndExclusive = invoice.getPeriodEnd().plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
        return !usage.getPeriodStart().isBefore(periodStart) && usage.getPeriodStart().isBefore(periodEndExclusive);
    }

    private InvoiceItem persistItemAndRecalculate(UUID invoiceId, InvoiceItem item) {
        invoiceItemRepository.save(item);
        recalculateTotals(invoiceId);
        return item;
    }

    private void recalculateTotals(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Invoice not found: " + invoiceId));
        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(invoiceId);

        BigDecimal subtotal = items.stream().map(InvoiceItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal taxAmount = items.stream().map(InvoiceItem::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        invoice.applyTotals(subtotal, taxAmount);
    }

    private BigDecimal resolveTaxRatePercentage(String taxRateCode) {
        if (taxRateCode == null || taxRateCode.isBlank()) {
            return DEFAULT_TAX_RATE_PERCENTAGE;
        }
        return taxRateRepository.findByCode(taxRateCode)
                .map(TaxRate::getPercentage)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Tax rate not found: " + taxRateCode));
    }
}
