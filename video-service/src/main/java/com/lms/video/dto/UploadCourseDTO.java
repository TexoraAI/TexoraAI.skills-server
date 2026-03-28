package com.lms.video.dto;

public class UploadCourseDTO {

    private Long courseId;
    private String title;

    private String instructorName;
    private String instructorRole;
    private String experience;
    private String studentCount;

    private String description;
    private String learnPoints;

    private String publishDate;
    private String learnersCount;

    private boolean showInstructorLive;

    // ===== GETTERS & SETTERS =====

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    public String getInstructorRole() { return instructorRole; }
    public void setInstructorRole(String instructorRole) { this.instructorRole = instructorRole; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getStudentCount() { return studentCount; }
    public void setStudentCount(String studentCount) { this.studentCount = studentCount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLearnPoints() { return learnPoints; }
    public void setLearnPoints(String learnPoints) { this.learnPoints = learnPoints; }

    public String getPublishDate() { return publishDate; }
    public void setPublishDate(String publishDate) { this.publishDate = publishDate; }

    public String getLearnersCount() { return learnersCount; }
    public void setLearnersCount(String learnersCount) { this.learnersCount = learnersCount; }

    public boolean isShowInstructorLive() { return showInstructorLive; }
    public void setShowInstructorLive(boolean showInstructorLive) { this.showInstructorLive = showInstructorLive; }
}