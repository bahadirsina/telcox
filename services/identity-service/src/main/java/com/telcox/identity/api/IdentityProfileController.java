package com.telcox.identity.api;

import com.telcox.identity.service.IdentityAuditService;
import com.telcox.identity.service.IdentityProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/identity")
public class IdentityProfileController {

    private final IdentityProfileService profileService;
    private final IdentityAuditService auditService;

    public IdentityProfileController(IdentityProfileService profileService, IdentityAuditService auditService) {
        this.profileService = profileService;
        this.auditService = auditService;
    }

    @GetMapping("/profile/me")
    public ProfileResponse me(@RequestHeader(value = "X-User-Id", required = false) String userId,
                              @RequestHeader(value = "X-Telcox-User-Name", required = false) String username,
                              @RequestHeader(value = "X-Telcox-User-Email", required = false) String email,
                              @RequestHeader(value = "X-Roles", required = false) String roles,
                              @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        UserContext context = UserContext.fromHeaders(userId, username, email, roles);
        return ProfileResponse.from(profileService.getOrCreateProfile(context, correlationId));
    }

    @PatchMapping("/profile/me")
    public ProfileResponse updateMe(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                    @RequestHeader(value = "X-Telcox-User-Name", required = false) String username,
                                    @RequestHeader(value = "X-Telcox-User-Email", required = false) String email,
                                    @RequestHeader(value = "X-Roles", required = false) String roles,
                                    @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                    @Valid @RequestBody ProfileUpdateRequest request) {
        UserContext context = UserContext.fromHeaders(userId, username, email, roles);
        return ProfileResponse.from(profileService.updateProfile(context, request, correlationId));
    }

    @GetMapping("/audit/me")
    public List<IdentityAuditLogResponse> myAuditLogs(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                                      @RequestHeader(value = "X-Telcox-User-Name", required = false) String username,
                                                      @RequestHeader(value = "X-Telcox-User-Email", required = false) String email,
                                                      @RequestHeader(value = "X-Roles", required = false) String roles) {
        UserContext context = UserContext.fromHeaders(userId, username, email, roles);
        UUID localUserId = profileService.getOrCreateProfile(context, null).getId();
        return auditService.listForActor(localUserId).stream()
                .map(IdentityAuditLogResponse::from)
                .toList();
    }
}
