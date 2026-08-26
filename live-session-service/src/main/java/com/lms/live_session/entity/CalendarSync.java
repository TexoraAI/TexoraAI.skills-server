package com.lms.live_session.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "calendar_syncs")
public class CalendarSync {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId; // creator's email / Spring Security principal name

    @Column(name = "google_email", nullable = false)
    private String googleEmail;

    @Column(name = "refresh_token", nullable = false, columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false, length = 20)
    private SyncStatus syncStatus;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    @Column(name = "next_sync_at")
    private LocalDateTime nextSyncAt;

    // --- Added beyond the original spec ---
    // Event.organizationId / creatorName / creatorRole are NOT NULL columns on the
    // Event entity, but a Google Calendar event carries none of that information.
    // These are captured once when the user connects their calendar and reused on
    // every sync run. Wire population of these up to your real user/org lookup in
    // GoogleCalendarService.handleOAuthCallback() (see TODO there) - the sync will
    // throw a NOT NULL constraint violation until they're populated.
    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "creator_name")
    private String creatorName;

    @Column(name = "creator_role", length = 32)
    private String creatorRole;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public CalendarSync() {}

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getGoogleEmail() { return googleEmail; }
    public void setGoogleEmail(String googleEmail) { this.googleEmail = googleEmail; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public LocalDateTime getTokenExpiresAt() { return tokenExpiresAt; }
    public void setTokenExpiresAt(LocalDateTime tokenExpiresAt) { this.tokenExpiresAt = tokenExpiresAt; }

    public SyncStatus getSyncStatus() { return syncStatus; }
    public void setSyncStatus(SyncStatus syncStatus) { this.syncStatus = syncStatus; }

    public LocalDateTime getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(LocalDateTime lastSyncAt) { this.lastSyncAt = lastSyncAt; }

    public LocalDateTime getNextSyncAt() { return nextSyncAt; }
    public void setNextSyncAt(LocalDateTime nextSyncAt) { this.nextSyncAt = nextSyncAt; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public String getCreatorRole() { return creatorRole; }
    public void setCreatorRole(String creatorRole) { this.creatorRole = creatorRole; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}