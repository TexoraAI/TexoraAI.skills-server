//package com.lms.video.dto;
//
//public class WatchNowDTO {
//
//    private Long courseId;
//    private String title;
//
//    // ── Instructor ──
//    private String instructorName;
//    private String instructorRole;
//    private String experience;
//    private String studentCount;
//
//    // ── Description ──
//    private String description;
//    private String learnPoints;   // JSON string: [{"title":"...","desc":"..."}]
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
//    private String status;            // "published" | "draft"
//    private boolean showMoreEnabled;
//
//    // ── Optional URL fallback ──
//    private String videoUrl;
//
//    // ===== GETTERS & SETTERS =====
//
//    public Long getCourseId() { return courseId; }
//    public void setCourseId(Long courseId) { this.courseId = courseId; }
//
//    public String getTitle() { return title; }
//    public void setTitle(String title) { this.title = title; }
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

package com.lms.video.dto;

public class WatchNowDTO {

    private String quote;
    private String externalVideoUrl;
    private String personName;
    private String personRole;
    private String status;
    private int sortOrder;

    // ===== GETTERS & SETTERS =====

    public String getQuote() { return quote; }
    public void setQuote(String quote) { this.quote = quote; }

    public String getExternalVideoUrl() { return externalVideoUrl; }
    public void setExternalVideoUrl(String externalVideoUrl) { this.externalVideoUrl = externalVideoUrl; }

    public String getPersonName() { return personName; }
    public void setPersonName(String personName) { this.personName = personName; }

    public String getPersonRole() { return personRole; }
    public void setPersonRole(String personRole) { this.personRole = personRole; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}