package com.telcox.order.domain;

public enum OrderStatus {
    PAYMENT_REQUESTED,
    PAYMENT_CONFIRMED,
    ACTIVATION_REQUESTED,
    COMPLETED,
    CANCELLED,
    COMPENSATING,
    FAILED
}
