package com.lms.course.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "content_items")
public class ContentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long courseId;
    private String title;

    @Column(length = 1000)
    private String description;

    private String contentType;
    private String url;

    private Integer durationSeconds;
    private Integer orderIndex;

    @Column(nullable = false)
    private String ownerEmail; // 🔐 JWT owner

    private Instant createdAt = Instant.now();

    public ContentItem() {}

    // =========================
    // GETTERS & SETTERS
    // =========================

    public Long getId() {
        return id;
    }
//kjdfnjfnf
    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
