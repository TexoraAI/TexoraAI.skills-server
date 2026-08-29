package com.lms.course.dto;

import java.math.BigDecimal;

public class FeaturedProgramSummaryDTO {

    private Long id;
    private String title;
    private String category;
    private String instructorName;
    private String instructorRole;
    private String level;
    private Integer durationWeeks;
    private Integer lessons;
    private Integer liveSessions;
    private Integer projects;
    private String studentsEnrolled;
    private Double rating;
    private BigDecimal price;
    private String shortDescription;
    private String thumbnailUrl;
    private String bannerUrl;
    private String instructorPhotoUrl;
    private String instructorLinkedIn;
    private String videoUrl;
    private String enrollmentUrl;

    public FeaturedProgramSummaryDTO() {
    }

    public FeaturedProgramSummaryDTO(Long id, String title, String category, String instructorName,
            String instructorRole, String level, Integer durationWeeks, Integer lessons, Integer liveSessions,
            Integer projects, String studentsEnrolled, Double rating, BigDecimal price, String shortDescription,
            String thumbnailUrl, String bannerUrl, String instructorPhotoUrl, String instructorLinkedIn,
            String videoUrl, String enrollmentUrl) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.instructorName = instructorName;
        this.instructorRole = instructorRole;
        this.level = level;
        this.durationWeeks = durationWeeks;
        this.lessons = lessons;
        this.liveSessions = liveSessions;
        this.projects = projects;
        this.studentsEnrolled = studentsEnrolled;
        this.rating = rating;
        this.price = price;
        this.shortDescription = shortDescription;
        this.thumbnailUrl = thumbnailUrl;
        this.bannerUrl = bannerUrl;
        this.instructorPhotoUrl = instructorPhotoUrl;
        this.instructorLinkedIn = instructorLinkedIn;
        this.videoUrl = videoUrl;
        this.enrollmentUrl = enrollmentUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    public String getInstructorRole() { return instructorRole; }
    public void setInstructorRole(String instructorRole) { this.instructorRole = instructorRole; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public Integer getDurationWeeks() { return durationWeeks; }
    public void setDurationWeeks(Integer durationWeeks) { this.durationWeeks = durationWeeks; }

    public Integer getLessons() { return lessons; }
    public void setLessons(Integer lessons) { this.lessons = lessons; }

    public Integer getLiveSessions() { return liveSessions; }
    public void setLiveSessions(Integer liveSessions) { this.liveSessions = liveSessions; }

    public Integer getProjects() { return projects; }
    public void setProjects(Integer projects) { this.projects = projects; }

    public String getStudentsEnrolled() { return studentsEnrolled; }
    public void setStudentsEnrolled(String studentsEnrolled) { this.studentsEnrolled = studentsEnrolled; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }

    public String getInstructorPhotoUrl() { return instructorPhotoUrl; }
    public void setInstructorPhotoUrl(String instructorPhotoUrl) { this.instructorPhotoUrl = instructorPhotoUrl; }

    public String getInstructorLinkedIn() { return instructorLinkedIn; }
    public void setInstructorLinkedIn(String instructorLinkedIn) { this.instructorLinkedIn = instructorLinkedIn; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getEnrollmentUrl() { return enrollmentUrl; }
    public void setEnrollmentUrl(String enrollmentUrl) { this.enrollmentUrl = enrollmentUrl; }
}