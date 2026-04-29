package com.lms.assessment.dto;

import java.time.LocalDateTime;

public class ProblemAssignmentResponse {

    private Long assignmentId;
    private Long problemId;
    private String problemTitle;
    private String batchId;
    private String assignedByEmail;
    private LocalDateTime assignedAt;
    private LocalDateTime dueDate;
    private Boolean isActive;

    // ── Constructors ──────────────────────────────
    public ProblemAssignmentResponse() {}

    private ProblemAssignmentResponse(Builder b) {
        this.assignmentId    = b.assignmentId;
        this.problemId       = b.problemId;
        this.problemTitle    = b.problemTitle;
        this.batchId         = b.batchId;
        this.assignedByEmail = b.assignedByEmail;
        this.assignedAt      = b.assignedAt;
        this.dueDate         = b.dueDate;
        this.isActive        = b.isActive;
    }

    // ── Getters ───────────────────────────────────
    public Long getAssignmentId()         { return assignmentId; }
    public Long getProblemId()            { return problemId; }
    public String getProblemTitle()       { return problemTitle; }
    public String getBatchId()            { return batchId; }
    public String getAssignedByEmail()    { return assignedByEmail; }
    public LocalDateTime getAssignedAt()  { return assignedAt; }
    public LocalDateTime getDueDate()     { return dueDate; }
    public Boolean getIsActive()          { return isActive; }

    // ── Setters ───────────────────────────────────
    public void setAssignmentId(Long assignmentId)         { this.assignmentId = assignmentId; }
    public void setProblemId(Long problemId)               { this.problemId = problemId; }
    public void setProblemTitle(String problemTitle)       { this.problemTitle = problemTitle; }
    public void setBatchId(String batchId)                 { this.batchId = batchId; }
    public void setAssignedByEmail(String assignedByEmail) { this.assignedByEmail = assignedByEmail; }
    public void setAssignedAt(LocalDateTime assignedAt)    { this.assignedAt = assignedAt; }
    public void setDueDate(LocalDateTime dueDate)          { this.dueDate = dueDate; }
    public void setIsActive(Boolean isActive)              { this.isActive = isActive; }

    // ── Builder ───────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long assignmentId;
        private Long problemId;
        private String problemTitle;
        private String batchId;
        private String assignedByEmail;
        private LocalDateTime assignedAt;
        private LocalDateTime dueDate;
        private Boolean isActive;

        public Builder assignmentId(Long assignmentId)         { this.assignmentId = assignmentId; return this; }
        public Builder problemId(Long problemId)               { this.problemId = problemId; return this; }
        public Builder problemTitle(String problemTitle)       { this.problemTitle = problemTitle; return this; }
        public Builder batchId(String batchId)                 { this.batchId = batchId; return this; }
        public Builder assignedByEmail(String assignedByEmail) { this.assignedByEmail = assignedByEmail; return this; }
        public Builder assignedAt(LocalDateTime assignedAt)    { this.assignedAt = assignedAt; return this; }
        public Builder dueDate(LocalDateTime dueDate)          { this.dueDate = dueDate; return this; }
        public Builder isActive(Boolean isActive)              { this.isActive = isActive; return this; }

        public ProblemAssignmentResponse build() { return new ProblemAssignmentResponse(this); }
    }
}