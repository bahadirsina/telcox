package com.telcox.subscription.api;

import com.telcox.subscription.domain.MnpStatus;
import jakarta.validation.constraints.NotNull;

public record AdvanceMnpRequest(@NotNull MnpStatus status, String reason) {
}
