package com.telcox.order.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private final OrderOrchestrationService orderOrchestrationService;

    public OrderEventListener(OrderOrchestrationService orderOrchestrationService) {
        this.orderOrchestrationService = orderOrchestrationService;
    }

    @KafkaListener(
            topics = "${telcox.topics.payment-captured:telcox.payment.payment-captured.v1}",
            groupId = "${spring.kafka.consumer.group-id:order-service}"
    )
    public void onPaymentCaptured(String message) {
        orderOrchestrationService.handlePaymentCaptured(message);
    }

    @KafkaListener(
            topics = "${telcox.topics.payment-failed:telcox.payment.payment-failed.v1}",
            groupId = "${spring.kafka.consumer.group-id:order-service}"
    )
    public void onPaymentFailed(String message) {
        orderOrchestrationService.handlePaymentFailed(message);
    }

    @KafkaListener(
            topics = "${telcox.topics.subscription-activated:telcox.subscription.subscription-activated.v1}",
            groupId = "${spring.kafka.consumer.group-id:order-service}"
    )
    public void onSubscriptionActivated(String message) {
        orderOrchestrationService.handleSubscriptionActivated(message);
    }

    @KafkaListener(
            topics = "${telcox.topics.subscription-plan-changed:telcox.subscription.subscription-plan-changed.v1}",
            groupId = "${spring.kafka.consumer.group-id:order-service}"
    )
    public void onSubscriptionPlanChanged(String message) {
        orderOrchestrationService.handleSubscriptionActivated(message);
    }

    @KafkaListener(
            topics = "${telcox.topics.subscription-addon-added:telcox.subscription.subscription-addon-added.v1}",
            groupId = "${spring.kafka.consumer.group-id:order-service}"
    )
    public void onSubscriptionAddonAdded(String message) {
        orderOrchestrationService.handleSubscriptionActivated(message);
    }
}
