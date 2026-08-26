package com.lms.live_session.dto;

import java.time.LocalDateTime;

public class CalendarSyncStatusDTO {

    private Boolean connected;
    private String googleEmail;
    private LocalDateTime lastSyncAt;
    private String syncMessage;

    public CalendarSyncStatusDTO() {
        this.connected = false;
        this.syncMessage = "Not connected";
    }

    public CalendarSyncStatusDTO(Boolean connected, String googleEmail,
                                  LocalDateTime lastSyncAt, String syncMessage) {
        this.connected = connected;
        this.googleEmail = googleEmail;
        this.lastSyncAt = lastSyncAt;
        this.syncMessage = syncMessage;
    }

    public Boolean getConnected() { return connected; }
    public void setConnected(Boolean connected) { this.connected = connected; }

    public String getGoogleEmail() { return googleEmail; }
    public void setGoogleEmail(String googleEmail) { this.googleEmail = googleEmail; }

    public LocalDateTime getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(LocalDateTime lastSyncAt) { this.lastSyncAt = lastSyncAt; }

    public String getSyncMessage() { return syncMessage; }
    public void setSyncMessage(String syncMessage) { this.syncMessage = syncMessage; }
}