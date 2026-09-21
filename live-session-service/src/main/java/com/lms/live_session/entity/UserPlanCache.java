package com.lms.live_session.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Local read cache of individual user plan, populated from auth-events
 * (USER_PLAN_UPDATED). Used as a fallback when the caller has no
 * organizationId (individual / non-org users).
 */
@Entity
@Table(name = "user_plan_cache")
public class UserPlanCache {

    @Id
    private String email;

    private String plan;

    /** Nullable — organizationId of the user at the time of the event, informational only. */
    private String organizationId;

    private LocalDateTime updatedAt;

    public UserPlanCache() {
    }

    public UserPlanCache(String email, String plan, String organizationId, LocalDateTime updatedAt) {
        this.email = email;
        this.plan = plan;
        this.organizationId = organizationId;
        this.updatedAt = updatedAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
