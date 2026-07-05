package com.telcox.subscription.service;

import com.telcox.common.exception.BusinessException;
import com.telcox.common.exception.ErrorCode;
import com.telcox.subscription.api.ActivationRequest;
import com.telcox.subscription.api.AddonRequest;
import com.telcox.subscription.api.AddonResponse;
import com.telcox.subscription.api.AdvanceMnpRequest;
import com.telcox.subscription.api.CreateMnpRequest;
import com.telcox.subscription.api.MnpResponse;
import com.telcox.subscription.api.SubscriptionActionRequest;
import com.telcox.subscription.api.SubscriptionResponse;
import com.telcox.subscription.domain.MnpPortRequest;
import com.telcox.subscription.domain.MnpStatus;
import com.telcox.subscription.domain.Subscription;
import com.telcox.subscription.domain.SubscriptionAddon;
import com.telcox.subscription.domain.SubscriptionOutboxEvent;
import com.telcox.subscription.domain.SubscriptionProcessedEvent;
import com.telcox.subscription.repository.MnpPortRequestRepository;
import com.telcox.subscription.repository.SubscriptionAddonRepository;
import com.telcox.subscription.repository.SubscriptionOutboxEventRepository;
import com.telcox.subscription.repository.SubscriptionProcessedEventRepository;
import com.telcox.subscription.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SubscriptionLifecycleService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionAddonRepository addonRepository;
    private final MnpPortRequestRepository mnpPortRequestRepository;
    private final SubscriptionOutboxEventRepository outboxEventRepository;
    private final SubscriptionProcessedEventRepository processedEventRepository;
    private final InboundEventReader inboundEventReader;

    public SubscriptionLifecycleService(SubscriptionRepository subscriptionRepository,
                                        SubscriptionAddonRepository addonRepository,
                                        MnpPortRequestRepository mnpPortRequestRepository,
                                        SubscriptionOutboxEventRepository outboxEventRepository,
                                        SubscriptionProcessedEventRepository processedEventRepository,
                                        InboundEventReader inboundEventReader) {
        this.subscriptionRepository = subscriptionRepository;
        this.addonRepository = addonRepository;
        this.mnpPortRequestRepository = mnpPortRequestRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.processedEventRepository = processedEventRepository;
        this.inboundEventReader = inboundEventReader;
    }

    @Transactional
    public SubscriptionResponse activate(ActivationRequest request, String correlationId) {
        if (request.orderId() != null) {
            var existing = subscriptionRepository.findByOrderId(request.orderId());
            if (existing.isPresent()) {
                return toResponse(existing.get());
            }
        }
        String msisdn = request.msisdn() == null || request.msisdn().isBlank() ? allocateMsisdn() : request.msisdn();
        if (subscriptionRepository.existsByMsisdn(msisdn)) {
            throw new BusinessException(ErrorCode.CONFLICT, "MSISDN already has a subscription: " + msisdn);
        }
        String resolvedCorrelationId = correlationId == null || correlationId.isBlank()
                ? "subscription-" + UUID.randomUUID()
                : correlationId;
        Subscription subscription = Subscription.active(
                request.customerId(),
                request.orderId(),
                msisdn,
                request.simIccid(),
                request.planCode(),
                resolvedCorrelationId
        );
        subscriptionRepository.save(subscription);
        addInitialAddons(subscription, request.addonCodes());
        publish(subscription, "subscription-activated", subscriptionPayload(subscription));
        return toResponse(subscription);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> list(UUID customerId) {
        List<Subscription> subscriptions = customerId == null
                ? subscriptionRepository.findAll()
                : subscriptionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        return subscriptions.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse get(UUID subscriptionId) {
        return toResponse(findSubscription(subscriptionId));
    }

    @Transactional
    public SubscriptionResponse suspend(UUID subscriptionId, SubscriptionActionRequest request) {
        Subscription subscription = findSubscription(subscriptionId);
        applyStateChange(() -> subscription.suspend(request.reason()));
        publish(subscription, "subscription-suspended", subscriptionPayload(subscription));
        return toResponse(subscription);
    }

    @Transactional
    public SubscriptionResponse reactivate(UUID subscriptionId, SubscriptionActionRequest request) {
        Subscription subscription = findSubscription(subscriptionId);
        applyStateChange(() -> subscription.reactivate(request.reason()));
        publish(subscription, "subscription-reactivated", subscriptionPayload(subscription));
        return toResponse(subscription);
    }

    @Transactional
    public SubscriptionResponse terminate(UUID subscriptionId, SubscriptionActionRequest request) {
        Subscription subscription = findSubscription(subscriptionId);
        applyStateChange(() -> subscription.terminate(request.reason()));
        publish(subscription, "subscription-terminated", subscriptionPayload(subscription));
        return toResponse(subscription);
    }

    @Transactional
    public SubscriptionResponse addAddon(UUID subscriptionId, AddonRequest request) {
        Subscription subscription = findSubscription(subscriptionId);
        addonRepository.findBySubscription_IdAndAddonCode(subscriptionId, request.addonCode()).ifPresent(existing -> {
            throw new BusinessException(ErrorCode.CONFLICT, "Addon already exists on subscription: " + request.addonCode());
        });
        SubscriptionAddon addon = subscription.addAddon(request.addonCode());
        addonRepository.save(addon);
        publish(subscription, "subscription-addon-added", addonPayload(subscription, addon));
        return toResponse(subscription);
    }

    @Transactional
    public SubscriptionResponse removeAddon(UUID subscriptionId, String addonCode) {
        Subscription subscription = findSubscription(subscriptionId);
        SubscriptionAddon addon = addonRepository.findBySubscription_IdAndAddonCode(subscriptionId, addonCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Addon not found: " + addonCode));
        addon.remove();
        publish(subscription, "subscription-addon-removed", addonPayload(subscription, addon));
        return toResponse(subscription);
    }

    @Transactional
    public MnpResponse createMnpRequest(CreateMnpRequest request, String correlationId) {
        if (subscriptionRepository.existsByMsisdn(request.msisdn())) {
            throw new BusinessException(ErrorCode.CONFLICT, "MSISDN already has a subscription: " + request.msisdn());
        }
        String resolvedCorrelationId = correlationId == null || correlationId.isBlank()
                ? "mnp-" + UUID.randomUUID()
                : correlationId;
        Subscription subscription = Subscription.pendingPortIn(
                request.customerId(),
                request.msisdn(),
                request.simIccid(),
                request.planCode(),
                resolvedCorrelationId
        );
        subscriptionRepository.save(subscription);
        MnpPortRequest portRequest = new MnpPortRequest(subscription, request.donorOperator(), request.recipientOperator());
        mnpPortRequestRepository.save(portRequest);
        publish(subscription, "mnp-port-requested", mnpPayload(portRequest));
        return toResponse(portRequest);
    }

    @Transactional
    public MnpResponse advanceMnpRequest(UUID requestId, AdvanceMnpRequest request) {
        MnpPortRequest portRequest = findMnpRequest(requestId);
        applyStateChange(() -> portRequest.advance(request.status(), request.reason()));
        if (request.status() == MnpStatus.COMPLETED) {
            Subscription subscription = findSubscription(portRequest.getSubscriptionId());
            subscription.completePortIn();
            publish(subscription, "subscription-activated", subscriptionPayload(subscription));
        } else if (request.status() == MnpStatus.REJECTED || request.status() == MnpStatus.CANCELLED) {
            Subscription subscription = findSubscription(portRequest.getSubscriptionId());
            subscription.terminate(request.reason() == null ? request.status().name() : request.reason());
        }
        outboxEventRepository.save(new SubscriptionOutboxEvent(
                portRequest.getId(),
                "MNP_PORT_REQUEST",
                "mnp-port-state-changed",
                mnpPayload(portRequest),
                portRequest.getCorrelationId()
        ));
        return toResponse(portRequest);
    }

    @Transactional(readOnly = true)
    public List<MnpResponse> listMnpRequests(UUID customerId) {
        List<MnpPortRequest> requests = customerId == null
                ? mnpPortRequestRepository.findAll()
                : mnpPortRequestRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        return requests.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MnpResponse getMnpRequest(UUID requestId) {
        return toResponse(findMnpRequest(requestId));
    }

    @Transactional
    public void handleActivationRequested(String message) {
        InboundEvent event = inboundEventReader.read(message);
        if (processedEventRepository.existsByEventId(event.eventId())) {
            return;
        }
        SubscriptionProcessedEvent processedEvent = new SubscriptionProcessedEvent(
                event.eventId(),
                event.type(),
                event.sourceService(),
                event.aggregateId()
        );
        processedEventRepository.save(processedEvent);

        String orderType = inboundEventReader.payloadText(event, "orderType");
        if (orderType == null || "NEW_LINE".equals(orderType)) {
            ActivationRequest request = new ActivationRequest(
                    inboundEventReader.payloadUuid(event, "customerId"),
                    inboundEventReader.payloadUuid(event, "orderId"),
                    inboundEventReader.payloadText(event, "msisdn"),
                    inboundEventReader.payloadText(event, "simIccid"),
                    inboundEventReader.payloadText(event, "planCode"),
                    inboundEventReader.payloadStringList(event, "addonCodes")
            );
            activate(request, event.correlationId());
        } else if ("PLAN_CHANGE".equals(orderType)) {
            applyPlanChangeOrder(event);
        } else if ("ADDON".equals(orderType)) {
            applyAddonOrder(event);
        } else {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Unsupported order type: " + orderType);
        }
        processedEvent.markProcessed();
    }

    @Transactional
    public void handleCompensationRequested(String message) {
        InboundEvent event = inboundEventReader.read(message);
        if (processedEventRepository.existsByEventId(event.eventId())) {
            return;
        }
        SubscriptionProcessedEvent processedEvent = new SubscriptionProcessedEvent(
                event.eventId(),
                event.type(),
                event.sourceService(),
                event.aggregateId()
        );
        processedEventRepository.save(processedEvent);

        boolean releaseSubscription = event.payload().path("releaseSubscription").asBoolean(false);
        UUID orderId = inboundEventReader.payloadUuid(event, "orderId");
        if (releaseSubscription && orderId != null) {
            subscriptionRepository.findByOrderId(orderId).ifPresent(subscription -> {
                if (subscription.getTerminatedAt() == null) {
                    subscription.terminate("Order compensation requested");
                    publish(subscription, "subscription-terminated", subscriptionPayload(subscription));
                }
            });
        }
        processedEvent.markProcessed();
    }

    private void applyPlanChangeOrder(InboundEvent event) {
        UUID orderId = inboundEventReader.payloadUuid(event, "orderId");
        String msisdn = inboundEventReader.payloadText(event, "msisdn");
        String planCode = inboundEventReader.payloadText(event, "planCode");
        Subscription subscription = findByMsisdn(msisdn);
        applyStateChange(() -> subscription.changePlan(planCode, "Order " + orderId));
        publish(subscription, "subscription-plan-changed", subscriptionPayload(subscription, orderId));
    }

    private void applyAddonOrder(InboundEvent event) {
        UUID orderId = inboundEventReader.payloadUuid(event, "orderId");
        String msisdn = inboundEventReader.payloadText(event, "msisdn");
        List<String> addonCodes = inboundEventReader.payloadStringList(event, "addonCodes");
        if (addonCodes.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Addon order must contain addonCodes");
        }
        Subscription subscription = findByMsisdn(msisdn);
        for (String addonCode : addonCodes) {
            addonRepository.findBySubscription_IdAndAddonCode(subscription.getId(), addonCode).ifPresent(existing -> {
                throw new BusinessException(ErrorCode.CONFLICT, "Addon already exists on subscription: " + addonCode);
            });
            addonRepository.save(subscription.addAddon(addonCode));
        }
        Map<String, Object> payload = subscriptionPayload(subscription, orderId);
        payload.put("orderedAddonCodes", addonCodes);
        publish(subscription, "subscription-addon-added", payload);
    }

    private void addInitialAddons(Subscription subscription, List<String> addonCodes) {
        if (addonCodes == null) {
            return;
        }
        addonCodes.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(subscription::addAddon)
                .forEach(addonRepository::save);
    }

    private Subscription findSubscription(UUID subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Subscription not found: " + subscriptionId));
    }

    private Subscription findByMsisdn(String msisdn) {
        if (msisdn == null || msisdn.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "msisdn is required");
        }
        return subscriptionRepository.findByMsisdn(msisdn)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Subscription not found for MSISDN: " + msisdn));
    }

    private MnpPortRequest findMnpRequest(UUID requestId) {
        return mnpPortRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "MNP request not found: " + requestId));
    }

    private void applyStateChange(Runnable stateChange) {
        try {
            stateChange.run();
        } catch (IllegalStateException ex) {
            throw new BusinessException(ErrorCode.CONFLICT, ex.getMessage(), ex);
        }
    }

    private void publish(Subscription subscription, String eventType, Map<String, Object> payload) {
        outboxEventRepository.save(new SubscriptionOutboxEvent(
                subscription.getId(),
                "SUBSCRIPTION",
                eventType,
                payload,
                subscription.getCorrelationId()
        ));
    }

    private Map<String, Object> subscriptionPayload(Subscription subscription) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("subscriptionId", subscription.getId());
        payload.put("customerId", subscription.getCustomerId());
        payload.put("orderId", subscription.getOrderId());
        payload.put("msisdn", subscription.getMsisdn());
        payload.put("simIccid", subscription.getSimIccid());
        payload.put("planCode", subscription.getPlanCode());
        payload.put("status", subscription.getStatus().name());
        payload.put("correlationId", subscription.getCorrelationId());
        payload.put("addonCodes", subscription.getAddons().stream().map(SubscriptionAddon::getAddonCode).toList());
        return payload;
    }

    private Map<String, Object> subscriptionPayload(Subscription subscription, UUID orderId) {
        Map<String, Object> payload = subscriptionPayload(subscription);
        payload.put("orderId", orderId);
        return payload;
    }

    private Map<String, Object> addonPayload(Subscription subscription, SubscriptionAddon addon) {
        Map<String, Object> payload = subscriptionPayload(subscription);
        payload.put("addonCode", addon.getAddonCode());
        payload.put("addonStatus", addon.getStatus().name());
        return payload;
    }

    private Map<String, Object> mnpPayload(MnpPortRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("mnpRequestId", request.getId());
        payload.put("subscriptionId", request.getSubscriptionId());
        payload.put("customerId", request.getCustomerId());
        payload.put("msisdn", request.getMsisdn());
        payload.put("donorOperator", request.getDonorOperator());
        payload.put("recipientOperator", request.getRecipientOperator());
        payload.put("planCode", request.getPlanCode());
        payload.put("status", request.getStatus().name());
        payload.put("correlationId", request.getCorrelationId());
        return payload;
    }

    private String allocateMsisdn() {
        String msisdn;
        do {
            msisdn = "+90555" + (1000000 + RANDOM.nextInt(9000000));
        } while (subscriptionRepository.existsByMsisdn(msisdn));
        return msisdn;
    }

    private SubscriptionResponse toResponse(Subscription subscription) {
        List<AddonResponse> addons = subscription.getAddons().stream()
                .map(addon -> new AddonResponse(addon.getId(), addon.getAddonCode(), addon.getStatus(), addon.getEffectiveAt()))
                .toList();
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getCustomerId(),
                subscription.getOrderId(),
                subscription.getMsisdn(),
                subscription.getSimIccid(),
                subscription.getPlanCode(),
                subscription.getStatus(),
                subscription.getStatusReason(),
                subscription.getCorrelationId(),
                subscription.getActivatedAt(),
                subscription.getSuspendedAt(),
                subscription.getTerminatedAt(),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt(),
                addons
        );
    }

    private MnpResponse toResponse(MnpPortRequest request) {
        return new MnpResponse(
                request.getId(),
                request.getSubscriptionId(),
                request.getCustomerId(),
                request.getMsisdn(),
                request.getDonorOperator(),
                request.getRecipientOperator(),
                request.getPlanCode(),
                request.getSimIccid(),
                request.getStatus(),
                request.getRejectionReason(),
                request.getCorrelationId(),
                request.getCreatedAt(),
                request.getUpdatedAt(),
                request.getCompletedAt()
        );
    }
}
