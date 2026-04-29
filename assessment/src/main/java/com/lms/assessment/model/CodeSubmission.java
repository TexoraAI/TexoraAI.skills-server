package com.lms.assessment.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "code_submissions", indexes = {
    @Index(name = "idx_cs_student_email", columnList = "studentEmail"),
    @Index(name = "idx_cs_batch_id",      columnList = "batchId")
})
public class CodeSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String studentEmail;

    @Column(nullable = false)
    private String batchId;

    @Column(nullable = false, length = 20)
    private String language;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String code;

    @Column(columnDefinition = "TEXT")
    private String output;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ExecutionStatus status;

    @Column(nullable = false)
    private Long executionTimeMs;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum ExecutionStatus {
        SUCCESS, COMPILE_ERROR, RUNTIME_ERROR, TIMEOUT, INVALID_LANGUAGE
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────
    public CodeSubmission() {}

    public CodeSubmission(Long id, String studentEmail, String batchId, String language,
                          String code, String output, ExecutionStatus status,
                          Long executionTimeMs, LocalDateTime createdAt) {
        this.id = id;
        this.studentEmail = studentEmail;
        this.batchId = batchId;
        this.language = language;
        this.code = code;
        this.output = output;
        this.status = status;
        this.executionTimeMs = executionTimeMs;
        this.createdAt = createdAt;
    }

    // ── Getters ───────────────────────────────────
    public Long getId()                    { return id; }
    public String getStudentEmail()        { return studentEmail; }
    public String getBatchId()             { return batchId; }
    public String getLanguage()            { return language; }
    public String getCode()                { return code; }
    public String getOutput()              { return output; }
    public ExecutionStatus getStatus()     { return status; }
    public Long getExecutionTimeMs()       { return executionTimeMs; }
    public LocalDateTime getCreatedAt()    { return createdAt; }

    // ── Setters ───────────────────────────────────
    public void setId(Long id)                           { this.id = id; }
    public void setStudentEmail(String studentEmail)     { this.studentEmail = studentEmail; }
    public void setBatchId(String batchId)               { this.batchId = batchId; }
    public void setLanguage(String language)             { this.language = language; }
    public void setCode(String code)                     { this.code = code; }
    public void setOutput(String output)                 { this.output = output; }
    public void setStatus(ExecutionStatus status)        { this.status = status; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    public void setCreatedAt(LocalDateTime createdAt)    { this.createdAt = createdAt; }

    // ── Builder ───────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String studentEmail;
        private String batchId;
        private String language;
        private String code;
        private String output;
        private ExecutionStatus status;
        private Long executionTimeMs;
        private LocalDateTime createdAt;

        public Builder id(Long id)                           { this.id = id; return this; }
        public Builder studentEmail(String studentEmail)     { this.studentEmail = studentEmail; return this; }
        public Builder batchId(String batchId)               { this.batchId = batchId; return this; }
        public Builder language(String language)             { this.language = language; return this; }
        public Builder code(String code)                     { this.code = code; return this; }
        public Builder output(String output)                 { this.output = output; return this; }
        public Builder status(ExecutionStatus status)        { this.status = status; return this; }
        public Builder executionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; return this; }
        public Builder createdAt(LocalDateTime createdAt)    { this.createdAt = createdAt; return this; }

        public CodeSubmission build() {
            return new CodeSubmission(id, studentEmail, batchId, language,
                                      code, output, status, executionTimeMs, createdAt);
        }
    }
}