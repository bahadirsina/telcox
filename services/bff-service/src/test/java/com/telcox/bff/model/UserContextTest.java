package com.telcox.bff.model;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class UserContextTest {

    @Test
    void normalizesGatewayRolesAndShapesVisibleSections() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Telcox-User-Id", "user-1");
        request.addHeader("X-Telcox-User-Roles", "ROLE_ADMIN,finance");

        UserContext user = UserContext.from(request);

        assertThat(user.roles()).containsExactly("ADMIN", "FINANCE");
        assertThat(user.canSeeBilling()).isTrue();
        assertThat(user.canSeeTickets()).isTrue();
        assertThat(user.visibleSections()).contains("billing", "payments", "tickets", "platform");
    }

    @Test
    void defaultsToCustomerCareForDirectLocalRequests() {
        UserContext user = UserContext.from(new MockHttpServletRequest());

        assertThat(user.userId()).isEqualTo("anonymous");
        assertThat(user.roles()).containsExactly("CUSTOMER_CARE");
        assertThat(user.canSeeBilling()).isFalse();
        assertThat(user.visibleSections()).contains("dashboard", "customers", "orders", "subscriptions");
    }
}
