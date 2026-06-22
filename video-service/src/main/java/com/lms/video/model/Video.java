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
//    @Column(name = "batch_id", nullable = true)
//    private Long batchId;
//
//    @Column(nullable = false)
//    private String title;
//
//    @Column(length = 1000)
//    private String description;
//
//    private String originalFileName;
//    private String storedFileName;
//    private String videoUrl;
//    private long size;
//    private String uploadedBy;
//
//    @Column(name = "uploaded_at")
//    private Instant uploadedAt = Instant.now();
//
//    // ─── NEW FIELDS ───────────────────────────────────────────
//    @Column(length = 500)
//    private String tags;           // comma-separated: "react,hooks,state"
//
//    @Column(length = 100)
//    private String category;       // "Programming", "Education", etc.
//
//    @Column(length = 50)
//    private String language;       // "English", "Hindi", etc.
//
//    @Column(length = 20)
//    private String visibility;     // "public" | "unlisted" | "private"
//
//    @Column(length = 20)
//    private String audience;       // "kids" | "not-kids"
//
////    @Column(name = "age_restrict")
////    private boolean ageRestrict;   // 18+ flag
// // ✅ AFTER — wrapper, handles null from old rows gracefully
//    @Column(name = "age_restrict", columnDefinition = "boolean default false")
//    private Boolean ageRestrict;
//
//    @Column(length = 200)
//    private String course;         // course/playlist name
//    
//    @Column(length = 20)
//    private String status;   // "draft" | "published"
//
//    @Column(name = "organization_id")
//    private String organizationId;
//    // ─────────────────────────────────────────────────────────
//
//    public Video() {}
//
//    // ── Existing getters/setters (unchanged) ──────────────────
//    public String uploadedBy() { return uploadedBy; }
//    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//    public Long getBatchId() { return batchId; }
//    public void setBatchId(Long batchId) { this.batchId = batchId; }
//    public String getTitle() { return title; }
//    public void setTitle(String title) { this.title = title; }
//    public String getDescription() { return description; }
//    public void setDescription(String description) { this.description = description; }
//    public String getOriginalFileName() { return originalFileName; }
//    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
//    public String getStoredFileName() { return storedFileName; }
//    public void setStoredFileName(String storedFileName) { this.storedFileName = storedFileName; }
//    public String getVideoUrl() { return videoUrl; }
//    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
//    public long getSize() { return size; }
//    public void setSize(long size) { this.size = size; }
//    public Instant getUploadedAt() { return uploadedAt; }
//    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
//
//    // ── New getters/setters ────────────────────────────────────
//    public String getTags() { return tags; }
//    public void setTags(String tags) { this.tags = tags; }
//
//    public String getCategory() { return category; }
//    public void setCategory(String category) { this.category = category; }
//
//    public String getLanguage() { return language; }
//    public void setLanguage(String language) { this.language = language; }
//
//    public String getVisibility() { return visibility; }
//    public void setVisibility(String visibility) { this.visibility = visibility; }
//
//    public String getAudience() { return audience; }
//    public void setAudience(String audience) { this.audience = audience; }
//
////    public boolean isAgeRestrict() { return ageRestrict; }
// // Update getter/setter too:
//    public Boolean isAgeRestrict() { return ageRestrict != null ? ageRestrict : false; }
////    public void setAgeRestrict(boolean ageRestrict) { this.ageRestrict = ageRestrict; }
//    public void setAgeRestrict(Boolean ageRestrict) { this.ageRestrict = ageRestrict != null ? ageRestrict : false; }
//    
//    
//
//    public String getCourse() { return course; }
//    public void setCourse(String course) { this.course = course; }
//    
//    public String getStatus() { return status; }
//    public void setStatus(String status) { this.status = status; }
//    
//    
//
//    public String getOrganizationId() { return organizationId; }
//    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
//}




package com.lms.video.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "videos", indexes = {
        @Index(name = "idx_videos_org_id", columnList = "organization_id"),
        @Index(name = "idx_videos_org_batch_status", columnList = "organization_id, batch_id, status")
})
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

    // ✅ NEW — tenant boundary. null = belongs to a non-org user (Super Admin,
    // Google login, self-registered) and is therefore unrestricted.
    @Column(name = "organization_id", length = 64)
    private String organizationId;

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

    @Column(length = 20)
    private String status;   // "draft" | "published"


    // ─────────────────────────────────────────────────────────

    public Video() {}

    // ── Existing getters/setters (unchanged) ──────────────────
    public String uploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    // ✅ NEW — standard JavaBean getter. Jackson only serializes properties
    // it recognizes as getXxx()/isXxx(); the existing uploadedBy() method
    // above doesn't match that pattern, so the field was being silently
    // omitted from every JSON response. This fixes that without touching
    // any of the existing video.uploadedBy() call sites in VideoService.
    public String getUploadedBy() { return uploadedBy; }
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

    // ✅ NEW — getter/setter for organizationId.
    // Set ONCE at upload time from the caller's JWT. Never expose this as an
    // editable request parameter — that would let a caller spoof org assignment.
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // ✅ NEW — computed, not persisted. Lets the admin video list distinguish
    // an uploaded file from a YouTube/Vimeo/direct URL video without a
    // migration. @Transient tells JPA to ignore it; Jackson still serializes
    // it in the JSON response because it's a normal public getter.
    @Transient
    public String getVideoType() {
        if (videoUrl != null && !videoUrl.isBlank()) {
            String url = videoUrl.toLowerCase();
            if (url.contains("youtube.com") || url.contains("youtu.be")) {
                return "YOUTUBE";
            }
            if (url.contains("vimeo.com")) {
                return "VIMEO";
            }
            return "DIRECT_URL";
        }
        return "UPLOADED_FILE";
    }
}