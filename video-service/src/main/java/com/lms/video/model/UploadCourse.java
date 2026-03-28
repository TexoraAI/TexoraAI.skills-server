package com.lms.video.model;

import jakarta.persistence.*;

@Entity
@Table(name = "upload_courses")
public class UploadCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long courseId;

    private String title;

    private String fileName;
    private String filePath;

    private String thumbnail;

    private String instructorName;
    private String instructorRole;
    private String experience;
    private String studentCount;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String learnPoints;

    private String publishDate;
    private String learnersCount;

    private boolean showInstructorLive;

    // ===== GETTERS & SETTERS =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

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