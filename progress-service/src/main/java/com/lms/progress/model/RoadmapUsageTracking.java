package com.lms.progress.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "roadmap_usage_tracking",
       uniqueConstraints = { @UniqueConstraint(columnNames = {"owner_id", "period"}) })
public class RoadmapUsageTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(length = 7)
    private String period; // "YYYY-MM"

    @Column(name = "roadmap_count")
    private Integer roadmapCount = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId()               { return id; }

    public Long getOwnerId()          { return ownerId; }
    public void setOwnerId(Long id)   { this.ownerId = id; }

    public String getPeriod()             { return period; }
    public void setPeriod(String period)  { this.period = period; }

    public Integer getRoadmapCount()             { return roadmapCount; }
    public void setRoadmapCount(Integer count)   { this.roadmapCount = count; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}