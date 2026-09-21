package com.lms.progress.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_plan_cache")
public class UserPlanCache {

    @Id
    private String email;

    private String plan;

    @Column(name = "organization_id")
    private String organizationId; // nullable

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getEmail()          { return email; }
    public void setEmail(String email){ this.email = email; }

    public String getPlan()           { return plan; }
    public void setPlan(String plan)  { this.plan = plan; }

    public String getOrganizationId()           { return organizationId; }
    public void setOrganizationId(String orgId) { this.organizationId = orgId; }

    public LocalDateTime getUpdatedAt()             { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}