package com.telcox.billing.api;

import com.telcox.billing.domain.Invoice;
import com.telcox.billing.repository.InvoiceRepository;
import com.telcox.billing.service.InvoiceIssuanceService;
import com.telcox.billing.service.InvoiceLineService;
import com.telcox.billing.service.InvoicePdfService;
import com.telcox.common.exception.BusinessException;
import com.telcox.common.exception.ErrorCode;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * BILL-01 (goruntuleme) + BILL-02 (kalem ekleme) + BILL-03 (issue + PDF) uc noktalari.
 */
@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineService invoiceLineService;
    private final InvoiceIssuanceService invoiceIssuanceService;
    private final InvoicePdfService invoicePdfService;

    public InvoiceController(InvoiceRepository invoiceRepository,
                             InvoiceLineService invoiceLineService,
                             InvoiceIssuanceService invoiceIssuanceService,
                             InvoicePdfService invoicePdfService) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceLineService = invoiceLineService;
        this.invoiceIssuanceService = invoiceIssuanceService;
        this.invoicePdfService = invoicePdfService;
    }

    @GetMapping("/{invoiceId}")
    public InvoiceResponse get(@PathVariable UUID invoiceId) {
        return InvoiceResponse.from(findInvoice(invoiceId));
    }

    @GetMapping("/by-account/{billingAccountId}")
    public List<InvoiceResponse> listByAccount(@PathVariable UUID billingAccountId) {
        return invoiceRepository.findByBillingAccountIdOrderByCreatedAtDesc(billingAccountId).stream()
                .map(InvoiceResponse::from)
                .toList();
    }

    @GetMapping("/{invoiceId}/items")
    public List<InvoiceItemResponse> listItems(@PathVariable UUID invoiceId) {
        findInvoice(invoiceId);
        return invoiceLineService.listItems(invoiceId).stream().map(InvoiceItemResponse::from).toList();
    }

    @PostMapping("/{invoiceId}/items/base-plan")
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceItemResponse addBasePlan(@PathVariable UUID invoiceId, @Valid @RequestBody BasePlanChargeRequest request) {
        return InvoiceItemResponse.from(invoiceLineService.addBasePlanCharge(invoiceId, request));
    }

    @PostMapping("/{invoiceId}/items/addon")
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceItemResponse addAddonOrVas(@PathVariable UUID invoiceId, @Valid @RequestBody AddonLineRequest request) {
        return InvoiceItemResponse.from(invoiceLineService.addAddonOrVasLine(invoiceId, request));
    }

    @PostMapping("/{invoiceId}/items/overage")
    public InvoiceItemResponse addOverage(@PathVariable UUID invoiceId, @Valid @RequestBody OverageRequest request) {
        var item = invoiceLineService.addOverageLineIfAny(invoiceId, request);
        return item == null ? null : InvoiceItemResponse.from(item);
    }

    // ---- BILL-03: Issue + PDF ----

    @PostMapping("/{invoiceId}/issue")
    public InvoiceResponse issue(@PathVariable UUID invoiceId) {
        return InvoiceResponse.from(invoiceIssuanceService.issue(invoiceId));
    }

    @GetMapping(value = "/{invoiceId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPdf(@PathVariable UUID invoiceId) {
        Invoice invoice = findInvoice(invoiceId);
        byte[] pdf = invoicePdfService.generatePdf(invoiceId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + invoice.getInvoiceNumber() + ".pdf\"")
                .body(pdf);
    }

    private Invoice findInvoice(UUID invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Invoice not found: " + invoiceId));
    }
}
