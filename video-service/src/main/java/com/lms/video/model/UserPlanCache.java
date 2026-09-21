package com.lms.video.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// WHY: Local read cache of individual/standalone user plan, kept in sync via Kafka
// auth-events (USER_PLAN_UPDATED). organizationId is nullable here — null means
// the user is on the standalone/individual plan path, not an org-driven plan.
@Entity
@Table(name = "user_plan_cache")
public class UserPlanCache {

    @Id
    private String email;

    private String plan;

    private String organizationId; // nullable — null means standalone

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getEmail()          { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPlan()          { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public String getOrganizationId()           { return organizationId; }
    public void setOrganizationId(String orgId) { this.organizationId = orgId; }

    public LocalDateTime getUpdatedAt()               { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}