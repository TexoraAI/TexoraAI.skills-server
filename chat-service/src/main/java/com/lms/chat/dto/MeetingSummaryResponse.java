package com.lms.chat.dto;

import com.lms.chat.entity.MeetingSummary;
import java.time.LocalDateTime;

public class MeetingSummaryResponse {

    private Long id;
    private Long meetingId;
    private String title;
    private String requestedByEmail;
    private String requestedByRole;
    private Long organizationId;
    private String status;
    private String summaryText;
    private String keyPoints;
    private String source;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime generatedAt;

    public static MeetingSummaryResponse from(MeetingSummary m) {
        MeetingSummaryResponse r = new MeetingSummaryResponse();
        r.id                = m.getId();
        r.meetingId         = m.getMeetingId();
        r.title             = m.getTitle();
        r.requestedByEmail  = m.getRequestedByEmail();
        r.requestedByRole   = m.getRequestedByRole();
        r.organizationId    = m.getOrganizationId();
        r.status            = m.getStatus() != null ? m.getStatus().name() : null;
        r.summaryText       = m.getSummaryText();
        r.keyPoints         = m.getKeyPoints();
        r.source            = m.getSource();
        r.createdAt         = m.getCreatedAt();
        r.updatedAt         = m.getUpdatedAt();
        r.generatedAt       = m.getGeneratedAt();
        return r;
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMeetingId() { return meetingId; }
    public void setMeetingId(Long meetingId) { this.meetingId = meetingId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getRequestedByEmail() { return requestedByEmail; }
    public void setRequestedByEmail(String requestedByEmail) { this.requestedByEmail = requestedByEmail; }

    public String getRequestedByRole() { return requestedByRole; }
    public void setRequestedByRole(String requestedByRole) { this.requestedByRole = requestedByRole; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSummaryText() { return summaryText; }
    public void setSummaryText(String summaryText) { this.summaryText = summaryText; }

    public String getKeyPoints() { return keyPoints; }
    public void setKeyPoints(String keyPoints) { this.keyPoints = keyPoints; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}