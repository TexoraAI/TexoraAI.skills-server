package com.lms.live_session.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="recordings")
public class Recording {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sessionId;
    private String title;
    private String videoUrl;

    private Long trainerId;
    private Long batchId;

    private LocalDateTime uploadDate;

    public Recording() {}

    public Long getId() { return id; }

    public Long getSessionId() { return sessionId; }

    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getVideoUrl() { return videoUrl; }

    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public Long getTrainerId() { return trainerId; }

    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }

    public Long getBatchId() { return batchId; }

    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public LocalDateTime getUploadDate() { return uploadDate; }

    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
}