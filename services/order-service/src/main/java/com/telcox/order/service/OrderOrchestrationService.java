package com.telcox.order.service;

import com.telcox.common.exception.BusinessException;
import com.telcox.common.exception.ErrorCode;
import com.telcox.order.api.CancelOrderRequest;
import com.telcox.order.api.CreateOrderRequest;
import com.telcox.order.api.OrderResponse;
import com.telcox.order.api.SagaStepResponse;
import com.telcox.order.domain.CustomerOrder;
import com.telcox.order.domain.OrderOutboxEvent;
import com.telcox.order.domain.OrderProcessedEvent;
import com.telcox.order.domain.OrderSagaHistory;
import com.telcox.order.domain.OrderStatus;
import com.telcox.order.domain.OrderType;
import com.telcox.order.domain.SagaStepStatus;
import com.telcox.order.repository.CustomerOrderRepository;
import com.telcox.order.repository.OrderOutboxEventRepository;
import com.telcox.order.repository.OrderProcessedEventRepository;
import com.telcox.order.repository.OrderSagaHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderOrchestrationService {

    private final CustomerOrderRepository orderRepository;
    private final OrderSagaHistoryRepository sagaHistoryRepository;
    private final OrderOutboxEventRepository outboxEventRepository;
    private final OrderProcessedEventRepository processedEventRepository;
    private final InboundEventReader inboundEventReader;

    public OrderOrchestrationService(CustomerOrderRepository orderRepository,
                                     OrderSagaHistoryRepository sagaHistoryRepository,
                                     OrderOutboxEventRepository outboxEventRepository,
                                     OrderProcessedEventRepository processedEventRepository,
                                     InboundEventReader inboundEventReader) {
        this.orderRepository = orderRepository;
        this.sagaHistoryRepository = sagaHistoryRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.processedEventRepository = processedEventRepository;
        this.inboundEventReader = inboundEventReader;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String correlationId) {
        validate(request);
        String resolvedCorrelationId = correlationId == null || correlationId.isBlank()
                ? "order-" + UUID.randomUUID()
                : correlationId;
        CustomerOrder order = CustomerOrder.create(
                request.customerId(),
                request.orderType(),
                request.planCode(),
                request.addonCodes(),
                request.msisdn(),
                request.simIccid(),
                resolvedCorrelationId
        );
        orderRepository.save(order);
        appendStep(order, "ORDER_CREATED", SagaStepStatus.COMPLETED, "order-created", "Order accepted");
        appendStep(order, "PAYMENT", SagaStepStatus.WAITING, "payment-requested", "Waiting for payment capture");
        publish(order, "order-created", basePayload(order));
        publish(order, "payment-requested", basePayload(order));
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listOrders(UUID customerId) {
        List<CustomerOrder> orders = customerId == null
                ? orderRepository.findAll()
                : orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        return orders.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId) {
        return toResponse(findOrder(orderId));
    }

    @Transactional
    public OrderResponse cancelOrder(UUID orderId, CancelOrderRequest request) {
        CustomerOrder order = findOrder(orderId);
        if (order.isTerminal()) {
            throw new BusinessException(ErrorCode.CONFLICT, "Terminal order cannot be cancelled");
        }
        OrderStatus previousStatus = order.getStatus();
        order.markCompensating(request.reason());
        appendStep(order, "CANCELLATION", SagaStepStatus.COMPENSATING, "order-cancelled", request.reason());
        publish(order, "order-cancelled", Map.of(
                "orderId", order.getId(),
                "customerId", order.getCustomerId(),
                "previousStatus", previousStatus.name(),
                "reason", request.reason()
        ));
        publish(order, "order-compensation-requested", Map.of(
                "orderId", order.getId(),
                "customerId", order.getCustomerId(),
                "previousStatus", previousStatus.name(),
                "reason", request.reason(),
                "releaseSubscription", previousStatus == OrderStatus.ACTIVATION_REQUESTED,
                "refundPayment", previousStatus == OrderStatus.PAYMENT_CONFIRMED || previousStatus == OrderStatus.ACTIVATION_REQUESTED
        ));
        order.markCancelled();
        return toResponse(order);
    }

    @Transactional
    public void handlePaymentCaptured(String message) {
        InboundEvent event = inboundEventReader.read(message);
        if (processedEventRepository.existsByEventId(event.eventId())) {
            return;
        }
        OrderProcessedEvent processedEvent = new OrderProcessedEvent(event.eventId(), event.type(), event.sourceService(), event.aggregateId());
        processedEventRepository.save(processedEvent);

        UUID orderId = inboundEventReader.payloadUuid(event, "orderId");
        CustomerOrder order = findOrder(orderId);
        order.markPaymentConfirmed();
        appendStep(order, "PAYMENT", SagaStepStatus.COMPLETED, event.type(), "Payment captured");
        order.markActivationRequested();
        appendStep(order, "SUBSCRIPTION_ACTIVATION", SagaStepStatus.WAITING, "subscription-activation-requested", "Waiting for subscription activation");
        publish(order, "subscription-activation-requested", basePayload(order));
        processedEvent.markProcessed();
    }

    @Transactional
    public void handlePaymentFailed(String message) {
        InboundEvent event = inboundEventReader.read(message);
        if (processedEventRepository.existsByEventId(event.eventId())) {
            return;
        }
        OrderProcessedEvent processedEvent = new OrderProcessedEvent(event.eventId(), event.type(), event.sourceService(), event.aggregateId());
        processedEventRepository.save(processedEvent);

        UUID orderId = inboundEventReader.payloadUuid(event, "orderId");
        CustomerOrder order = findOrder(orderId);
        String reason = event.payload().path("reason").asText("Payment failed");
        order.markFailed(reason);
        appendStep(order, "PAYMENT", SagaStepStatus.FAILED, event.type(), reason);
        publish(order, "order-compensation-requested", Map.of(
                "orderId", order.getId(),
                "customerId", order.getCustomerId(),
                "reason", reason,
                "refundPayment", false,
                "releaseSubscription", false
        ));
        processedEvent.markProcessed();
    }

    @Transactional
    public void handleSubscriptionActivated(String message) {
        InboundEvent event = inboundEventReader.read(message);
        if (processedEventRepository.existsByEventId(event.eventId())) {
            return;
        }
        OrderProcessedEvent processedEvent = new OrderProcessedEvent(event.eventId(), event.type(), event.sourceService(), event.aggregateId());
        processedEventRepository.save(processedEvent);

        UUID orderId = inboundEventReader.payloadUuid(event, "orderId");
        CustomerOrder order = findOrder(orderId);
        if (order.isTerminal()) {
            processedEvent.markProcessed();
            return;
        }
        order.markCompleted();
        appendStep(order, "SUBSCRIPTION_ACTIVATION", SagaStepStatus.COMPLETED, event.type(), "Subscription activated");
        appendStep(order, "ORDER_COMPLETED", SagaStepStatus.COMPLETED, "order-completed", "Order saga completed");
        publish(order, "order-completed", basePayload(order));
        processedEvent.markProcessed();
    }

    private void validate(CreateOrderRequest request) {
        if ((request.orderType() == OrderType.NEW_LINE || request.orderType() == OrderType.PLAN_CHANGE)
                && (request.planCode() == null || request.planCode().isBlank())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "planCode is required for " + request.orderType());
        }
        if ((request.orderType() == OrderType.PLAN_CHANGE || request.orderType() == OrderType.ADDON)
                && (request.msisdn() == null || request.msisdn().isBlank())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "msisdn is required for " + request.orderType());
        }
        if (request.orderType() == OrderType.ADDON && (request.addonCodes() == null || request.addonCodes().isEmpty())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "At least one addon code is required");
        }
    }

    private CustomerOrder findOrder(UUID orderId) {
        if (orderId == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "orderId is required");
        }
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Order not found: " + orderId));
    }

    private void appendStep(CustomerOrder order, String step, SagaStepStatus status, String eventType, String message) {
        sagaHistoryRepository.save(new OrderSagaHistory(order.getId(), step, status, eventType, message));
    }

    private void publish(CustomerOrder order, String eventType, Map<String, Object> payload) {
        outboxEventRepository.save(new OrderOutboxEvent(order.getId(), "ORDER", eventType, payload, order.getCorrelationId()));
    }

    private Map<String, Object> basePayload(CustomerOrder order) {
        return Map.of(
                "orderId", order.getId(),
                "orderNumber", order.getOrderNumber(),
                "customerId", order.getCustomerId(),
                "orderType", order.getOrderType().name(),
                "planCode", order.getPlanCode() == null ? "" : order.getPlanCode(),
                "addonCodes", order.addonCodeList(),
                "msisdn", order.getMsisdn() == null ? "" : order.getMsisdn(),
                "simIccid", order.getSimIccid() == null ? "" : order.getSimIccid(),
                "correlationId", order.getCorrelationId()
        );
    }

    private OrderResponse toResponse(CustomerOrder order) {
        List<SagaStepResponse> saga = sagaHistoryRepository.findByOrderIdOrderByOccurredAtAsc(order.getId())
                .stream()
                .map(step -> new SagaStepResponse(
                        step.getId(),
                        step.getStepName(),
                        step.getStepStatus(),
                        step.getEventType(),
                        step.getMessage(),
                        step.getOccurredAt()
                ))
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getOrderType(),
                order.getPlanCode(),
                order.addonCodeList(),
                order.getMsisdn(),
                order.getSimIccid(),
                order.getStatus(),
                order.getCancellationReason(),
                order.getCorrelationId(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getCompletedAt(),
                saga
        );
    }
}
