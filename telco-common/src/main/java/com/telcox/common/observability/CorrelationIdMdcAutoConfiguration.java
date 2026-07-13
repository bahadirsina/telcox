package com.telcox.common.observability;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(OncePerRequestFilter.class)
public class CorrelationIdMdcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CorrelationIdMdcFilter.class)
    FilterRegistrationBean<CorrelationIdMdcFilter> correlationIdMdcFilterRegistration() {
        FilterRegistrationBean<CorrelationIdMdcFilter> registration = new FilterRegistrationBean<>(new CorrelationIdMdcFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
