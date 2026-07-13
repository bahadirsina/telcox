package com.telcox.bff.model;

public record DashboardCounter(
        String label,
        long value,
        String status
) {
}
