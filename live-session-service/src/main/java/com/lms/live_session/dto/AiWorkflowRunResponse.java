package com.lms.live_session.dto;

import java.time.LocalDateTime;

public class AiWorkflowRunResponse {

    private Long id;
    private Long workflowId;
    private Long sessionId;
    private String triggeredBy;
    private String status;          // PENDING | RUNNING | COMPLETED | FAILED
    private String resultJson;      // JSON array of per-node execution results
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public AiWorkflowRunResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}