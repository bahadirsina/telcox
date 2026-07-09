package com.telcox.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_template")
public class NotificationTemplate {

    @Id
    private UUID id;

    @Column(name = "template_code", nullable = false, length = 100)
    private String templateCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(nullable = false, length = 10)
    private String locale;

    @Column(name = "subject_template", length = 255)
    private String subjectTemplate;

    @Column(name = "content_template", nullable = false, columnDefinition = "text")
    private String contentTemplate;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected NotificationTemplate() {
    }

    public NotificationTemplate(String templateCode, NotificationChannel channel, String locale,
                                String subjectTemplate, String contentTemplate, boolean active) {
        this.id = UUID.randomUUID();
        this.templateCode = templateCode;
        this.channel = channel;
        this.locale = locale;
        this.subjectTemplate = subjectTemplate;
        this.contentTemplate = contentTemplate;
        this.active = active;
    }

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public void update(String subjectTemplate, String contentTemplate, boolean active) {
        this.subjectTemplate = subjectTemplate;
        this.contentTemplate = contentTemplate;
        this.active = active;
    }

    public UUID getId() { return id; }
    public String getTemplateCode() { return templateCode; }
    public NotificationChannel getChannel() { return channel; }
    public String getLocale() { return locale; }
    public String getSubjectTemplate() { return subjectTemplate; }
    public String getContentTemplate() { return contentTemplate; }
    public boolean isActive() { return active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
