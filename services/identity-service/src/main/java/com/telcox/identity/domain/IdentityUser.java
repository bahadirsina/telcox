package com.telcox.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "identity_service_user")
public class IdentityUser {

    @Id
    private UUID id;

    @Column(name = "keycloak_subject", unique = true)
    private UUID keycloakSubject;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(length = 20)
    private String locale;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected IdentityUser() {
    }

    public IdentityUser(UUID keycloakSubject, String email, String displayName) {
        this.id = UUID.randomUUID();
        this.keycloakSubject = keycloakSubject;
        this.email = email;
        this.status = "ACTIVE";
        this.displayName = displayName;
        this.locale = "tr-TR";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.lastSeenAt = this.createdAt;
    }

    public void refreshFromTrustedContext(String email, String displayName) {
        boolean changed = false;
        if (email != null && !email.isBlank() && !email.equals(this.email)) {
            this.email = email;
            changed = true;
        }
        if (displayName != null && !displayName.isBlank() && !displayName.equals(this.displayName)) {
            this.displayName = displayName;
            changed = true;
        }
        this.lastSeenAt = LocalDateTime.now();
        if (changed) {
            touch();
        }
    }

    public void updateProfile(String displayName, String phoneNumber, String locale) {
        if (displayName != null) {
            this.displayName = blankToNull(displayName);
        }
        if (phoneNumber != null) {
            this.phoneNumber = blankToNull(phoneNumber);
        }
        if (locale != null) {
            this.locale = blankToNull(locale);
        }
        touch();
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    public UUID getId() {
        return id;
    }

    public UUID getKeycloakSubject() {
        return keycloakSubject;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getLocale() {
        return locale;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
