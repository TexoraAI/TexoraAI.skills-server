package com.lms.chat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notebook_sources")
public class NotebookSource {

    public enum SourceType { PDF, WEBSITE, YOUTUBE, TEXT, AUDIO, IMAGE, DRIVE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notebook_id", nullable = false)
    private Notebook notebook;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @Column(name = "title")
    private String title;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "url")
    private String url;

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    @Column(columnDefinition = "TEXT")
    private String extractedContent;

    // ===== Getters =====
    public Long getId() {
        return id;
    }

    public Notebook getNotebook() {
        return notebook;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public String getTitle() {
        return title;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getUrl() {
        return url;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

public String getExtractedContent() { return extractedContent; }
    // ===== Setters =====
    public void setId(Long id) {
        this.id = id;
    }

    public void setNotebook(Notebook notebook) {
        this.notebook = notebook;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public void setExtractedContent(String extractedContent) { this.extractedContent = extractedContent; }
}