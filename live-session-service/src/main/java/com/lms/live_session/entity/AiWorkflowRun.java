package com.lms.live_session.entity;
 
import jakarta.persistence.*;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "ai_workflow_runs")
public class AiWorkflowRun {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(name = "workflow_id", nullable = false)
    private Long workflowId;
 
    @Column(name = "session_id")
    private Long sessionId;
 
    @Column(name = "triggered_by", nullable = false)
    private String triggeredBy;
 
    // PENDING | RUNNING | COMPLETED | FAILED
    @Column(name = "status", nullable = false)
    private String status = "PENDING";
 
    @Column(name = "result_json", columnDefinition = "TEXT")
    private String resultJson;
 
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
 
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
 
    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
 
    // Getters & Setters
    public Long getId() { return id; }
    public Long getWorkflowId() { return workflowId; }
    public void setWorkflowId(Long workflowId) { this.workflowId = workflowId; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResultJson() { return resultJson; }
    public void setResultJson(String resultJson) { this.resultJson = resultJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}