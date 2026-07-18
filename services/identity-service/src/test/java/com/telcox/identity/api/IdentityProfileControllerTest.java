package com.telcox.identity.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telcox.identity.domain.IdentityUser;
import com.telcox.identity.service.IdentityAuditService;
import com.telcox.identity.service.IdentityProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class IdentityProfileControllerTest {

    @Mock
    IdentityProfileService profileService;

    @Mock
    IdentityAuditService auditService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new IdentityProfileController(profileService, auditService)).build();
    }

    @Test
    void shouldReturnCurrentProfileFromGatewayHeaders() throws Exception {
        UUID keycloakSubject = UUID.randomUUID();
        IdentityUser user = new IdentityUser(keycloakSubject, "admin@telcox.local", "admin");
        when(profileService.getOrCreateProfile(any(UserContext.class), eq("corr-1"))).thenReturn(user);

        mockMvc.perform(get("/api/v1/identity/profile/me")
                        .header("X-User-Id", keycloakSubject.toString())
                        .header("X-Telcox-User-Name", "admin")
                        .header("X-Telcox-User-Email", "admin@telcox.local")
                        .header("X-Correlation-Id", "corr-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keycloakSubject").value(keycloakSubject.toString()))
                .andExpect(jsonPath("$.email").value("admin@telcox.local"));
    }

    @Test
    void shouldUpdateCurrentProfile() throws Exception {
        UUID keycloakSubject = UUID.randomUUID();
        IdentityUser user = new IdentityUser(keycloakSubject, "customer@telcox.local", "Customer User");
        user.updateProfile("Customer User", "+905551112233", "tr-TR");
        when(profileService.updateProfile(any(UserContext.class), any(ProfileUpdateRequest.class), eq("corr-2")))
                .thenReturn(user);

        mockMvc.perform(patch("/api/v1/identity/profile/me")
                        .header("X-User-Id", keycloakSubject.toString())
                        .header("X-Correlation-Id", "corr-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ProfileUpdateRequest("Customer User", "+905551112233", "tr-TR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value("+905551112233"));
    }

    @Test
    void shouldRejectMissingGatewayUserContext() throws Exception {
        mockMvc.perform(get("/api/v1/identity/profile/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldListCurrentUserAuditLogs() throws Exception {
        UUID keycloakSubject = UUID.randomUUID();
        IdentityUser user = new IdentityUser(keycloakSubject, "admin@telcox.local", "admin");
        when(profileService.getOrCreateProfile(any(UserContext.class), eq(null))).thenReturn(user);
        when(auditService.listForActor(user.getId())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/identity/audit/me")
                        .header("X-User-Id", keycloakSubject.toString()))
                .andExpect(status().isOk());
    }
}
