package com.telcox.order.projection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_customer_snapshot_projection")
public class CustomerSnapshotProjection {
    @Id
    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;
    @Column(name = "customer_number", nullable = false, length = 64)
    private String customerNumber;
    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;
    @Column(name = "customer_status", nullable = false, length = 32)
    private String customerStatus;
    @Column(length = 64)
    private String segment;
    @Column(name = "last_event_id", nullable = false)
    private UUID lastEventId;
    @Column(name = "source_updated_at", nullable = false)
    private OffsetDateTime sourceUpdatedAt;
    @Column(name = "projected_at", nullable = false)
    private OffsetDateTime projectedAt;

    protected CustomerSnapshotProjection() {}

    public CustomerSnapshotProjection(CustomerSnapshotChanged event, OffsetDateTime projectedAt) {
        customerId = event.customerId();
        apply(event, projectedAt);
    }

    public void apply(CustomerSnapshotChanged event, OffsetDateTime projectedAt) {
        customerNumber = event.customerNumber();
        fullName = event.fullName();
        customerStatus = event.customerStatus();
        segment = event.segment();
        lastEventId = event.eventId();
        sourceUpdatedAt = event.sourceUpdatedAt();
        this.projectedAt = projectedAt;
    }

    public UUID getCustomerId() { return customerId; }
    public String getFullName() { return fullName; }
    public UUID getLastEventId() { return lastEventId; }
    public OffsetDateTime getSourceUpdatedAt() { return sourceUpdatedAt; }
}
