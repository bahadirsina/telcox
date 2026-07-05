package com.telcox.subscription.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionEventListener {

    private final SubscriptionLifecycleService subscriptionLifecycleService;

    public SubscriptionEventListener(SubscriptionLifecycleService subscriptionLifecycleService) {
        this.subscriptionLifecycleService = subscriptionLifecycleService;
    }

    @KafkaListener(
            topics = "${telcox.topics.subscription-activation-requested:telcox.order.subscription-activation-requested.v1}",
            groupId = "${spring.kafka.consumer.group-id:subscription-service}"
    )
    public void onActivationRequested(String message) {
        subscriptionLifecycleService.handleActivationRequested(message);
    }

    @KafkaListener(
            topics = "${telcox.topics.order-compensation-requested:telcox.order.order-compensation-requested.v1}",
            groupId = "${spring.kafka.consumer.group-id:subscription-service}"
    )
    public void onCompensationRequested(String message) {
        subscriptionLifecycleService.handleCompensationRequested(message);
    }
}
