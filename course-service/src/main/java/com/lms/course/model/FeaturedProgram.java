
package com.lms.course.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "featured_programs")
public class FeaturedProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String instructorName;

    private String company;

    @Column(nullable = false)
    private String level = "Beginner";

    @Column(nullable = false)
    private String status = "Active";

    private Integer displayOrder = 1;

    private Integer durationWeeks;

    private Integer lessons;

    private Integer projects;
    private Integer liveSessions;

    private String studentsEnrolled;

    private Double rating;

    private BigDecimal price;

    private String offerText;

    private String enrollmentButtonText;

    private String enrollmentUrl;

    private String syllabusButtonText;

    @Column(length = 300)
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String fullDescription;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "program_learning_outcomes", joinColumns = @JoinColumn(name = "program_id"))
    @Column(name = "outcome", columnDefinition = "TEXT")
    private List<String> learningOutcomes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "program_highlights", joinColumns = @JoinColumn(name = "program_id"))
    @Column(name = "highlight", columnDefinition = "TEXT")
    private List<String> highlights = new ArrayList<>();

//    private String videoUrl;
//
//    private String thumbnailUrl;
    @Column(columnDefinition = "TEXT")
    private String videoUrl;

    @Column(columnDefinition = "TEXT")
    private String thumbnailUrl;

    private String instructorRole;

    private String experience;

    private String studentCount;

    private Long learnersCount;

    private LocalDate publishDate;

    private Boolean showLiveBadge = false;

    // ===== NEW: Basic info =====

//    private String bannerUrl;
//
//    private String instructorPhotoUrl;
//
//    private String instructorLinkedIn;
    @Column(columnDefinition = "TEXT")
    private String bannerUrl;

    @Column(columnDefinition = "TEXT")
    private String instructorPhotoUrl;

    private String instructorLinkedIn;

    // ===== NEW: Pricing (Course Details tab) =====

    private BigDecimal originalPrice;

    private Integer discountPercent;

    @Column(nullable = false)
    private String currency = "INR";

    private Boolean emiAvailable = false;

    private Boolean freeTrial = false;

    // ===== NEW: Course statistics (Course Details tab) =====

    private Integer assignmentsCount;

    private Integer quizzesCount;

    private Integer reviewsCount;

    // ===== NEW: Instructor bio / career info (About tab) =====

    @Column(columnDefinition = "TEXT")
    private String instructorBio;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "program_job_roles", joinColumns = @JoinColumn(name = "program_id"))
    @Column(name = "job_role", columnDefinition = "TEXT")
    private List<String> jobRoles = new ArrayList<>();

    private String salaryRange;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "program_hiring_companies", joinColumns = @JoinColumn(name = "program_id"))
    @Column(name = "hiring_company", columnDefinition = "TEXT")
    private List<String> hiringCompanies = new ArrayList<>();

    private Boolean placementSupport = false;

    @Column(columnDefinition = "TEXT")
    private String careerAssistance;

    // ===== NEW: Outcomes tab =====

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "program_skills", joinColumns = @JoinColumn(name = "program_id"))
    @Column(name = "skill", columnDefinition = "TEXT")
    private List<String> skills = new ArrayList<>();

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("displayOrder ASC")
    private List<ProgramProject> projectsList = new ArrayList<>();

    // ===== NEW: Highlights tab - certificate =====

//    private String certificateTitle;
//
//    private String certificateImageUrl;
//
//    private String certificateVerificationUrl;
    private String certificateTitle;

    @Column(columnDefinition = "TEXT")
    private String certificateImageUrl;

    private String certificateVerificationUrl;

    // ===== NEW: FAQs tab - SEO =====

    @Column(length = 70)
    private String metaTitle;

    @Column(length = 160)
    private String metaDescription;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "program_meta_keywords", joinColumns = @JoinColumn(name = "program_id"))
    @Column(name = "keyword", columnDefinition = "TEXT")
    private List<String> metaKeywords = new ArrayList<>();

