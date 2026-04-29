package com.lms.assessment.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "code_files")
public class CodeFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String studentEmail;

    @Column(nullable = false)
    private String batchId;

    @Column(nullable = false)
    private String language;       // JAVA / PYTHON / JAVASCRIPT / MYSQL / BASH

    @Column(nullable = false)
    private String fileName;       // e.g. "hello.py", "Main.java"

    @Column(columnDefinition = "TEXT", nullable = false)
    private String code;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────
    public CodeFile() {}

    public CodeFile(String studentEmail, String batchId, String language,
                    String fileName, String code) {
        this.studentEmail = studentEmail;
        this.batchId      = batchId;
        this.language     = language;
        this.fileName     = fileName;
        this.code         = code;
    }

    // ── Getters & Setters ─────────────────────────
    public Long            getId()           { return id; }
    public String          getStudentEmail() { return studentEmail; }
    public String          getBatchId()      { return batchId; }
    public String          getLanguage()     { return language; }
    public String          getFileName()     { return fileName; }
    public String          getCode()         { return code; }
    public LocalDateTime   getCreatedAt()    { return createdAt; }
    public LocalDateTime   getUpdatedAt()    { return updatedAt; }

    public void setId(Long id)                       { this.id = id; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
    public void setBatchId(String batchId)           { this.batchId = batchId; }
    public void setLanguage(String language)         { this.language = language; }
    public void setFileName(String fileName)         { this.fileName = fileName; }
    public void setCode(String code)                 { this.code = code; }
    public void setCreatedAt(LocalDateTime t)        { this.createdAt = t; }
    public void setUpdatedAt(LocalDateTime t)        { this.updatedAt = t; }
}