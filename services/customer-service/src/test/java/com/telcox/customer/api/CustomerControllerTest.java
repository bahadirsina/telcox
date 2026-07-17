package com.telcox.customer.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telcox.customer.domain.CustomerSegment;
import com.telcox.customer.service.CustomerAuditService;
import com.telcox.customer.service.CustomerProfileService;
import com.telcox.customer.service.CustomerRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock CustomerRegistrationService registrationService;
    @Mock CustomerProfileService profileService;
    @Mock CustomerAuditService auditService;
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new CustomerController(registrationService, profileService, auditService)).build();
    }

    @Test
    void shouldReturnCreatedForValidRegistration() throws Exception {
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(UUID.randomUUID(), "Ada", "Lovelace",
                "10000000146", LocalDate.of(1995, 1, 1), CustomerSegment.MASS);
        when(registrationService.registerIndividual(any(), eq("corr-1"))).thenReturn(null);

        mockMvc.perform(post("/api/v1/customers")
                        .header("X-Correlation-Id", "corr-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnBadRequestForBlankRequiredFields() throws Exception {
        String body = "{\"firstName\":\"\",\"lastName\":\"\",\"nationalId\":\"\"}";
        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldListCustomers() throws Exception {
        when(registrationService.list()).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk());
    }
}
