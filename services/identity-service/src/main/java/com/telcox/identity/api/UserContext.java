package com.telcox.identity.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public record UserContext(
        UUID keycloakSubject,
        String username,
        String email,
        String roles
) {
    public static UserContext fromHeaders(String userId, String username, String email, String roles) {
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Missing gateway user context header: X-User-Id");
        }
        return new UserContext(stableSubjectKey(userId), username, email, roles);
    }

    private static UUID stableSubjectKey(String userId) {
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException ex) {
            return UUID.nameUUIDFromBytes(("keycloak:" + userId).getBytes(StandardCharsets.UTF_8));
        }
    }
}
