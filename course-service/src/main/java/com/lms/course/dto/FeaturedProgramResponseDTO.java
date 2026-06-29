package com.lms.course.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class FeaturedProgramResponseDTO {

    private Long id;
    private String title;
    private String slug;
    private String category;
    private String instructorName;
    private String company;
    private String level;
    private String status;
    private Integer displayOrder;
    private Integer durationWeeks;
    private Integer lessons;
    private Integer liveSessions;
    private Integer projects;
    private String studentsEnrolled;
    private Double rating;
    private BigDecimal price;
    private String offerText;
    private String enrollmentButtonText;
    private String enrollmentUrl;
    private String syllabusButtonText;
    private String shortDescription;
    private String fullDescription;
    private List<String> learningOutcomes;
    private List<String> highlights;
    private List<FAQDto> faqs;
    private List<SyllabusWeekDto> syllabusWeeks;
    private String videoUrl;
    private String thumbnailUrl;
    private String instructorRole;
    private String experience;
    private String studentCount;
    private Long learnersCount;
    private LocalDate publishDate;
    private Boolean showLiveBadge;
    private LocalDateTime createdAt;

    public FeaturedProgramResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Integer getDurationWeeks() {
        return durationWeeks;
    }

    public void setDurationWeeks(Integer durationWeeks) {
        this.durationWeeks = durationWeeks;
    }

    public Integer getLessons() {
        return lessons;
    }

    public void setLessons(Integer lessons) {
        this.lessons = lessons;
    }
    public Integer getLiveSessions() {
        return liveSessions;
    }

    public void setLiveSessions(Integer liveSessions) {
        this.liveSessions = liveSessions;
    }
    public Integer getProjects() {
        return projects;
    }

    public void setProjects(Integer projects) {
        this.projects = projects;
    }

    public String getStudentsEnrolled() {
        return studentsEnrolled;
    }

    public void setStudentsEnrolled(String studentsEnrolled) {
        this.studentsEnrolled = studentsEnrolled;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getOfferText() {
        return offerText;
    }

    public void setOfferText(String offerText) {
        this.offerText = offerText;
    }

    public String getEnrollmentButtonText() {
        return enrollmentButtonText;
    }

    public void setEnrollmentButtonText(String enrollmentButtonText) {
        this.enrollmentButtonText = enrollmentButtonText;
    }

    public String getEnrollmentUrl() {
        return enrollmentUrl;
    }

    public void setEnrollmentUrl(String enrollmentUrl) {
        this.enrollmentUrl = enrollmentUrl;
    }

    public String getSyllabusButtonText() {
        return syllabusButtonText;
    }

    public void setSyllabusButtonText(String syllabusButtonText) {
        this.syllabusButtonText = syllabusButtonText;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public List<String> getLearningOutcomes() {
        return learningOutcomes;
    }

    public void setLearningOutcomes(List<String> learningOutcomes) {
        this.learningOutcomes = learningOutcomes;
    }

    public List<String> getHighlights() {
        return highlights;
    }

    public void setHighlights(List<String> highlights) {
        this.highlights = highlights;
    }

    public List<FAQDto> getFaqs() {
        return faqs;
    }

    public void setFaqs(List<FAQDto> faqs) {
        this.faqs = faqs;
    }

    public List<SyllabusWeekDto> getSyllabusWeeks() {
        return syllabusWeeks;
    }

    public void setSyllabusWeeks(List<SyllabusWeekDto> syllabusWeeks) {
        this.syllabusWeeks = syllabusWeeks;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getInstructorRole() {
        return instructorRole;
    }

    public void setInstructorRole(String instructorRole) {
        this.instructorRole = instructorRole;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(String studentCount) {
        this.studentCount = studentCount;
    }

    public Long getLearnersCount() {
        return learnersCount;
    }

    public void setLearnersCount(Long learnersCount) {
        this.learnersCount = learnersCount;
    }

    public LocalDate getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDate publishDate) {
        this.publishDate = publishDate;
    }

    public Boolean getShowLiveBadge() {
        return showLiveBadge;
    }

    public void setShowLiveBadge(Boolean showLiveBadge) {
        this.showLiveBadge = showLiveBadge;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}