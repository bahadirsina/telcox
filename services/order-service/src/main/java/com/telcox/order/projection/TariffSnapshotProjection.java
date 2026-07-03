package com.telcox.order.projection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_product_catalog_tariff_projection")
public class TariffSnapshotProjection {
    @Id
    @Column(name = "tariff_id", nullable = false, updatable = false)
    private UUID tariffId;
    @Column(name = "tariff_code", nullable = false, length = 64)
    private String tariffCode;
    @Column(name = "tariff_name", nullable = false, length = 200)
    private String tariffName;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(name = "tariff_status", nullable = false, length = 32)
    private String tariffStatus;
    @Column(name = "last_event_id", nullable = false)
    private UUID lastEventId;
    @Column(name = "source_updated_at", nullable = false)
    private OffsetDateTime sourceUpdatedAt;
    @Column(name = "projected_at", nullable = false)
    private OffsetDateTime projectedAt;

    protected TariffSnapshotProjection() {}

    public TariffSnapshotProjection(TariffSnapshotChanged event, OffsetDateTime projectedAt) {
        tariffId = event.tariffId();
        apply(event, projectedAt);
    }

    public void apply(TariffSnapshotChanged event, OffsetDateTime projectedAt) {
        tariffCode = event.tariffCode();
        tariffName = event.tariffName();
        amount = event.amount();
        currency = event.currency();
        tariffStatus = event.tariffStatus();
        lastEventId = event.eventId();
        sourceUpdatedAt = event.sourceUpdatedAt();
        this.projectedAt = projectedAt;
    }

    public UUID getTariffId() { return tariffId; }
    public BigDecimal getAmount() { return amount; }
    public UUID getLastEventId() { return lastEventId; }
    public OffsetDateTime getSourceUpdatedAt() { return sourceUpdatedAt; }
}
