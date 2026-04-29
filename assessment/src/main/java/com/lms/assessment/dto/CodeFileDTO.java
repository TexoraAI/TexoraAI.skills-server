package com.lms.assessment.dto;

import java.time.LocalDateTime;

public class CodeFileDTO {

    private Long          id;
    private String        studentEmail;
    private String        batchId;
    private String        language;
    private String        fileName;
    private String        code;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Constructors ──────────────────────────────
    public CodeFileDTO() {}

    public CodeFileDTO(Long id, String studentEmail, String batchId,
                       String language, String fileName, String code,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id           = id;
        this.studentEmail = studentEmail;
        this.batchId      = batchId;
        this.language     = language;
        this.fileName     = fileName;
        this.code         = code;
        this.createdAt    = createdAt;
        this.updatedAt    = updatedAt;
    }

    // ── Request DTO (for save / update) ───────────
    public static class SaveRequest {
        private String studentEmail;
        private String batchId;
        private String language;
        private String fileName;
        private String code;

        public String getStudentEmail() { return studentEmail; }
        public String getBatchId()      { return batchId; }
        public String getLanguage()     { return language; }
        public String getFileName()     { return fileName; }
        public String getCode()         { return code; }

        public void setStudentEmail(String v) { this.studentEmail = v; }
        public void setBatchId(String v)      { this.batchId = v; }
        public void setLanguage(String v)     { this.language = v; }
        public void setFileName(String v)     { this.fileName = v; }
        public void setCode(String v)         { this.code = v; }
    }

    public static class UpdateRequest {
        private String fileName;
        private String code;

        public String getFileName() { return fileName; }
        public String getCode()     { return code; }
        public void setFileName(String v) { this.fileName = v; }
        public void setCode(String v)     { this.code = v; }
    }

    // ── Getters & Setters ─────────────────────────
    public Long          getId()           { return id; }
    public String        getStudentEmail() { return studentEmail; }
    public String        getBatchId()      { return batchId; }
    public String        getLanguage()     { return language; }
    public String        getFileName()     { return fileName; }
    public String        getCode()         { return code; }
    public LocalDateTime getCreatedAt()    { return createdAt; }
    public LocalDateTime getUpdatedAt()    { return updatedAt; }

    public void setId(Long id)                       { this.id = id; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
    public void setBatchId(String batchId)           { this.batchId = batchId; }
    public void setLanguage(String language)         { this.language = language; }
    public void setFileName(String fileName)         { this.fileName = fileName; }
    public void setCode(String code)                 { this.code = code; }
    public void setCreatedAt(LocalDateTime t)        { this.createdAt = t; }
    public void setUpdatedAt(LocalDateTime t)        { this.updatedAt = t; }
}