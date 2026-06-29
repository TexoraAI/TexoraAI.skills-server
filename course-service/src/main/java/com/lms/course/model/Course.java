package com.lms.course.model;

import jakarta.persistence.*;
import java.time.Instant;

// OPTIMIZATION: Added @Index annotations for all columns used in WHERE/ORDER BY:
//   ownerEmail  — findByOwnerEmail (trainer's course list, HIGH TRAFFIC)
//   batchId     — findByBatchId, findByBatchIdIn (student course list, HIGH TRAFFIC)
//   organization_id — findByOrganizationId (org admin queries)
//   batchId+ownerEmail — findByBatchIdAndOwnerEmail (composite saves second index scan)
//   created_at  — findAllByOrderByCreatedAtDesc (admin listing)
@Entity
@Table(name = "courses", indexes = {
    @Index(name = "idx_course_owner_email", columnList = "ownerEmail"),
    @Index(name = "idx_course_batch_id",    columnList = "batchId"),
    @Index(name = "idx_course_org_id",      columnList = "organization_id"),
    @Index(name = "idx_course_batch_owner", columnList = "batchId, ownerEmail"),
    @Index(name = "idx_course_created_at",  columnList = "created_at"),
    @Index(name = "idx_course_assigned_trainer", columnList = "assigned_trainer_email")
})
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String description;

    private String category;

    @Column(nullable = false)
    private String ownerEmail;

    private Long batchId;

    @Column(name = "organization_id")
    private String organizationId;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
    
    @Column(name = "assigned_trainer_email") 
    private String assignedTrainerEmail;

    public Course() {}

    public Long getId()                            { return id; }
    public void setId(Long id)                     { this.id = id; }

    public String getTitle()                       { return title; }
    public void setTitle(String title)             { this.title = title; }

    public String getDescription()                 { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory()                    { return category; }
    public void setCategory(String category)       { this.category = category; }

    public String getOwnerEmail()                  { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail)   { this.ownerEmail = ownerEmail; }

    public Long getBatchId()                       { return batchId; }
    public void setBatchId(Long batchId)           { this.batchId = batchId; }

    public String getOrganizationId()              { return organizationId; }
    public void setOrganizationId(String orgId)    { this.organizationId = orgId; }

    public Instant getCreatedAt()                  { return createdAt; }
    
    public String getAssignedTrainerEmail() {
        return assignedTrainerEmail;
    }

    public void setAssignedTrainerEmail(String assignedTrainerEmail) {
        this.assignedTrainerEmail = assignedTrainerEmail;
    }
}