package com.lms.course.model;

import jakarta.persistence.*;
import java.time.Instant;
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String description;

    private String category;

    @Column(nullable = false)
    private String ownerEmail; // 🔐 linked to JWT

    private Long batchId;
    
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public Course() {}

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

    public Long getBatchId() { 
        return batchId; 
    }

    public void setBatchId(Long batchId) { 
        this.batchId = batchId; 
    }
    
    
    public Instant getCreatedAt() { return createdAt; }
}
