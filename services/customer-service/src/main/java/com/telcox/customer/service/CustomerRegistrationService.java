package com.telcox.customer.service;

import com.telcox.common.exception.BusinessException;
import com.telcox.common.exception.ErrorCode;
import com.telcox.customer.api.CustomerRegistrationRequest;
import com.telcox.customer.api.CustomerResponse;
import com.telcox.customer.api.KycDecisionRequest;
import com.telcox.customer.domain.Customer;
import com.telcox.customer.domain.CustomerOutboxEvent;
import com.telcox.customer.repository.CustomerOutboxEventRepository;
import com.telcox.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CustomerRegistrationService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String AGGREGATE_TYPE = "CUSTOMER";

    private final CustomerRepository customerRepository;
    private final CustomerOutboxEventRepository outboxEventRepository;

    public CustomerRegistrationService(CustomerRepository customerRepository,
                                       CustomerOutboxEventRepository outboxEventRepository) {
        this.customerRepository = customerRepository;
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    public CustomerResponse registerIndividual(CustomerRegistrationRequest request, String correlationId) {
        if (customerRepository.existsByNationalIdAndDeletedAtIsNull(request.nationalId())) {
            throw new BusinessException(ErrorCode.CONFLICT,
                    "A customer with this nationalId is already registered: " + request.nationalId());
        }
        String resolvedCorrelationId = correlationId == null || correlationId.isBlank()
                ? "customer-" + UUID.randomUUID()
                : correlationId;

        Customer customer = Customer.registerIndividual(
                request.identityUserId(),
                generateCustomerNumber(),
                request.firstName(),
                request.lastName(),
                request.nationalId(),
                request.dateOfBirth(),
                request.segment()
        );
        customer = customerRepository.save(customer);

        publishEvent(customer, "CustomerRegistered", resolvedCorrelationId);

        return CustomerResponse.from(customer);
    }

    public List<CustomerResponse> list() {
        return customerRepository.findByDeletedAtIsNullOrderByCreatedAtDesc()
                .stream()
                .map(CustomerResponse::from)
                .toList();
    }

    public CustomerResponse get(UUID customerId) {
        return CustomerResponse.from(findActiveCustomer(customerId));
    }

    @Transactional
    public CustomerResponse approveKyc(UUID customerId, KycDecisionRequest request) {
        Customer customer = findActiveCustomer(customerId);
        customer.approveKyc(request.reason());
        publishEvent(customer, "CustomerKycApproved", "customer-" + UUID.randomUUID());
        return CustomerResponse.from(customer);
    }

    @Transactional
    public CustomerResponse rejectKyc(UUID customerId, KycDecisionRequest request) {
        Customer customer = findActiveCustomer(customerId);
        customer.rejectKyc(request.reason());
        publishEvent(customer, "CustomerKycRejected", "customer-" + UUID.randomUUID());
        return CustomerResponse.from(customer);
    }

    private Customer findActiveCustomer(UUID customerId) {
        return customerRepository.findByIdAndDeletedAtIsNull(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Customer not found: " + customerId));
    }

    private void publishEvent(Customer customer, String eventType, String correlationId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("customerId", customer.getId().toString());
        payload.put("customerNumber", customer.getCustomerNumber());
        payload.put("status", customer.getStatus().name());
        CustomerOutboxEvent event = new CustomerOutboxEvent(customer.getId(), AGGREGATE_TYPE, eventType, payload, correlationId);
        outboxEventRepository.save(event);
    }

    private String generateCustomerNumber() {
        String candidate;
        do {
            candidate = "CUS-" + (100000 + RANDOM.nextInt(900000));
        } while (customerRepository.existsByCustomerNumber(candidate));
        return candidate;
    }
}
