package com.lms.video.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "featured_video_transcripts")
public class FeaturedVideoTranscript {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TranscriptStatus status = TranscriptStatus.NONE;

    // Nullable — Whisper can auto-detect language; we store whatever it detected.
    private String language;

    // Populated only when status = FAILED.
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) default 'FEATURED'")
    private TranscriptSourceType sourceType = TranscriptSourceType.FEATURED;
    
    
    public FeaturedVideoTranscript() {}

    public Long getId() { return id; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public TranscriptStatus getStatus() { return status; }
    public void setStatus(TranscriptStatus status) { this.status = status; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    
    public TranscriptSourceType getSourceType() { return sourceType; }
    public void setSourceType(TranscriptSourceType sourceType) { this.sourceType = sourceType; }
}