//    private String ogImageUrl;
    @Column(columnDefinition = "TEXT")
    private String ogImageUrl;

    // ===== NEW: Syllabus tab - display settings =====

    private Boolean showOnHomepage = false;

    private Boolean isFeatured = false;

    private Boolean isTrending = false;

    private Boolean isBestseller = false;

    private Boolean isPopular = false;

    private Boolean isRecommended = false;

    private Boolean isComingSoon = false;

    // ===== NEW: Draft / publish workflow =====

    @Column(nullable = false)
    private String publishStatus = "Draft";

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<FeaturedProgramFAQ> faqs = new ArrayList<>();

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SyllabusWeek> syllabusWeeks = new ArrayList<>();

    public FeaturedProgram() {
    }

    // ===== Getters and Setters =====

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

    public Integer getProjects() {
        return projects;
    }

    public void setProjects(Integer projects) {
        this.projects = projects;
    }

    public String getStudentsEnrolled() {
        return studentsEnrolled;
    }
    public Integer getLiveSessions() {
        return liveSessions;
    }

    public void setLiveSessions(Integer liveSessions) {
        this.liveSessions = liveSessions;
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

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getInstructorPhotoUrl() {
        return instructorPhotoUrl;
    }

    public void setInstructorPhotoUrl(String instructorPhotoUrl) {
        this.instructorPhotoUrl = instructorPhotoUrl;
    }

    public String getInstructorLinkedIn() {
        return instructorLinkedIn;
    }

    public void setInstructorLinkedIn(String instructorLinkedIn) {
        this.instructorLinkedIn = instructorLinkedIn;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Integer getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Integer discountPercent) {
        this.discountPercent = discountPercent;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Boolean getEmiAvailable() {
        return emiAvailable;
    }

    public void setEmiAvailable(Boolean emiAvailable) {
        this.emiAvailable = emiAvailable;
    }

    public Boolean getFreeTrial() {
        return freeTrial;
    }

    public void setFreeTrial(Boolean freeTrial) {
        this.freeTrial = freeTrial;
    }

    public Integer getAssignmentsCount() {
        return assignmentsCount;
    }

    public void setAssignmentsCount(Integer assignmentsCount) {
        this.assignmentsCount = assignmentsCount;
    }

    public Integer getQuizzesCount() {
        return quizzesCount;
    }

    public void setQuizzesCount(Integer quizzesCount) {
        this.quizzesCount = quizzesCount;
    }

    public Integer getReviewsCount() {
        return reviewsCount;
    }

    public void setReviewsCount(Integer reviewsCount) {
        this.reviewsCount = reviewsCount;
    }

    public String getInstructorBio() {
        return instructorBio;
    }

    public void setInstructorBio(String instructorBio) {
        this.instructorBio = instructorBio;
    }

    public List<String> getJobRoles() {
        return jobRoles;
    }

    public void setJobRoles(List<String> jobRoles) {
        this.jobRoles = jobRoles;
    }

    public String getSalaryRange() {
        return salaryRange;
    }

    public void setSalaryRange(String salaryRange) {
        this.salaryRange = salaryRange;
    }

    public List<String> getHiringCompanies() {
        return hiringCompanies;
    }

    public void setHiringCompanies(List<String> hiringCompanies) {
        this.hiringCompanies = hiringCompanies;
    }

    public Boolean getPlacementSupport() {
        return placementSupport;
    }

    public void setPlacementSupport(Boolean placementSupport) {
        this.placementSupport = placementSupport;
    }

    public String getCareerAssistance() {
        return careerAssistance;
    }

    public void setCareerAssistance(String careerAssistance) {
        this.careerAssistance = careerAssistance;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<ProgramProject> getProjectsList() {
        return projectsList;
    }

    public void setProjectsList(List<ProgramProject> projectsList) {
        this.projectsList = projectsList;
    }

    public String getCertificateTitle() {
        return certificateTitle;
    }

    public void setCertificateTitle(String certificateTitle) {
        this.certificateTitle = certificateTitle;
    }

    public String getCertificateImageUrl() {
        return certificateImageUrl;
    }

    public void setCertificateImageUrl(String certificateImageUrl) {
        this.certificateImageUrl = certificateImageUrl;
    }

    public String getCertificateVerificationUrl() {
        return certificateVerificationUrl;
    }

    public void setCertificateVerificationUrl(String certificateVerificationUrl) {
        this.certificateVerificationUrl = certificateVerificationUrl;
    }

    public String getMetaTitle() {
        return metaTitle;
    }

    public void setMetaTitle(String metaTitle) {
        this.metaTitle = metaTitle;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public List<String> getMetaKeywords() {
        return metaKeywords;
    }

    public void setMetaKeywords(List<String> metaKeywords) {
        this.metaKeywords = metaKeywords;
    }

    public String getOgImageUrl() {
        return ogImageUrl;
    }

    public void setOgImageUrl(String ogImageUrl) {
        this.ogImageUrl = ogImageUrl;
    }

    public Boolean getShowOnHomepage() {
        return showOnHomepage;
    }

    public void setShowOnHomepage(Boolean showOnHomepage) {
        this.showOnHomepage = showOnHomepage;
    }

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public Boolean getIsTrending() {
        return isTrending;
    }

    public void setIsTrending(Boolean isTrending) {
        this.isTrending = isTrending;
    }

    public Boolean getIsBestseller() {
        return isBestseller;
    }

    public void setIsBestseller(Boolean isBestseller) {
        this.isBestseller = isBestseller;
    }

    public Boolean getIsPopular() {
        return isPopular;
    }

    public void setIsPopular(Boolean isPopular) {
        this.isPopular = isPopular;
    }

    public Boolean getIsRecommended() {
        return isRecommended;
    }

    public void setIsRecommended(Boolean isRecommended) {
        this.isRecommended = isRecommended;
    }

    public Boolean getIsComingSoon() {
        return isComingSoon;
    }

    public void setIsComingSoon(Boolean isComingSoon) {
        this.isComingSoon = isComingSoon;
    }

    public String getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(String publishStatus) {
        this.publishStatus = publishStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<FeaturedProgramFAQ> getFaqs() {
        return faqs;
    }

    public void setFaqs(List<FeaturedProgramFAQ> faqs) {
        this.faqs = faqs;
    }

    public List<SyllabusWeek> getSyllabusWeeks() {
        return syllabusWeeks;
    }

    public void setSyllabusWeeks(List<SyllabusWeek> syllabusWeeks) {
        this.syllabusWeeks = syllabusWeeks;
    }
}