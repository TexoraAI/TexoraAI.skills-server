package com.lms.live_session.entity;
 
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "session_action_items")
public class SessionActionItem {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(name = "session_id", nullable = false)
    private Long sessionId;
 
    @Column(name = "title", nullable = false)
    private String title;
 
    @Column(name = "description", length = 2000)
    private String description;
 
    // TRAINER | STUDENT | ALL
    @Column(name = "assigned_to")
    private String assignedTo;
 
    // PENDING | IN_PROGRESS | DONE
    @Column(name = "status", nullable = false)
    private String status = "PENDING";
 
    @Column(name = "due_date")
    private LocalDate dueDate;
 
    @Column(name = "created_by", nullable = false)
    private String createdBy;
 
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
 
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
 
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
 
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
 
    // Getters & Setters
    public Long getId() { return id; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}