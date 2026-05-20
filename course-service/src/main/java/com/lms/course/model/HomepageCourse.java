package com.lms.course.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "homepage_courses")
public class HomepageCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String instructor;
    private String duration;
    private String students;
    private Double rating;
    private String level;

    @Column(columnDefinition = "TEXT")
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String description;

    private BigDecimal price;
    private String category;
    private Integer liveSessions;
    private Integer totalLessons;
    private Integer projects;
    private Boolean featured;
    private Boolean active;
    private String thumbnailUrl;

    @Column(columnDefinition = "TEXT")
    private String learningPointsJson;

    @Column(columnDefinition = "TEXT")
    private String modulesJson;

    @Column(columnDefinition = "TEXT")
    private String highlightsJson;

    @Column(columnDefinition = "TEXT")
    private String syllabusJson;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Constructors ---

    public HomepageCourse() {}

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getStudents() { return students; }
    public void setStudents(String students) { this.students = students; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getLiveSessions() { return liveSessions; }
    public void setLiveSessions(Integer liveSessions) { this.liveSessions = liveSessions; }

    public Integer getTotalLessons() { return totalLessons; }
    public void setTotalLessons(Integer totalLessons) { this.totalLessons = totalLessons; }

    public Integer getProjects() { return projects; }
    public void setProjects(Integer projects) { this.projects = projects; }

    public Boolean getFeatured() { return featured; }
    public void setFeatured(Boolean featured) { this.featured = featured; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getLearningPointsJson() { return learningPointsJson; }
    public void setLearningPointsJson(String learningPointsJson) { this.learningPointsJson = learningPointsJson; }

    public String getModulesJson() { return modulesJson; }
    public void setModulesJson(String modulesJson) { this.modulesJson = modulesJson; }

    public String getHighlightsJson() { return highlightsJson; }
    public void setHighlightsJson(String highlightsJson) { this.highlightsJson = highlightsJson; }

    public String getSyllabusJson() { return syllabusJson; }
    public void setSyllabusJson(String syllabusJson) { this.syllabusJson = syllabusJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}