package com.telcox.customer.service;

import com.telcox.common.exception.BusinessException;
import com.telcox.common.exception.ErrorCode;
import com.telcox.customer.api.AuditLogResponse;
import com.telcox.customer.api.ConsentRequest;
import com.telcox.customer.api.ConsentResponse;
import com.telcox.customer.api.NoteRequest;
import com.telcox.customer.api.NoteResponse;
import com.telcox.customer.domain.Customer;
import com.telcox.customer.domain.CustomerAuditLog;
import com.telcox.customer.domain.CustomerConsent;
import com.telcox.customer.domain.CustomerNote;
import com.telcox.customer.repository.CustomerAuditLogRepository;
import com.telcox.customer.repository.CustomerConsentRepository;
import com.telcox.customer.repository.CustomerNoteRepository;
import com.telcox.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CustomerAuditService {

    private static final String ENTITY_TYPE_CUSTOMER = "CUSTOMER";

    private final CustomerRepository customerRepository;
    private final CustomerConsentRepository consentRepository;
    private final CustomerNoteRepository noteRepository;
    private final CustomerAuditLogRepository auditLogRepository;

    public CustomerAuditService(CustomerRepository customerRepository,
                                CustomerConsentRepository consentRepository,
                                CustomerNoteRepository noteRepository,
                                CustomerAuditLogRepository auditLogRepository) {
        this.customerRepository = customerRepository;
        this.consentRepository = consentRepository;
        this.noteRepository = noteRepository;
        this.auditLogRepository = auditLogRepository;
    }

    // ---- Soft delete (KVKK/GDPR) ----

    @Transactional
    public void softDeleteCustomer(UUID customerId, UUID actorUserId, String reason) {
        Customer customer = customerRepository.findByIdAndDeletedAtIsNull(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Customer not found: " + customerId));

        Map<String, Object> oldValue = new LinkedHashMap<>();
        oldValue.put("status", customer.getStatus().name());
        oldValue.put("deletedAt", null);

        customer.softDelete();

        Map<String, Object> newValue = new LinkedHashMap<>();
        newValue.put("status", customer.getStatus().name());
        newValue.put("deletedAt", customer.getDeletedAt().toString());
        newValue.put("reason", reason);

        writeAuditLog(actorUserId, "SOFT_DELETE", ENTITY_TYPE_CUSTOMER, customerId, oldValue, newValue, null);
    }

    // ---- Consent (KVKK) ----

    @Transactional
    public ConsentResponse upsertConsent(UUID customerId, ConsentRequest request) {
        requireCustomerExists(customerId);
        CustomerConsent consent = consentRepository.findByCustomerIdAndConsentType(customerId, request.consentType())
                .orElseGet(() -> consentRepository.save(
                        new CustomerConsent(customerId, request.consentType(), request.channel(), false)));

        boolean wasGranted = consent.isGranted();
        if (request.granted() && !wasGranted) {
            consent.grant();
        } else if (!request.granted() && wasGranted) {
            consent.revoke();
        }

        writeAuditLog(null, request.granted() ? "CONSENT_GRANTED" : "CONSENT_REVOKED", "CUSTOMER_CONSENT",
                consent.getId(), Map.of("granted", wasGranted), Map.of("granted", consent.isGranted()), null);

        return ConsentResponse.from(consent);
    }

    public List<ConsentResponse> listConsents(UUID customerId) {
        requireCustomerExists(customerId);
        return consentRepository.findByCustomerId(customerId).stream().map(ConsentResponse::from).toList();
    }

    // ---- Note ----

    @Transactional
    public NoteResponse addNote(UUID customerId, NoteRequest request) {
        requireCustomerExists(customerId);
        CustomerNote note = new CustomerNote(customerId, request.authorUserId(), request.noteText());
        return NoteResponse.from(noteRepository.save(note));
    }

    public List<NoteResponse> listNotes(UUID customerId) {
        requireCustomerExists(customerId);
        return noteRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream().map(NoteResponse::from).toList();
    }

    // ---- Audit log query ----

    public List<AuditLogResponse> listAuditLogs(UUID customerId) {
        requireCustomerExists(customerId);
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(ENTITY_TYPE_CUSTOMER, customerId)
                .stream().map(AuditLogResponse::from).toList();
    }

    /**
     * Diger servisler/servis metotlari tarafindan cagrilabilecek genel audit-log yazma helper'i.
     */
    @Transactional
    public void writeAuditLog(UUID actorUserId, String action, String entityType, UUID entityId,
                              Map<String, Object> oldValueJson, Map<String, Object> newValueJson, String correlationId) {
        CustomerAuditLog log = new CustomerAuditLog(actorUserId, action, entityType, entityId, oldValueJson, newValueJson, correlationId);
        auditLogRepository.save(log);
    }

    private void requireCustomerExists(UUID customerId) {
        if (customerRepository.findByIdAndDeletedAtIsNull(customerId).isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Customer not found: " + customerId);
        }
    }
}
