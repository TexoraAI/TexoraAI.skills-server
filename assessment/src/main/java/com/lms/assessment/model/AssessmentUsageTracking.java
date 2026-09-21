package com.lms.assessment.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

// WHY: ONE table backs all 8 action types via the "action" column — do not create
// a separate table per feature.
@Entity
@Table(name = "assessment_usage_tracking",
       uniqueConstraints = { @UniqueConstraint(columnNames = {"email", "period", "action"}) })
public class AssessmentUsageTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(length = 7)
    private String period; // "YYYY-MM"

    private String action; // AssessmentUsageLimits.Action enum name

    private Integer usageCount = 0;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId()                { return id; }
    public void setId(Long id)         { this.id = id; }

    public String getEmail()           { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPeriod()            { return period; }
    public void setPeriod(String period) { this.period = period; }

    public String getAction()            { return action; }
    public void setAction(String action) { this.action = action; }

    public Integer getUsageCount()               { return usageCount; }
    public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }

    public LocalDateTime getUpdatedAt()               { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}