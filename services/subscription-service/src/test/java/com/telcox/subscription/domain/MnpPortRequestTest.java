package com.telcox.subscription.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MnpPortRequestTest {

    @Test
    void progressesThroughValidMnpStates() {
        Subscription subscription = Subscription.pendingPortIn(
                UUID.randomUUID(),
                "+905551234567",
                "899001001234567890",
                "ATL-40-P",
                "corr-1"
        );
        MnpPortRequest request = new MnpPortRequest(subscription, "DONOR", "TELCOX");

        request.advance(MnpStatus.VALIDATING, null);
        request.advance(MnpStatus.PORTING, null);
        request.advance(MnpStatus.COMPLETED, null);

        assertThat(request.getStatus()).isEqualTo(MnpStatus.COMPLETED);
        assertThat(request.getCompletedAt()).isNotNull();
    }

    @Test
    void rejectsInvalidMnpTransition() {
        Subscription subscription = Subscription.pendingPortIn(
                UUID.randomUUID(),
                "+905551234567",
                null,
                "ATL-40-P",
                "corr-2"
        );
        MnpPortRequest request = new MnpPortRequest(subscription, "DONOR", "TELCOX");

        assertThatThrownBy(() -> request.advance(MnpStatus.COMPLETED, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid MNP transition");
    }
}
