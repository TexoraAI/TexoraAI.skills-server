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

    // PENDING | RUNNING | PAUSED | COMPLETED | FAILED
    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    @Column(name = "result_json", columnDefinition = "TEXT")
    private String resultJson;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // ── Added for c2 async delay support ──────────────────────────────
    // When status == PAUSED, these carry everything needed to resume
    // exactly where execution left off.

    // Index into the parsed nodesJson array of the NEXT node to run.
    @Column(name = "next_node_index")
    private Integer nextNodeIndex;

    // Most recent `ai` node output at the point of pausing (feeds ac1).
    @Column(name = "pending_last_ai_output", columnDefinition = "TEXT")
    private String pendingLastAiOutput;

    // When the SessionResumeScheduler should pick this run back up.
    @Column(name = "resume_at")
    private LocalDateTime resumeAt;

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

    public Integer getNextNodeIndex() { return nextNodeIndex; }
    public void setNextNodeIndex(Integer nextNodeIndex) { this.nextNodeIndex = nextNodeIndex; }
    public String getPendingLastAiOutput() { return pendingLastAiOutput; }
    public void setPendingLastAiOutput(String pendingLastAiOutput) { this.pendingLastAiOutput = pendingLastAiOutput; }
    public LocalDateTime getResumeAt() { return resumeAt; }
    public void setResumeAt(LocalDateTime resumeAt) { this.resumeAt = resumeAt; }
}