package com.lms.assessment.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "study_plans")
public class StudyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "trainer_email", nullable = false)
    private String trainerEmail;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "thumbnail_color")
    private String thumbnailColor;   // e.g. "#6366f1"

    @Column(name = "icon")
    private String icon;             // emoji or icon string e.g. "📘"

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "studyPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private List<StudyPlanSection> sections = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ── Getters & Setters ──────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getThumbnailColor() { return thumbnailColor; }
    public void setThumbnailColor(String thumbnailColor) { this.thumbnailColor = thumbnailColor; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<StudyPlanSection> getSections() { return sections; }
    public void setSections(List<StudyPlanSection> sections) { this.sections = sections; }
}