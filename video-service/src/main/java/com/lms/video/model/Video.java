//
//
//
//
//
//
//package com.lms.video.model;
//
//import jakarta.persistence.*;
//import java.time.Instant;
//
//@Entity
//@Table(name = "videos")
//public class Video {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "batch_id", nullable = true) // Set to true first
//    private Long batchId;
//    
//    // ✅ NEW FIELDS
//    @Column(nullable = false)
//    private String title;
//
//    @Column(length = 1000)
//    private String description;
//
//    private String originalFileName;
//
//    private String storedFileName;
//
//    private String videoUrl;
//
//    private long size;
//
//    private String uploadedBy;
//
//    
//    @Column(name = "uploaded_at")
//    private Instant uploadedAt = Instant.now();
//
//    public Video() {
//    }
//
//    // ---------- Getters & Setters ----------
//   public String UploadedBy()
//   {
//	   return uploadedBy;
//   }
//   public void setUploadedBy(String uploadedBy) {
//	    this.uploadedBy = uploadedBy;
//	}
//    
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public Long getBatchId() {
//        return batchId;
//    }
//
//    public void setBatchId(Long batchId) {
//        this.batchId = batchId;
//    }
//
//    
//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public String getOriginalFileName() {
//        return originalFileName;
//    }
//
//    public void setOriginalFileName(String originalFileName) {
//        this.originalFileName = originalFileName;
//    }
//
//    public String getStoredFileName() {
//        return storedFileName;
//    }
//
//    public void setStoredFileName(String storedFileName) {
//        this.storedFileName = storedFileName;
//    }
//
//    public String getVideoUrl() {
//        return videoUrl;
//    }
//
//    public void setVideoUrl(String videoUrl) {
//        this.videoUrl = videoUrl;
//    }
//
//    public long getSize() {
//        return size;
//    }
//
//    public void setSize(long size) {
//        this.size = size;
//    }
//
//    public Instant getUploadedAt() {
//        return uploadedAt;
//    }
//
//    public void setUploadedAt(Instant uploadedAt) {
//        this.uploadedAt = uploadedAt;
//    }
//}

package com.lms.video.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "videos")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = true)
    private Long batchId;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    private String originalFileName;
    private String storedFileName;
    private String videoUrl;
    private long size;
    private String uploadedBy;

    @Column(name = "uploaded_at")
    private Instant uploadedAt = Instant.now();

    // ─── NEW FIELDS ───────────────────────────────────────────
    @Column(length = 500)
    private String tags;           // comma-separated: "react,hooks,state"

    @Column(length = 100)
    private String category;       // "Programming", "Education", etc.

    @Column(length = 50)
    private String language;       // "English", "Hindi", etc.

    @Column(length = 20)
    private String visibility;     // "public" | "unlisted" | "private"

    @Column(length = 20)
    private String audience;       // "kids" | "not-kids"

//    @Column(name = "age_restrict")
//    private boolean ageRestrict;   // 18+ flag
 // ✅ AFTER — wrapper, handles null from old rows gracefully
    @Column(name = "age_restrict", columnDefinition = "boolean default false")
    private Boolean ageRestrict;

    @Column(length = 200)
    private String course;         // course/playlist name
    // ─────────────────────────────────────────────────────────

    public Video() {}

    // ── Existing getters/setters (unchanged) ──────────────────
    public String uploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
    public String getStoredFileName() { return storedFileName; }
    public void setStoredFileName(String storedFileName) { this.storedFileName = storedFileName; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }

    // ── New getters/setters ────────────────────────────────────
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }

//    public boolean isAgeRestrict() { return ageRestrict; }
 // Update getter/setter too:
    public Boolean isAgeRestrict() { return ageRestrict != null ? ageRestrict : false; }
//    public void setAgeRestrict(boolean ageRestrict) { this.ageRestrict = ageRestrict; }
    public void setAgeRestrict(Boolean ageRestrict) { this.ageRestrict = ageRestrict != null ? ageRestrict : false; }
    
    

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
}