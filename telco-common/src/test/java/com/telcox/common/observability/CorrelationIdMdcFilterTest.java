package com.telcox.common.observability;

import jakarta.servlet.ServletException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdMdcFilterTest {

    private final CorrelationIdMdcFilter filter = new CorrelationIdMdcFilter();

    @Test
    void usesIncomingCorrelationIdInMdcAndResponse() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
        request.addHeader(CorrelationIds.HEADER_NAME, "corr-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> mdcValueInsideChain = new AtomicReference<>();
        FilterChain chain = (servletRequest, servletResponse) ->
                mdcValueInsideChain.set(MDC.get(CorrelationIds.MDC_KEY));

        filter.doFilter(request, response, chain);

        assertThat(mdcValueInsideChain).hasValue("corr-123");
        assertThat(response.getHeader(CorrelationIds.HEADER_NAME)).isEqualTo("corr-123");
        assertThat(MDC.get(CorrelationIds.MDC_KEY)).isNull();
    }

    @Test
    void createsCorrelationIdWhenHeaderIsMissing() throws ServletException, IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> mdcValueInsideChain = new AtomicReference<>();
        FilterChain chain = (servletRequest, servletResponse) ->
                mdcValueInsideChain.set(MDC.get(CorrelationIds.MDC_KEY));

        filter.doFilter(new MockHttpServletRequest("GET", "/orders"), response, chain);

        assertThat(mdcValueInsideChain.get()).startsWith("http-");
        assertThat(response.getHeader(CorrelationIds.HEADER_NAME)).isEqualTo(mdcValueInsideChain.get());
        assertThat(MDC.get(CorrelationIds.MDC_KEY)).isNull();
    }
}
