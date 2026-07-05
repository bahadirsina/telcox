package com.telcox.subscription.api;

import com.telcox.subscription.domain.AddonStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AddonResponse(UUID id, String addonCode, AddonStatus status, LocalDateTime effectiveAt) {
}
