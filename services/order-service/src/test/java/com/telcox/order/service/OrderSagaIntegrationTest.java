package com.telcox.order.service;

import com.telcox.order.OrderServiceApplication;
import com.telcox.order.api.CreateOrderRequest;
import com.telcox.order.api.OrderResponse;
import com.telcox.order.domain.OrderOutboxEvent;
import com.telcox.order.domain.OrderStatus;
import com.telcox.order.domain.OrderType;
import com.telcox.order.repository.OrderOutboxEventRepository;
import com.telcox.order.repository.OrderProcessedEventRepository;
import com.telcox.testsupport.TelcoxIntegrationContainers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = OrderServiceApplication.class,
        properties = {
                "spring.cloud.config.enabled=false",
                "spring.kafka.listener.auto-startup=false",
                "eureka.client.enabled=false",
                "management.tracing.enabled=false",
                "management.tracing.export.zipkin.enabled=false"
        }
)
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class OrderSagaIntegrationTest {

    @Autowired
    private OrderOrchestrationService orderOrchestrationService;

    @Autowired
    private OrderOutboxEventRepository outboxEventRepository;

    @Autowired
    private OrderProcessedEventRepository processedEventRepository;

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        TelcoxIntegrationContainers.applyPostgres(registry);
    }

    @Test
    void createsOutboxEventsAndProcessesSagaEventsIdempotently() {
        UUID customerId = UUID.randomUUID();
        String correlationId = "corr-saga-it";
        CreateOrderRequest request = new CreateOrderRequest(
                customerId,
                OrderType.NEW_LINE,
                "ATL-40-P",
                List.of("NIGHT-20"),
                "+905551234567",
                "899001001234567890"
        );

        OrderResponse created = orderOrchestrationService.createOrder(request, correlationId);

        assertThat(created.status()).isEqualTo(OrderStatus.PAYMENT_REQUESTED);
        assertThat(outboxEventRepository.findAll())
                .extracting(OrderOutboxEvent::getEventType)
                .containsExactlyInAnyOrder("order-created", "payment-requested");
        assertThat(outboxEventRepository.findAll())
                .allSatisfy(event -> assertThat(event.getCorrelationId()).isEqualTo(correlationId));

        UUID paymentEventId = UUID.randomUUID();
        orderOrchestrationService.handlePaymentCaptured(eventJson(
                paymentEventId,
                "payment-captured",
                "payment-service",
                created.id(),
                correlationId
        ));

        assertThat(orderOrchestrationService.getOrder(created.id()).status())
                .isEqualTo(OrderStatus.ACTIVATION_REQUESTED);
        assertThat(outboxEventRepository.findAll())
                .extracting(OrderOutboxEvent::getEventType)
                .contains("subscription-activation-requested");
        long outboxCountAfterPayment = outboxEventRepository.count();
        long processedCountAfterPayment = processedEventRepository.count();

        orderOrchestrationService.handlePaymentCaptured(eventJson(
                paymentEventId,
                "payment-captured",
                "payment-service",
                created.id(),
                correlationId
        ));

        assertThat(outboxEventRepository.count()).isEqualTo(outboxCountAfterPayment);
        assertThat(processedEventRepository.count()).isEqualTo(processedCountAfterPayment);

        orderOrchestrationService.handleSubscriptionActivated(eventJson(
                UUID.randomUUID(),
                "subscription-activated",
                "subscription-service",
                created.id(),
                correlationId
        ));

        OrderResponse completed = orderOrchestrationService.getOrder(created.id());
        assertThat(completed.status()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(completed.saga()).extracting("eventType")
                .contains("order-created", "payment-requested", "payment-captured", "subscription-activated", "order-completed");
    }

    private static String eventJson(UUID eventId,
                                    String eventType,
                                    String sourceService,
                                    UUID orderId,
                                    String correlationId) {
        return """
                {
                  "eventId": "%s",
                  "type": "%s",
                  "aggregateId": "%s",
                  "sourceService": "%s",
                  "correlationId": "%s",
                  "payload": {
                    "orderId": "%s"
                  }
                }
                """.formatted(eventId, eventType, orderId, sourceService, correlationId, orderId);
    }
}
