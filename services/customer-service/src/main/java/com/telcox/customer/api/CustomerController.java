package com.telcox.customer.api;

import com.telcox.customer.service.CustomerAuditService;
import com.telcox.customer.service.CustomerProfileService;
import com.telcox.customer.service.CustomerRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerRegistrationService customerRegistrationService;
    private final CustomerProfileService customerProfileService;
    private final CustomerAuditService customerAuditService;

    public CustomerController(CustomerRegistrationService customerRegistrationService,
                              CustomerProfileService customerProfileService,
                              CustomerAuditService customerAuditService) {
        this.customerRegistrationService = customerRegistrationService;
        this.customerProfileService = customerProfileService;
        this.customerAuditService = customerAuditService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse register(@Valid @RequestBody CustomerRegistrationRequest request,
                                     @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        return customerRegistrationService.registerIndividual(request, correlationId);
    }

    @GetMapping
    public List<CustomerResponse> list() {
        return customerRegistrationService.list();
    }

    @GetMapping("/{customerId}")
    public CustomerResponse get(@PathVariable UUID customerId) {
        return customerRegistrationService.get(customerId);
    }

    @PostMapping("/{customerId}/kyc/approve")
    public CustomerResponse approveKyc(@PathVariable UUID customerId, @Valid @RequestBody KycDecisionRequest request) {
        return customerRegistrationService.approveKyc(customerId, request);
    }

    @PostMapping("/{customerId}/kyc/reject")
    public CustomerResponse rejectKyc(@PathVariable UUID customerId, @Valid @RequestBody KycDecisionRequest request) {
        return customerRegistrationService.rejectKyc(customerId, request);
    }

    // ---- FR-03: Address ----

    @PostMapping("/{customerId}/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse addAddress(@PathVariable UUID customerId, @Valid @RequestBody AddressRequest request) {
        return customerProfileService.addAddress(customerId, request);
    }

    @GetMapping("/{customerId}/addresses")
    public List<AddressResponse> listAddresses(@PathVariable UUID customerId) {
        return customerProfileService.listAddresses(customerId);
    }

    @PutMapping("/{customerId}/addresses/{addressId}")
    public AddressResponse updateAddress(@PathVariable UUID customerId, @PathVariable UUID addressId,
                                         @Valid @RequestBody AddressRequest request) {
        return customerProfileService.updateAddress(customerId, addressId, request);
    }

    // ---- FR-03: Contact ----

    @PostMapping("/{customerId}/contacts")
    @ResponseStatus(HttpStatus.CREATED)
    public ContactResponse addContact(@PathVariable UUID customerId, @Valid @RequestBody ContactRequest request) {
        return customerProfileService.addContact(customerId, request);
    }

    @GetMapping("/{customerId}/contacts")
    public List<ContactResponse> listContacts(@PathVariable UUID customerId) {
        return customerProfileService.listContacts(customerId);
    }

    @PostMapping("/{customerId}/contacts/{contactId}/verify")
    public ContactResponse verifyContact(@PathVariable UUID customerId, @PathVariable UUID contactId) {
        return customerProfileService.verifyContact(customerId, contactId);
    }

    // ---- FR-03: Document ----

    @PostMapping("/{customerId}/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse uploadDocument(@PathVariable UUID customerId, @Valid @RequestBody DocumentUploadRequest request) {
        return customerProfileService.uploadDocument(customerId, request);
    }

    @GetMapping("/{customerId}/documents")
    public List<DocumentResponse> listDocuments(@PathVariable UUID customerId) {
        return customerProfileService.listDocuments(customerId);
    }

    @PostMapping("/{customerId}/documents/{documentId}/verify")
    public DocumentResponse verifyDocument(@PathVariable UUID customerId, @PathVariable UUID documentId) {
        return customerProfileService.verifyDocument(customerId, documentId);
    }

    @PostMapping("/{customerId}/documents/{documentId}/reject")
    public DocumentResponse rejectDocument(@PathVariable UUID customerId, @PathVariable UUID documentId) {
        return customerProfileService.rejectDocument(customerId, documentId);
    }

    // ---- CUST-03: Soft delete ----

    @DeleteMapping("/{customerId}")
    public void deleteCustomer(@PathVariable UUID customerId, @Valid @RequestBody DeleteCustomerRequest request) {
        customerAuditService.softDeleteCustomer(customerId, request.actorUserId(), request.reason());
    }

    // ---- CUST-03: Consent (KVKK) ----

    @PostMapping("/{customerId}/consents")
    public ConsentResponse upsertConsent(@PathVariable UUID customerId, @Valid @RequestBody ConsentRequest request) {
        return customerAuditService.upsertConsent(customerId, request);
    }

    @GetMapping("/{customerId}/consents")
    public List<ConsentResponse> listConsents(@PathVariable UUID customerId) {
        return customerAuditService.listConsents(customerId);
    }

    // ---- CUST-03: Note ----

    @PostMapping("/{customerId}/notes")
    @ResponseStatus(HttpStatus.CREATED)
    public NoteResponse addNote(@PathVariable UUID customerId, @Valid @RequestBody NoteRequest request) {
        return customerAuditService.addNote(customerId, request);
    }

    @GetMapping("/{customerId}/notes")
    public List<NoteResponse> listNotes(@PathVariable UUID customerId) {
        return customerAuditService.listNotes(customerId);
    }

    // ---- CUST-03: Audit log ----

    @GetMapping("/{customerId}/audit-logs")
    public List<AuditLogResponse> listAuditLogs(@PathVariable UUID customerId) {
        return customerAuditService.listAuditLogs(customerId);
    }
}
