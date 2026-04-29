package com.lms.assessment.dto;

import com.lms.assessment.model.CodeSubmission.ExecutionStatus;
import java.time.LocalDateTime;

public class CodeExecutionResponse {

    private Long submissionId;
    private String language;
    private String output;
    private ExecutionStatus status;
    private Long executionTimeMs;
    private LocalDateTime timestamp;
    private String studentEmail;
    private String batchId;

    // ── Constructors ──────────────────────────────
    public CodeExecutionResponse() {}

    private CodeExecutionResponse(Builder builder) {
        this.submissionId   = builder.submissionId;
        this.language       = builder.language;
        this.output         = builder.output;
        this.status         = builder.status;
        this.executionTimeMs = builder.executionTimeMs;
        this.timestamp      = builder.timestamp;
        this.studentEmail   = builder.studentEmail;
        this.batchId        = builder.batchId;
    }

    // ── Getters ───────────────────────────────────
    public Long getSubmissionId()        { return submissionId; }
    public String getLanguage()          { return language; }
    public String getOutput()            { return output; }
    public ExecutionStatus getStatus()   { return status; }
    public Long getExecutionTimeMs()     { return executionTimeMs; }
    public LocalDateTime getTimestamp()  { return timestamp; }
    public String getStudentEmail()      { return studentEmail; }
    public String getBatchId()           { return batchId; }

    // ── Setters ───────────────────────────────────
    public void setSubmissionId(Long submissionId)       { this.submissionId = submissionId; }
    public void setLanguage(String language)             { this.language = language; }
    public void setOutput(String output)                 { this.output = output; }
    public void setStatus(ExecutionStatus status)        { this.status = status; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    public void setTimestamp(LocalDateTime timestamp)    { this.timestamp = timestamp; }
    public void setStudentEmail(String studentEmail)     { this.studentEmail = studentEmail; }
    public void setBatchId(String batchId)               { this.batchId = batchId; }

    // ── Builder ───────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long submissionId;
        private String language;
        private String output;
        private ExecutionStatus status;
        private Long executionTimeMs;
        private LocalDateTime timestamp;
        private String studentEmail;
        private String batchId;

        public Builder submissionId(Long submissionId)       { this.submissionId = submissionId; return this; }
        public Builder language(String language)             { this.language = language; return this; }
        public Builder output(String output)                 { this.output = output; return this; }
        public Builder status(ExecutionStatus status)        { this.status = status; return this; }
        public Builder executionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; return this; }
        public Builder timestamp(LocalDateTime timestamp)    { this.timestamp = timestamp; return this; }
        public Builder studentEmail(String studentEmail)     { this.studentEmail = studentEmail; return this; }
        public Builder batchId(String batchId)               { this.batchId = batchId; return this; }

        public CodeExecutionResponse build() { return new CodeExecutionResponse(this); }
    }
}