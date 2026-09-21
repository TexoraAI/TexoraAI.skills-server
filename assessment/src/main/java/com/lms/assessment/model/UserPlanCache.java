package com.lms.assessment.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_plan_cache")
public class UserPlanCache {

    @Id
    private String email;

    private String plan;

    private String organizationId;

    private LocalDateTime updatedAt;

    public String getEmail()               { return email; }
    public void setEmail(String email)     { this.email = email; }

    public String getPlan()                { return plan; }
    public void setPlan(String plan)       { this.plan = plan; }

    public String getOrganizationId()               { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public LocalDateTime getUpdatedAt()              { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}