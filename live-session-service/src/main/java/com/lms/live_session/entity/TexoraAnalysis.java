package com.lms.live_session.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "texora_analysis", uniqueConstraints = {
        @UniqueConstraint(name = "uk_texora_analysis_meeting_id", columnNames = "texora_meeting_id")
})
public class TexoraAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "texora_meeting_id", nullable = false, updatable = false, length = 64)
    private String texoraMeetingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private TexoraAnalysisStatus status;

    @Column(name = "transcript_text", columnDefinition = "TEXT")
    private String transcriptText;

    @Column(name = "summary_json", columnDefinition = "TEXT")
    private String summaryJson;

    @Column(name = "failure_reason", length = 64)
    private String failureReason;

    @Column(name = "failure_message", length = 1024)
    private String failureMessage;

    @Column(name = "analysis_version", nullable = false)
    private Integer analysisVersion = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "transcript_s3_key", length = 512)
    private String transcriptS3Key;

    

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public TexoraAnalysis() {}

    public Long getId() { return id; }

    public String getTexoraMeetingId() { return texoraMeetingId; }
    public void setTexoraMeetingId(String texoraMeetingId) { this.texoraMeetingId = texoraMeetingId; }

    public TexoraAnalysisStatus getStatus() { return status; }
    public void setStatus(TexoraAnalysisStatus status) { this.status = status; }

    public String getTranscriptText() { return transcriptText; }
    public void setTranscriptText(String transcriptText) { this.transcriptText = transcriptText; }

    public String getSummaryJson() { return summaryJson; }
    public void setSummaryJson(String summaryJson) { this.summaryJson = summaryJson; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public String getFailureMessage() { return failureMessage; }
    public void setFailureMessage(String failureMessage) { this.failureMessage = failureMessage; }

    public Integer getAnalysisVersion() { return analysisVersion; }
    public void setAnalysisVersion(Integer analysisVersion) { this.analysisVersion = analysisVersion; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    public String getTranscriptS3Key() { return transcriptS3Key; }
    public void setTranscriptS3Key(String transcriptS3Key) { this.transcriptS3Key = transcriptS3Key; }
}
