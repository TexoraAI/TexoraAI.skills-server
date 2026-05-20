package com.lms.live_session.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_transcript_sessions")
public class AiTranscriptSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long liveSessionId; // optional, may be null for standalone notes

    private String trainerEmail; // from JWT

    private String title; // optional label

    @Enumerated(EnumType.STRING)
    private TranscriptStatus status; // RECORDING | COMPLETED

    private LocalDateTime startedAt;
    private LocalDateTime stoppedAt;
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.startedAt == null) this.startedAt = LocalDateTime.now();
        if (this.status == null) this.status = TranscriptStatus.RECORDING;
    }

    public enum TranscriptStatus {
        RECORDING, COMPLETED
    }

    // ── Getters & Setters ───────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLiveSessionId() { return liveSessionId; }
    public void setLiveSessionId(Long liveSessionId) { this.liveSessionId = liveSessionId; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public TranscriptStatus getStatus() { return status; }
    public void setStatus(TranscriptStatus status) { this.status = status; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getStoppedAt() { return stoppedAt; }
    public void setStoppedAt(LocalDateTime stoppedAt) { this.stoppedAt = stoppedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}