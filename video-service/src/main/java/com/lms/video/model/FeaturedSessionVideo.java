package com.lms.video.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "featured_session_videos")
public class FeaturedSessionVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sessionId;

    private String fileName;

    private String url;

    @Column(columnDefinition = "TEXT")
    private String thumbnailUrl; // now stores a base64 data URI, not a file URL

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer durationSeconds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeaturedVideoStatus status;

    private Instant uploadedAt = Instant.now();

    public FeaturedSessionVideo() {}

    public Long getId() { return id; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public FeaturedVideoStatus getStatus() { return status; }
    public void setStatus(FeaturedVideoStatus status) { this.status = status; }

    public Instant getUploadedAt() { return uploadedAt; }
}