package com.lms.live_session.dto;

import com.lms.live_session.entity.Recording;
import java.time.LocalDateTime;

public class RecordingResponse {

    private Long id;
    private Long sessionId;
    private Long batchId;
    private String trainerEmail;        // ✅ changed
    private String title;
    private String description;
    private String batchName;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSizeBytes;
    private String fileSizeMb;
    private String thumbnail;
    private String recordingType;
    private String status;
    private Integer durationMinutes;
    private Long viewCount;
    private LocalDateTime uploadedAt;
    private LocalDateTime createdAt;

    public RecordingResponse() {}

    public static RecordingResponse from(Recording r) {
        RecordingResponse res = new RecordingResponse();
        res.id              = r.getId();
        res.sessionId       = r.getSessionId();
        res.batchId         = r.getBatchId();
        res.trainerEmail    = r.getTrainerEmail();   // ✅ changed
        res.title           = r.getTitle();
        res.description     = r.getDescription();
        res.batchName       = r.getBatchName();
        res.fileName        = r.getFileName();
        res.filePath        = r.getFilePath();
        res.fileType        = r.getFileType();
        res.fileSizeBytes   = r.getFileSizeBytes();
        res.fileSizeMb      = r.getFileSizeBytes() != null
            ? String.format("%.2f MB", r.getFileSizeBytes() / (1024.0 * 1024.0))
            : "—";
        res.thumbnail       = r.getThumbnail();
        res.recordingType   = r.getRecordingType();
        res.status          = r.getStatus();
        res.durationMinutes = r.getDurationMinutes();
        res.viewCount       = r.getViewCount();
        res.uploadedAt      = r.getUploadedAt();
        res.createdAt       = r.getCreatedAt();
        return res;
    }

    public Long getId() { return id; }
    public Long getSessionId() { return sessionId; }
    public Long getBatchId() { return batchId; }
    public String getTrainerEmail() { return trainerEmail; }   // ✅ changed
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getBatchName() { return batchName; }
    public String getFileName() { return fileName; }
    public String getFilePath() { return filePath; }
    public String getFileType() { return fileType; }
    public Long getFileSizeBytes() { return fileSizeBytes; }
    public String getFileSizeMb() { return fileSizeMb; }
    public String getThumbnail() { return thumbnail; }
    public String getRecordingType() { return recordingType; }
    public String getStatus() { return status; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public Long getViewCount() { return viewCount; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}