package com.telcox.order.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerOrderTest {

    @Test
    void progressesThroughPaymentActivationAndCompletion() {
        CustomerOrder order = CustomerOrder.create(
                UUID.randomUUID(),
                OrderType.NEW_LINE,
                "ATL-40-P",
                List.of("NIGHT-20"),
                "+905551234567",
                "899001001234567890",
                "corr-1"
        );

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_REQUESTED);
        assertThat(order.addonCodeList()).containsExactly("NIGHT-20");

        order.markPaymentConfirmed();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_CONFIRMED);

        order.markActivationRequested();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ACTIVATION_REQUESTED);

        order.markCompleted();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.isTerminal()).isTrue();
        assertThat(order.getCompletedAt()).isNotNull();
    }

    @Test
    void cancellationMovesOrderToTerminalState() {
        CustomerOrder order = CustomerOrder.create(
                UUID.randomUUID(),
                OrderType.ADDON,
                null,
                List.of("ROAM-5"),
                "+905551234567",
                null,
                "corr-2"
        );

        order.markCompensating("Customer requested cancellation");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPENSATING);

        order.markCancelled();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.isTerminal()).isTrue();
    }
}
