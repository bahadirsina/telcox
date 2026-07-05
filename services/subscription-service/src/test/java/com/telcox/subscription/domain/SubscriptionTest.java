package com.telcox.subscription.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubscriptionTest {

    @Test
    void supportsSuspendReactivatePlanChangeAndTerminate() {
        Subscription subscription = Subscription.active(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "+905551234567",
                "899001001234567890",
                "ATL-40-P",
                "corr-1"
        );

        subscription.suspend("Lost SIM");
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.SUSPENDED);
        assertThat(subscription.getSuspendedAt()).isNotNull();

        subscription.changePlan("ATL-80-P", "Retention offer");
        assertThat(subscription.getPlanCode()).isEqualTo("ATL-80-P");

        subscription.reactivate("SIM replaced");
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);

        SubscriptionAddon addon = subscription.addAddon("NIGHT-20");
        assertThat(addon.getStatus()).isEqualTo(AddonStatus.ACTIVE);

        subscription.terminate("Customer churn");
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.TERMINATED);
        assertThat(subscription.getTerminatedAt()).isNotNull();
    }

    @Test
    void rejectsInvalidLifecycleTransitions() {
        Subscription subscription = Subscription.pendingPortIn(
                UUID.randomUUID(),
                "+905551234567",
                null,
                "ATL-40-P",
                "corr-2"
        );

        assertThatThrownBy(() -> subscription.suspend("not active"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only active subscriptions");
    }
}
