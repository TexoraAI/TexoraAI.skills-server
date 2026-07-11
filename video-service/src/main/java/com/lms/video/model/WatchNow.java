//package com.lms.video.model;
//
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "watch_now")
//public class WatchNow {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private Long courseId;
//
//    private String title;
//
//    // stored filename (e.g. "1720000000000_intro.mp4")
//    private String fileName;
//
//    // absolute path on disk – internal use only, never exposed in API response
//    private String filePath;
//
//    // thumbnail filename (stored in same VIDEO_DIR)
//    private String thumbnail;
//
//    // ── Instructor card ──
//    private String instructorName;
//    private String instructorRole;
//    private String experience;
//    private String studentCount;
//
//    // ── Description section ──
//    @Column(columnDefinition = "TEXT")
//    private String description;
//
//    // ── What you'll learn – stored as JSON string ──
//    @Column(columnDefinition = "TEXT")
//    private String learnPoints;
//
//    // ── Meta ──
//    private String publishDate;
//    private String learnersCount;
//    private boolean showInstructorLive;
//
//    // ── Top section ──
//    private String platformName;
//    private String featuredTag;
//    private String hostedBy;
//    private String status;           // "published" | "draft"
//    private boolean showMoreEnabled;
//
//    // ── Optional direct URL (fallback when no file uploaded) ──
//    private String videoUrl;
//
//    // ===== GETTERS & SETTERS =====
//
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public Long getCourseId() { return courseId; }
//    public void setCourseId(Long courseId) { this.courseId = courseId; }
//
//    public String getTitle() { return title; }
//    public void setTitle(String title) { this.title = title; }
//
//    public String getFileName() { return fileName; }
//    public void setFileName(String fileName) { this.fileName = fileName; }
//
//    public String getFilePath() { return filePath; }
//    public void setFilePath(String filePath) { this.filePath = filePath; }
//
//    public String getThumbnail() { return thumbnail; }
//    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
//
//    public String getInstructorName() { return instructorName; }
//    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
//
//    public String getInstructorRole() { return instructorRole; }
//    public void setInstructorRole(String instructorRole) { this.instructorRole = instructorRole; }
//
//    public String getExperience() { return experience; }
//    public void setExperience(String experience) { this.experience = experience; }
//
//    public String getStudentCount() { return studentCount; }
//    public void setStudentCount(String studentCount) { this.studentCount = studentCount; }
//
//    public String getDescription() { return description; }
//    public void setDescription(String description) { this.description = description; }
//
//    public String getLearnPoints() { return learnPoints; }
//    public void setLearnPoints(String learnPoints) { this.learnPoints = learnPoints; }
//
//    public String getPublishDate() { return publishDate; }
//    public void setPublishDate(String publishDate) { this.publishDate = publishDate; }
//
//    public String getLearnersCount() { return learnersCount; }
//    public void setLearnersCount(String learnersCount) { this.learnersCount = learnersCount; }
//
//    public boolean isShowInstructorLive() { return showInstructorLive; }
//    public void setShowInstructorLive(boolean showInstructorLive) { this.showInstructorLive = showInstructorLive; }
//
//    public String getPlatformName() { return platformName; }
//    public void setPlatformName(String platformName) { this.platformName = platformName; }
//
//    public String getFeaturedTag() { return featuredTag; }
//    public void setFeaturedTag(String featuredTag) { this.featuredTag = featuredTag; }
//
//    public String getHostedBy() { return hostedBy; }
//    public void setHostedBy(String hostedBy) { this.hostedBy = hostedBy; }
//
//    public String getStatus() { return status; }
//    public void setStatus(String status) { this.status = status; }
//
//    public boolean isShowMoreEnabled() { return showMoreEnabled; }
//    public void setShowMoreEnabled(boolean showMoreEnabled) { this.showMoreEnabled = showMoreEnabled; }
//
//    public String getVideoUrl() { return videoUrl; }
//    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
//}

package com.lms.video.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "watch_now")
public class WatchNow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String quote;

    // set only if a video FILE was uploaded
    private String videoFileName;
    private String videoFilePath;

    // set only if a YouTube/Vimeo/direct URL was pasted instead of uploading a file
    private String externalVideoUrl;

    // shown before play is clicked
    @Column(nullable = false)
    private String thumbnail;

    @Column(nullable = false)
    private String personName;

    @Column(nullable = false)
    private String personRole;

    // "draft" | "published"
    private String status = "draft";

    private int sortOrder = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===== GETTERS & SETTERS =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuote() { return quote; }
    public void setQuote(String quote) { this.quote = quote; }

    public String getVideoFileName() { return videoFileName; }
    public void setVideoFileName(String videoFileName) { this.videoFileName = videoFileName; }

    public String getVideoFilePath() { return videoFilePath; }
    public void setVideoFilePath(String videoFilePath) { this.videoFilePath = videoFilePath; }

    public String getExternalVideoUrl() { return externalVideoUrl; }
    public void setExternalVideoUrl(String externalVideoUrl) { this.externalVideoUrl = externalVideoUrl; }

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    public String getPersonName() { return personName; }
    public void setPersonName(String personName) { this.personName = personName; }

    public String getPersonRole() { return personRole; }
    public void setPersonRole(String personRole) { this.personRole = personRole; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}