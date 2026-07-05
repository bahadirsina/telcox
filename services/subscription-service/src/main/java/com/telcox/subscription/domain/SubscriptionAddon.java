package com.telcox.subscription.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscription_addon")
public class SubscriptionAddon {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(name = "addon_code", nullable = false, length = 80)
    private String addonCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AddonStatus status;

    @Column(name = "effective_at", nullable = false)
    private LocalDateTime effectiveAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected SubscriptionAddon() {
    }

    public SubscriptionAddon(Subscription subscription, String addonCode) {
        LocalDateTime now = LocalDateTime.now();
        this.id = UUID.randomUUID();
        this.subscription = subscription;
        this.addonCode = addonCode;
        this.status = AddonStatus.ACTIVE;
        this.effectiveAt = now;
        this.createdAt = now;
    }

    public void remove() {
        status = AddonStatus.REMOVED;
    }

    public UUID getId() {
        return id;
    }

    public String getAddonCode() {
        return addonCode;
    }

    public AddonStatus getStatus() {
        return status;
    }

    public LocalDateTime getEffectiveAt() {
        return effectiveAt;
    }
}
