package com.lms.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "resume_usage_tracking",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "period"})
    },
    indexes = {
        @Index(name = "idx_usage_user_period", columnList = "user_id, period")
    })
public class ResumeUsageTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Format: "YYYY-MM", e.g. "2026-09" — one row per user per calendar month
    @Column(name = "period", nullable = false, length = 7)
    private String period;

    @Column(name = "ai_generation_count", nullable = false)
    private Integer aiGenerationCount = 0;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void touch() {
        this.updatedAt = java.time.LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Integer getAiGenerationCount() {
        return aiGenerationCount;
    }

    public void setAiGenerationCount(Integer aiGenerationCount) {
        this.aiGenerationCount = aiGenerationCount;
    }

    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}