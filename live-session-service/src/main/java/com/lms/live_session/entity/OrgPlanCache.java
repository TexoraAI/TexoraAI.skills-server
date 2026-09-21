package com.lms.live_session.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Local read cache of org-level plan, populated from auth-events (ORG_UPDATED).
 * NOTE: organizationId is stored as String here to match the auth-events wire
 * format exactly (UUID text). This service's domain entities (LiveSession,
 * Recording, LiveSessionAccessValidator) use Long organizationId — callers
 * must convert explicitly (Long.toString()) at the resolver boundary.
 */
@Entity
@Table(name = "org_plan_cache")
public class OrgPlanCache {

    @Id
    private String organizationId;

    private String plan;

    private LocalDateTime updatedAt;

    public OrgPlanCache() {
    }

    public OrgPlanCache(String organizationId, String plan, LocalDateTime updatedAt) {
        this.organizationId = organizationId;
        this.plan = plan;
        this.updatedAt = updatedAt;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
