package com.telcox.identity.service;

import com.telcox.identity.api.ProfileUpdateRequest;
import com.telcox.identity.api.UserContext;
import com.telcox.identity.domain.IdentityUser;
import com.telcox.identity.repository.IdentityUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class IdentityProfileService {

    private static final String ENTITY_TYPE_PROFILE = "IDENTITY_PROFILE";

    private final IdentityUserRepository userRepository;
    private final IdentityAuditService auditService;

    public IdentityProfileService(IdentityUserRepository userRepository, IdentityAuditService auditService) {
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional
    public IdentityUser getOrCreateProfile(UserContext context, String correlationId) {
        return userRepository.findByKeycloakSubject(context.keycloakSubject())
                .map(user -> {
                    user.refreshFromTrustedContext(context.email(), context.username());
                    return user;
                })
                .orElseGet(() -> createProfile(context, correlationId));
    }

    @Transactional
    public IdentityUser updateProfile(UserContext context, ProfileUpdateRequest request, String correlationId) {
        IdentityUser user = getOrCreateProfile(context, correlationId);
        Map<String, Object> oldValue = profileSnapshot(user);
        user.updateProfile(request.displayName(), request.phoneNumber(), request.locale());
        Map<String, Object> newValue = profileSnapshot(user);
        auditService.writeAuditLog(user.getId(), "PROFILE_UPDATED", ENTITY_TYPE_PROFILE, user.getId(),
                oldValue, newValue, correlationId);
        return user;
    }

    private IdentityUser createProfile(UserContext context, String correlationId) {
        IdentityUser user = new IdentityUser(
                context.keycloakSubject(),
                resolveEmail(context),
                resolveDisplayName(context)
        );
        IdentityUser saved = userRepository.save(user);
        auditService.writeAuditLog(saved.getId(), "PROFILE_CREATED", ENTITY_TYPE_PROFILE, saved.getId(),
                null, profileSnapshot(saved), correlationId);
        return saved;
    }

    private String resolveEmail(UserContext context) {
        if (context.email() != null && !context.email().isBlank()) {
            return context.email();
        }
        return context.keycloakSubject() + "@keycloak.telcox.local";
    }

    private String resolveDisplayName(UserContext context) {
        if (context.username() != null && !context.username().isBlank()) {
            return context.username();
        }
        return context.email();
    }

    private Map<String, Object> profileSnapshot(IdentityUser user) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", user.getId());
        snapshot.put("keycloakSubject", user.getKeycloakSubject());
        snapshot.put("email", user.getEmail());
        snapshot.put("status", user.getStatus());
        snapshot.put("displayName", user.getDisplayName());
        snapshot.put("phoneNumber", user.getPhoneNumber());
        snapshot.put("locale", user.getLocale());
        return snapshot;
    }
}
