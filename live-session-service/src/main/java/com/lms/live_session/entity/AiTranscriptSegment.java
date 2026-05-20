package com.lms.live_session.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_transcript_segments")
public class AiTranscriptSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long transcriptSessionId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    private String speakerName = "Speaker 1";

    private Integer startedAtSecond; // seconds since transcript session started

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.speakerName == null) this.speakerName = "Speaker 1";
    }

    // ── Getters & Setters ───────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTranscriptSessionId() { return transcriptSessionId; }
    public void setTranscriptSessionId(Long transcriptSessionId) { this.transcriptSessionId = transcriptSessionId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getSpeakerName() { return speakerName; }
    public void setSpeakerName(String speakerName) { this.speakerName = speakerName; }

    public Integer getStartedAtSecond() { return startedAtSecond; }
    public void setStartedAtSecond(Integer startedAtSecond) { this.startedAtSecond = startedAtSecond; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}