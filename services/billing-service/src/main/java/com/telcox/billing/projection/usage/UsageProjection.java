package com.telcox.billing.projection.usage;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usage_projection")
public class UsageProjection {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usage_id", nullable = false, unique = true)
    private UUID usageId;
    @Column(name = "subscription_id", nullable = false)
    private UUID subscriptionId;
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    @Column(name = "usage_type", nullable = false)
    private String usageType;
    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;
    @Column(name = "unit", nullable = false)
    private String unit;
    @Column(name = "period_start", nullable = false)
    private Instant periodStart;
    @Column(name = "period_end", nullable = false)
    private Instant periodEnd;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UsageProjection() {}

    public UsageProjection(UUID usageId, UUID subscriptionId, UUID customerId, String usageType,
                            BigDecimal quantity, String unit, Instant periodStart, Instant periodEnd,
                            Instant updatedAt) {
        this.usageId = usageId; this.subscriptionId = subscriptionId; this.customerId = customerId;
        this.usageType = usageType; this.quantity = quantity; this.unit = unit;
        this.periodStart = periodStart; this.periodEnd = periodEnd; this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public UUID getUsageId() { return usageId; }
    public UUID getSubscriptionId() { return subscriptionId; }
    public UUID getCustomerId() { return customerId; }
    public String getUsageType() { return usageType; }
    public BigDecimal getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public Instant getPeriodStart() { return periodStart; }
    public Instant getPeriodEnd() { return periodEnd; }
    public Instant getUpdatedAt() { return updatedAt; }
}
