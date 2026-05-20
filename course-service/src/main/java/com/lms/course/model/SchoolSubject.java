package com.lms.course.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "school_subject")
public class SchoolSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_class_id", nullable = false)
    private SchoolClass schoolClass;

    @Column(name = "name", nullable = false)
    private String name;

    // values: "all", "Science", "Commerce", "Arts"
    // use "all" when subject belongs to all streams
    @Column(name = "stream", nullable = false)
    private String stream;

    // Lucide icon key — frontend maps this to icon component
    // examples: "Calculator", "Atom", "Brain", "FlaskConical", "Globe", "BookOpen"
    @Column(name = "icon_key")
    private String iconKey;

    // JSON array of chapter name strings
    // example: ["Number Systems", "Polynomials", "Coordinate Geometry"]
    @Column(name = "chapters_json", columnDefinition = "TEXT")
    private String chaptersJson;

    // JSON array of unit objects with topics
    // example: [{"unit":"Unit 1","topics":["Topic A","Topic B"]}]
    @Column(name = "syllabus_json", columnDefinition = "TEXT")
    private String syllabusJson;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public SchoolSubject() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SchoolClass getSchoolClass() { return schoolClass; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStream() { return stream; }
    public void setStream(String stream) { this.stream = stream; }

    public String getIconKey() { return iconKey; }
    public void setIconKey(String iconKey) { this.iconKey = iconKey; }

    public String getChaptersJson() { return chaptersJson; }
    public void setChaptersJson(String chaptersJson) { this.chaptersJson = chaptersJson; }

    public String getSyllabusJson() { return syllabusJson; }
    public void setSyllabusJson(String syllabusJson) { this.syllabusJson = syllabusJson; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}