package com.lms.live_session.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

/**
 * Per-user, per-period, per-action usage counter backing tier limit checks
 * (e.g. classes created this month, AI Companion uses this month).
 */
@Entity
@Table(
        name = "live_session_usage_tracking",
        uniqueConstraints = { @UniqueConstraint(columnNames = { "email", "period", "action" }) }
)
public class LiveSessionUsageTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    /** Format: "YYYY-MM" */
    private String period;

    /** "CLASS_CREATE" | "AI_COMPANION_USE" */
    private String action;

    private Integer usageCount = 0;

    private LocalDateTime updatedAt;

    public LiveSessionUsageTracking() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
