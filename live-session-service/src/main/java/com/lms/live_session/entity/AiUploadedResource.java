package com.lms.live_session.entity;
 
import jakarta.persistence.*;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "ai_uploaded_resources")
public class AiUploadedResource {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(name = "uploaded_by", nullable = false)
    private String uploadedBy;
 
    @Column(name = "session_id")
    private Long sessionId;
 
    @Column(name = "file_name", nullable = false)
    private String fileName;
 
    @Column(name = "file_type")
    private String fileType;
 
    @Column(name = "file_size")
    private Long fileSize;
 
    @Column(name = "s3_url")
    private String s3Url;
 
    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;
 
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
 
    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
 
    // Getters & Setters
    public Long getId() { return id; }
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getS3Url() { return s3Url; }
    public void setS3Url(String s3Url) { this.s3Url = s3Url; }
    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}