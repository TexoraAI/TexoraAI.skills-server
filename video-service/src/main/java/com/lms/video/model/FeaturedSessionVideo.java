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

    private String s3Key;

    // ✅ NEW — thumbnail no longer stores a base64 data URI; it's a real S3
    // object now, so it needs its own fileName + key, same shape as the video.
    private String thumbnailFileName;

    private String thumbnailS3Key;

    @Column(columnDefinition = "TEXT")
    private String thumbnailUrl;

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

    public String getS3Key() { return s3Key; }
    public void setS3Key(String s3Key) { this.s3Key = s3Key; }

    public String getThumbnailFileName() { return thumbnailFileName; }
    public void setThumbnailFileName(String thumbnailFileName) { this.thumbnailFileName = thumbnailFileName; }

    public String getThumbnailS3Key() { return thumbnailS3Key; }
    public void setThumbnailS3Key(String thumbnailS3Key) { this.thumbnailS3Key = thumbnailS3Key; }

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