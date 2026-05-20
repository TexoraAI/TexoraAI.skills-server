package com.lms.live_session.entity;
 
import jakarta.persistence.*;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "ai_messages")
public class AiMessage {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;
 
    // USER | ASSISTANT
    @Column(name = "role", nullable = false)
    private String role;
 
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
 
    @Column(name = "mode")
    private String mode;
 
    // Comma-separated list of sources used e.g. "MEETINGS,CHAT"
    @Column(name = "sources_used")
    private String sourcesUsed;
 
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
 
    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
 
    // Getters & Setters
    public Long getId() { return id; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getSourcesUsed() { return sourcesUsed; }
    public void setSourcesUsed(String sourcesUsed) { this.sourcesUsed = sourcesUsed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}