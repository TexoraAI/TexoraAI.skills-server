package com.lms.live_session.entity;
 
import jakarta.persistence.*;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "ai_activity_logs")
public class AiActivityLog {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(name = "user_email", nullable = false)
    private String userEmail;
 
    @Column(name = "session_id")
    private Long sessionId;
 
    @Column(name = "session_title")
    private String sessionTitle;
 
    @Column(name = "mode", nullable = false)
    private String mode;
 
    @Column(name = "sources_used")
    private String sourcesUsed;   // comma-separated
 
    @Column(name = "response_preview", length = 500)
    private String responsePreview;
 
    // SUCCESS | FAILED
    @Column(name = "status", nullable = false)
    private String status = "SUCCESS";
 
    @Column(name = "conversation_id")
    private Long conversationId;
 
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
 
    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
 
    // Getters & Setters
    public Long getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getSessionTitle() { return sessionTitle; }
    public void setSessionTitle(String sessionTitle) { this.sessionTitle = sessionTitle; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getSourcesUsed() { return sourcesUsed; }
    public void setSourcesUsed(String sourcesUsed) { this.sourcesUsed = sourcesUsed; }
    public String getResponsePreview() { return responsePreview; }
    public void setResponsePreview(String responsePreview) { this.responsePreview = responsePreview; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}