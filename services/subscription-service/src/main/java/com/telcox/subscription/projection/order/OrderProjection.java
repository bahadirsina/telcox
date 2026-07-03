package com.telcox.subscription.projection.order;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_projection")
public class OrderProjection {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    @Column(name = "tariff_id")
    private UUID tariffId;
    @Column(name = "order_status", nullable = false)
    private String orderStatus;
    @Column(name = "order_type")
    private String orderType;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected OrderProjection() {}

    public OrderProjection(UUID orderId, UUID customerId, UUID tariffId, String orderStatus,
                            String orderType, Instant createdAt, Instant updatedAt) {
        this.orderId = orderId; this.customerId = customerId; this.tariffId = tariffId;
        this.orderStatus = orderStatus; this.orderType = orderType;
        this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public void applyStatusUpdate(String newStatus, Instant updatedAt) {
        this.orderStatus = newStatus; this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public UUID getCustomerId() { return customerId; }
    public UUID getTariffId() { return tariffId; }
    public String getOrderStatus() { return orderStatus; }
    public String getOrderType() { return orderType; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
