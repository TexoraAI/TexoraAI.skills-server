package com.lms.course.model;

import jakarta.persistence.*;
import java.time.Instant;

// OPTIMIZATION: Added @Index annotations on courseId (primary lookup column) and
// composite courseId+ownerEmail (used in findByCourseIdAndOwnerEmail).
// Without these, every content list request does a full table scan.
@Entity
@Table(name = "content_items", indexes = {
    @Index(name = "idx_ci_course_id",    columnList = "courseId"),
    @Index(name = "idx_ci_course_owner", columnList = "courseId, ownerEmail")
})
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
    private String ownerEmail;

    private Instant createdAt = Instant.now();

    public ContentItem() {}

    public Long getId()                                        { return id; }

    public Long getCourseId()                                  { return courseId; }
    public void setCourseId(Long courseId)                     { this.courseId = courseId; }

    public String getOwnerEmail()                              { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail)               { this.ownerEmail = ownerEmail; }

    public String getTitle()                                   { return title; }
    public void setTitle(String title)                         { this.title = title; }

    public String getDescription()                             { return description; }
    public void setDescription(String description)             { this.description = description; }

    public String getContentType()                             { return contentType; }
    public void setContentType(String contentType)             { this.contentType = contentType; }

    public String getUrl()                                     { return url; }
    public void setUrl(String url)                             { this.url = url; }

    public Integer getDurationSeconds()                        { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds)    { this.durationSeconds = durationSeconds; }

    public Integer getOrderIndex()                             { return orderIndex; }
    public void setOrderIndex(Integer orderIndex)              { this.orderIndex = orderIndex; }

    public Instant getCreatedAt()                              { return createdAt; }
}