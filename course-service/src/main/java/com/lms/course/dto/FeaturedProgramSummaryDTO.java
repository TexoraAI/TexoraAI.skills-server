package com.lms.course.dto;

import java.math.BigDecimal;

/**
 * Lightweight projection used by GET /featurecourse/summary (LMS homepage).
 * Populated directly by the JPQL constructor expression in
 * FeaturedProgramRepository#findSummaryByStatusAndPublishStatus — field order
 * here MUST match the order of that SELECT new ... (...) clause exactly.
 *
 * NOTE: this file was reconstructed to match that constructor signature since
 * the original wasn't available — if your real version had extra
 * fields/annotations beyond this, re-add them; the constructor order below is
 * the part that's load-bearing.
 */
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

    // ── NEW: badge flags — homepage should read these instead of guessing
    // isBestseller from rating client-side ──
    private Boolean isFeatured;
    private Boolean isTrending;
    private Boolean isBestseller;
    private Boolean isPopular;
    private Boolean isRecommended;
    private Boolean isComingSoon;

    // ── NEW: discount pricing, so homepage cards can show strike-through price ──
    private BigDecimal originalPrice;
    private Integer discountPercent;

    public FeaturedProgramSummaryDTO(
            Long id, String title, String category, String instructorName, String instructorRole,
            String level, Integer durationWeeks, Integer lessons, Integer liveSessions, Integer projects,
            String studentsEnrolled, Double rating, BigDecimal price, String shortDescription,
            String thumbnailUrl, String bannerUrl, String instructorPhotoUrl, String instructorLinkedIn,
            String videoUrl, String enrollmentUrl,
            Boolean isFeatured, Boolean isTrending, Boolean isBestseller, Boolean isPopular,
            Boolean isRecommended, Boolean isComingSoon,
            BigDecimal originalPrice, Integer discountPercent) {
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
        this.isFeatured = isFeatured;
        this.isTrending = isTrending;
        this.isBestseller = isBestseller;
        this.isPopular = isPopular;
        this.isRecommended = isRecommended;
        this.isComingSoon = isComingSoon;
        this.originalPrice = originalPrice;
        this.discountPercent = discountPercent;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getInstructorName() { return instructorName; }
    public String getInstructorRole() { return instructorRole; }
    public String getLevel() { return level; }
    public Integer getDurationWeeks() { return durationWeeks; }
    public Integer getLessons() { return lessons; }
    public Integer getLiveSessions() { return liveSessions; }
    public Integer getProjects() { return projects; }
    public String getStudentsEnrolled() { return studentsEnrolled; }
    public Double getRating() { return rating; }
    public BigDecimal getPrice() { return price; }
    public String getShortDescription() { return shortDescription; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getBannerUrl() { return bannerUrl; }
    public String getInstructorPhotoUrl() { return instructorPhotoUrl; }
    public String getInstructorLinkedIn() { return instructorLinkedIn; }
    public String getVideoUrl() { return videoUrl; }
    public String getEnrollmentUrl() { return enrollmentUrl; }
    public Boolean getIsFeatured() { return isFeatured; }
    public Boolean getIsTrending() { return isTrending; }
    public Boolean getIsBestseller() { return isBestseller; }
    public Boolean getIsPopular() { return isPopular; }
    public Boolean getIsRecommended() { return isRecommended; }
    public Boolean getIsComingSoon() { return isComingSoon; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public Integer getDiscountPercent() { return discountPercent; }
}