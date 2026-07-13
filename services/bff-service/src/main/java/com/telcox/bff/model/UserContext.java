package com.telcox.bff.model;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public record UserContext(
        String userId,
        String username,
        String email,
        Set<String> roles
) {
    private static final String USER_ID = "X-Telcox-User-Id";
    private static final String USER_NAME = "X-Telcox-User-Name";
    private static final String USER_EMAIL = "X-Telcox-User-Email";
    private static final String USER_ROLES = "X-Telcox-User-Roles";

    public static UserContext from(HttpServletRequest request) {
        String userId = valueOrDefault(request.getHeader(USER_ID), "anonymous");
        String username = valueOrDefault(request.getHeader(USER_NAME), userId);
        String email = valueOrDefault(request.getHeader(USER_EMAIL), "");
        Set<String> roles = normalizeRoles(request.getHeader(USER_ROLES));
        return new UserContext(userId, username, email, roles);
    }

    public UserContextResponse toResponse() {
        return new UserContextResponse(userId, username, email, roles, visibleSections());
    }

    public List<String> visibleSections() {
        TreeSet<String> sections = new TreeSet<>();
        sections.add("dashboard");
        sections.add("customers");
        sections.add("orders");
        sections.add("subscriptions");

        if (hasAnyRole("ADMIN", "FINANCE", "BILLING")) {
            sections.add("billing");
            sections.add("payments");
        }
        if (hasAnyRole("ADMIN", "SUPPORT", "TICKET", "AGENT")) {
            sections.add("tickets");
        }
        if (hasAnyRole("ADMIN", "OPS", "PLATFORM")) {
            sections.add("platform");
        }
        return List.copyOf(sections);
    }

    public boolean canSeeBilling() {
        return hasAnyRole("ADMIN", "FINANCE", "BILLING");
    }

    public boolean canSeeTickets() {
        return hasAnyRole("ADMIN", "SUPPORT", "TICKET", "AGENT");
    }

    public String cacheScope() {
        return userId + ":" + String.join("|", roles);
    }

    public boolean hasAnyRole(String... expected) {
        for (String role : expected) {
            if (roles.contains(normalizeRole(role))) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> normalizeRoles(String raw) {
        TreeSet<String> roles = new TreeSet<>();
        if (raw != null && !raw.isBlank()) {
            Arrays.stream(raw.split(","))
                    .map(UserContext::normalizeRole)
                    .filter(role -> !role.isBlank())
                    .forEach(roles::add);
        }
        if (roles.isEmpty()) {
            roles.add("CUSTOMER_CARE");
        }
        return roles;
    }

    private static String normalizeRole(String role) {
        String normalized = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }
        return normalized;
    }

    private static String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
