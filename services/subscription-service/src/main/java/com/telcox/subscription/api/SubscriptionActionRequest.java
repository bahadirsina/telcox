package com.telcox.subscription.api;

import jakarta.validation.constraints.NotBlank;

public record SubscriptionActionRequest(@NotBlank String reason) {
}
