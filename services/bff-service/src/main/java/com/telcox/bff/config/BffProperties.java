package com.telcox.bff.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "telcox.bff")
public class BffProperties {

    private Duration dashboardCacheTtl = Duration.ofSeconds(45);
    private Duration operationTtl = Duration.ofMinutes(30);
    private Map<String, String> upstream = new LinkedHashMap<>();

    public Duration getDashboardCacheTtl() {
        return dashboardCacheTtl;
    }

    public void setDashboardCacheTtl(Duration dashboardCacheTtl) {
        this.dashboardCacheTtl = dashboardCacheTtl;
    }

    public Duration getOperationTtl() {
        return operationTtl;
    }

    public void setOperationTtl(Duration operationTtl) {
        this.operationTtl = operationTtl;
    }

    public Map<String, String> getUpstream() {
        return upstream;
    }

    public void setUpstream(Map<String, String> upstream) {
        this.upstream = upstream;
    }

    public String upstreamUrl(String service) {
        String url = upstream.get(service);
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("Missing BFF upstream URL for " + service);
        }
        return url;
    }
}
