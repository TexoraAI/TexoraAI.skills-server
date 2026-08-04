package com.lms.live_session.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// NOTE: using jakarta.persistence (Spring Boot 3). If you're on Spring Boot 2.x,
// change all "jakarta.persistence" imports to "javax.persistence" below.

@Entity
@Table(name = "whiteboard_snapshots")
public class WhiteboardSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true)
    private Long sessionId;

    @Lob
    @Column(name = "elements", columnDefinition = "TEXT")
    private String elements;

    @Lob
    @Column(name = "app_state", columnDefinition = "TEXT")
    private String appState;

    @Lob
    @Column(name = "files", columnDefinition = "TEXT")
    private String files;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public WhiteboardSnapshot() {}

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getElements() { return elements; }
    public void setElements(String elements) { this.elements = elements; }

    public String getAppState() { return appState; }
    public void setAppState(String appState) { this.appState = appState; }

    public String getFiles() { return files; }
    public void setFiles(String files) { this.files = files; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}