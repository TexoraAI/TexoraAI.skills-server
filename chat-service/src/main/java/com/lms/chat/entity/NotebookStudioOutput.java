//package com.lms.chat.entity;
//
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "notebook_studio_outputs")
//public class NotebookStudioOutput {
//
//    public enum StudioType { REPORT, DATATABLE, MINDMAP, FLASHCARDS, QUIZ, AUDIO, SLIDES, VIDEO }
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "notebook_id", nullable = false)
//    private Notebook notebook;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "type", nullable = false)
//    private StudioType type;
//
//    @Column(name = "language")
//    private String language;
//
//    // Plain text/markdown for report & datatable; JSON string for
//    // mindmap/flashcards/quiz/slides; the podcast script (for reference) for audio.
//    @Column(name = "text_content", columnDefinition = "TEXT")
//    private String textContent;
//
//    // S3 key for generated media (audio/slides/video); null for text-only types.
//    @Column(name = "s3_key")
//    private String s3Key;
//
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    @PrePersist
//    protected void onCreate() {
//        this.createdAt = LocalDateTime.now();
//    }
//
//    // ===== Getters =====
//    public Long getId() { return id; }
//    public Notebook getNotebook() { return notebook; }
//    public StudioType getType() { return type; }
//    public String getLanguage() { return language; }
//    public String getTextContent() { return textContent; }
//    public String getS3Key() { return s3Key; }
//    public LocalDateTime getCreatedAt() { return createdAt; }
//
//    // ===== Setters =====
//    public void setId(Long id) { this.id = id; }
//    public void setNotebook(Notebook notebook) { this.notebook = notebook; }
//    public void setType(StudioType type) { this.type = type; }
//    public void setLanguage(String language) { this.language = language; }
//    public void setTextContent(String textContent) { this.textContent = textContent; }
//    public void setS3Key(String s3Key) { this.s3Key = s3Key; }
//    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
//}



package com.lms.chat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notebook_studio_outputs")
public class NotebookStudioOutput {

    public enum StudioType { REPORT, DATATABLE, MINDMAP, FLASHCARDS, QUIZ, AUDIO, SLIDES, VIDEO, INFOGRAPHIC }

    public enum Status { PENDING, READY, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notebook_id", nullable = false)
    private Notebook notebook;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private StudioType type;

    @Column(name = "language")
    private String language;

    // Plain text/markdown for report & datatable; JSON string for
    // mindmap/flashcards/quiz/slides; the podcast script (for reference) for audio;
    // the AI-generated image-description prompt (for reference) for infographic.
    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent;

    // S3 key for generated media (audio/slides/video/infographic); null for text-only types.
    @Column(name = "s3_key")
    private String s3Key;

    // Lifecycle of the (now async) generation: PENDING while the background job
    // is running, READY once textContent/s3Key are populated, FAILED if the
    // background job threw or the thread pool rejected the task.
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    // Human-readable, safe-to-display failure reason (never a raw stack trace).
    // Only ever set when status == FAILED.
    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ===== Getters =====
    public Long getId() { return id; }
    public Notebook getNotebook() { return notebook; }
    public StudioType getType() { return type; }
    public String getLanguage() { return language; }
    public String getTextContent() { return textContent; }
    public String getS3Key() { return s3Key; }
    public Status getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // ===== Setters =====
    public void setId(Long id) { this.id = id; }
    public void setNotebook(Notebook notebook) { this.notebook = notebook; }
    public void setType(StudioType type) { this.type = type; }
    public void setLanguage(String language) { this.language = language; }
    public void setTextContent(String textContent) { this.textContent = textContent; }
    public void setS3Key(String s3Key) { this.s3Key = s3Key; }
    public void setStatus(Status status) { this.status = status; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}