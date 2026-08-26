package com.lms.live_session.dto;

import java.time.LocalDateTime;

public class CalendarSyncResponseDTO {

    private Long id;
    private String googleEmail;
    private String syncStatus;
    private LocalDateTime lastSyncAt;
    private LocalDateTime nextSyncAt;

    public CalendarSyncResponseDTO() {}

    public CalendarSyncResponseDTO(Long id, String googleEmail, String syncStatus,
                                    LocalDateTime lastSyncAt, LocalDateTime nextSyncAt) {
        this.id = id;
        this.googleEmail = googleEmail;
        this.syncStatus = syncStatus;
        this.lastSyncAt = lastSyncAt;
        this.nextSyncAt = nextSyncAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGoogleEmail() { return googleEmail; }
    public void setGoogleEmail(String googleEmail) { this.googleEmail = googleEmail; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }

    public LocalDateTime getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(LocalDateTime lastSyncAt) { this.lastSyncAt = lastSyncAt; }

    public LocalDateTime getNextSyncAt() { return nextSyncAt; }
    public void setNextSyncAt(LocalDateTime nextSyncAt) { this.nextSyncAt = nextSyncAt; }
}