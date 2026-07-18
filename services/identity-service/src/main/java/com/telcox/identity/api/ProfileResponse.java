package com.telcox.identity.api;

import com.telcox.identity.domain.IdentityUser;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProfileResponse(
        UUID id,
        UUID keycloakSubject,
        String email,
        String status,
        String displayName,
        String phoneNumber,
        String locale,
        LocalDateTime lastSeenAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProfileResponse from(IdentityUser user) {
        return new ProfileResponse(
                user.getId(),
                user.getKeycloakSubject(),
                user.getEmail(),
                user.getStatus(),
                user.getDisplayName(),
                user.getPhoneNumber(),
                user.getLocale(),
                user.getLastSeenAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
