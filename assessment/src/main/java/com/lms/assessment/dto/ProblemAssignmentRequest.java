package com.lms.assessment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ProblemAssignmentRequest {

    @NotNull(message = "Problem ID is required")
    private Long problemId;

    @NotBlank(message = "Batch ID is required")
    private String batchId;

    private LocalDateTime dueDate;  // optional

    // ── Constructors ──────────────────────────────
    public ProblemAssignmentRequest() {}

    // ── Getters ───────────────────────────────────
    public Long getProblemId()        { return problemId; }
    public String getBatchId()        { return batchId; }
    public LocalDateTime getDueDate() { return dueDate; }

    // ── Setters ───────────────────────────────────
    public void setProblemId(Long problemId)        { this.problemId = problemId; }
    public void setBatchId(String batchId)          { this.batchId = batchId; }
    public void setDueDate(LocalDateTime dueDate)   { this.dueDate = dueDate; }
}