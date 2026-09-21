package com.lms.video.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// WHY: Local read cache of org-level plan, kept in sync via Kafka auth-events (ORG_UPDATED)
// video-service has no User entity, so this is the org-plan source of truth locally.
@Entity
@Table(name = "org_plan_cache")
public class OrgPlanCache {

    @Id
    private String organizationId;

    private String plan;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getOrganizationId()            { return organizationId; }
    public void setOrganizationId(String orgId)  { this.organizationId = orgId; }

    public String getPlan()          { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public LocalDateTime getUpdatedAt()               { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}