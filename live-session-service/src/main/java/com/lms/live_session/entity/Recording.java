package com.lms.live_session.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recordings")
public class Recording {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sessionId;
    private Long batchId;

    @Column(name = "trainer_email", nullable = false)
    private String trainerEmail;

    private String title;
    private String description;
    private String batchName;

    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSizeBytes;

    private String thumbnail;
    private String recordingType;   // "LIVE_AUTO" | "UPLOADED"
    private String status;          // "PROCESSING" | "READY" | "FAILED"

    private Integer durationMinutes;
    private Long viewCount;

    private LocalDateTime uploadedAt;
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.uploadedAt == null) this.uploadedAt = LocalDateTime.now();
        if (this.status == null) this.status = "PROCESSING";
        if (this.viewCount == null) this.viewCount = 0L;
        if (this.recordingType == null) this.recordingType = "UPLOADED";
    }

    public Recording() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    public String getRecordingType() { return recordingType; }
    public void setRecordingType(String recordingType) { this.recordingType = recordingType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}