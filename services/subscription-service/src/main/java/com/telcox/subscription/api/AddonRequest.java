package com.telcox.subscription.api;

import jakarta.validation.constraints.NotBlank;

public record AddonRequest(@NotBlank String addonCode) {
}
