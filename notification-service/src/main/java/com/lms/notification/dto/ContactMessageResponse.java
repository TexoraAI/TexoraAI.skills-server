package com.lms.notification.dto;

import java.time.LocalDateTime;

public class ContactMessageResponse {

    private Long          id;
    private LocalDateTime submittedAt;
    private String        status;

    // ── Constructors ──────────────────────────────────────────
    public ContactMessageResponse() {}

    public ContactMessageResponse(Long id, LocalDateTime submittedAt, String status) {
        this.id          = id;
        this.submittedAt = submittedAt;
        this.status      = status;
    }

    // ── Getters & Setters ─────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "ContactMessageResponse{id=" + id + ", submittedAt=" + submittedAt + "}";
    }
}