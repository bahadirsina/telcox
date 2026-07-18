package com.telcox.identity.service;

import com.telcox.identity.api.ProfileUpdateRequest;
import com.telcox.identity.api.UserContext;
import com.telcox.identity.domain.IdentityUser;
import com.telcox.identity.repository.IdentityUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentityProfileServiceTest {

    @Mock
    IdentityUserRepository userRepository;

    @Mock
    IdentityAuditService auditService;

    private IdentityProfileService service;

    @BeforeEach
    void setUp() {
        service = new IdentityProfileService(userRepository, auditService);
    }

    @Test
    void shouldCreateProfileFromGatewayUserContext() {
        UUID keycloakSubject = UUID.randomUUID();
        UserContext context = new UserContext(keycloakSubject, "admin", "admin@telcox.local", "ADMIN");
        when(userRepository.findByKeycloakSubject(keycloakSubject)).thenReturn(Optional.empty());
        when(userRepository.save(any(IdentityUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IdentityUser user = service.getOrCreateProfile(context, "corr-1");

        assertThat(user.getKeycloakSubject()).isEqualTo(keycloakSubject);
        assertThat(user.getEmail()).isEqualTo("admin@telcox.local");
        assertThat(user.getDisplayName()).isEqualTo("admin");
        assertThat(user.getStatus()).isEqualTo("ACTIVE");
        verify(auditService).writeAuditLog(eq(user.getId()), eq("PROFILE_CREATED"), eq("IDENTITY_PROFILE"),
                eq(user.getId()), eq(null), any(), eq("corr-1"));
    }

    @Test
    void shouldUpdateApplicationProfileWithoutAuthResponsibilities() {
        UUID keycloakSubject = UUID.randomUUID();
        IdentityUser existing = new IdentityUser(keycloakSubject, "customer@telcox.local", "customer");
        UserContext context = new UserContext(keycloakSubject, "customer", "customer@telcox.local", "CUSTOMER");
        when(userRepository.findByKeycloakSubject(keycloakSubject)).thenReturn(Optional.of(existing));

        IdentityUser user = service.updateProfile(context,
                new ProfileUpdateRequest("Customer User", "+905551112233", "tr-TR"), "corr-2");

        assertThat(user.getDisplayName()).isEqualTo("Customer User");
        assertThat(user.getPhoneNumber()).isEqualTo("+905551112233");
        assertThat(user.getLocale()).isEqualTo("tr-TR");
        verify(auditService).writeAuditLog(eq(user.getId()), eq("PROFILE_UPDATED"), eq("IDENTITY_PROFILE"),
                eq(user.getId()), any(), any(), eq("corr-2"));
    }
}
