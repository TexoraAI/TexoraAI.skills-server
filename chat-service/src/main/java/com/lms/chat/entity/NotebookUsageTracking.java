package com.lms.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "notebook_usage_tracking",
        uniqueConstraints = { @UniqueConstraint(columnNames = {"student_email", "period"}) })
public class NotebookUsageTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_email", nullable = false)
    private String studentEmail;

    @Column(name = "period", length = 7)
    private String period;

    @Column(name = "action_count")
    private Integer actionCount = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId()                       { return id; }
    public void setId(Long id)                { this.id = id; }

    public String getStudentEmail()                     { return studentEmail; }
    public void setStudentEmail(String studentEmail)     { this.studentEmail = studentEmail; }

    public String getPeriod()                 { return period; }
    public void setPeriod(String period)      { this.period = period; }

    public Integer getActionCount()                     { return actionCount; }
    public void setActionCount(Integer actionCount)     { this.actionCount = actionCount; }

    public LocalDateTime getUpdatedAt()                 { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)   { this.updatedAt = updatedAt; }
}