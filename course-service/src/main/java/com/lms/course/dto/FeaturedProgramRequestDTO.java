//package com.lms.course.dto;
//
//import jakarta.validation.constraints.NotBlank;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.List;
//
//public class FeaturedProgramRequestDTO {
//
//    @NotBlank(message = "Title is required")
//    private String title;
//
//    private String slug;
//
//    @NotBlank(message = "Category is required")
//    private String category;
//
//    @NotBlank(message = "Instructor name is required")
//    private String instructorName;
//
//    private String company;
//
//    private String level;
//
//    private String status;
//
//    private Integer displayOrder;
//
//    private Integer durationWeeks;
//
//    private Integer lessons;
//    private Integer liveSessions;
//
//    private Integer projects;
//
//    private String studentsEnrolled;
//
//    private Double rating;
//
//    private BigDecimal price;
//
//    private String offerText;
//
//    private String enrollmentButtonText;
//
//    private String enrollmentUrl;
//
//    private String syllabusButtonText;
//
//    private String shortDescription;
//
//    private String fullDescription;
//
//    private List<String> learningOutcomes;
//
//    private List<String> highlights;
//
//    private List<FAQDto> faqs;
//
//    private List<SyllabusWeekDto> syllabusWeeks;
//
//    private String videoUrl;
//
//    private String thumbnailUrl;
//
//    private String instructorRole;
//
//    private String experience;
//
//    private String studentCount;
//
//    private Long learnersCount;
//
//    private LocalDate publishDate;
//
//    private Boolean showLiveBadge;
//
//    public FeaturedProgramRequestDTO() {
//    }
//
//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }
//
//    public String getSlug() {
//        return slug;
//    }
//
//    public void setSlug(String slug) {
//        this.slug = slug;
//    }
//
//    public String getCategory() {
//        return category;
//    }
//
//    public void setCategory(String category) {
//        this.category = category;
//    }
//
//    public String getInstructorName() {
//        return instructorName;
//    }
//
//    public void setInstructorName(String instructorName) {
//        this.instructorName = instructorName;
//    }
//
//    public String getCompany() {
//        return company;
//    }
//
//    public void setCompany(String company) {
//        this.company = company;
//    }
//
//    public String getLevel() {
//        return level;
//    }
//
//    public void setLevel(String level) {
//        this.level = level;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public Integer getDisplayOrder() {
//        return displayOrder;
//    }
//
//    public void setDisplayOrder(Integer displayOrder) {
//        this.displayOrder = displayOrder;
//    }
//
//    public Integer getDurationWeeks() {
//        return durationWeeks;
//    }
//
//    public void setDurationWeeks(Integer durationWeeks) {
//        this.durationWeeks = durationWeeks;
//    }
//
//    public Integer getLessons() {
//        return lessons;
//    }
//
//    public void setLessons(Integer lessons) {
//        this.lessons = lessons;
//    }
//
//    public Integer getProjects() {
//        return projects;
//    }
//    public Integer getLiveSessions() {
//        return liveSessions;
//    }
//
//    public void setLiveSessions(Integer liveSessions) {
//        this.liveSessions = liveSessions;
//    }
//    public void setProjects(Integer projects) {
//        this.projects = projects;
//    }
//
//    public String getStudentsEnrolled() {
//        return studentsEnrolled;
//    }
//
//    public void setStudentsEnrolled(String studentsEnrolled) {
//        this.studentsEnrolled = studentsEnrolled;
//    }
//
//    public Double getRating() {
//        return rating;
//    }
//
//    public void setRating(Double rating) {
//        this.rating = rating;
//    }
//
//    public BigDecimal getPrice() {
//        return price;
//    }
//
//    public void setPrice(BigDecimal price) {
//        this.price = price;
//    }
//
//    public String getOfferText() {
//        return offerText;
//    }
//
//    public void setOfferText(String offerText) {
//        this.offerText = offerText;
//    }
//
//    public String getEnrollmentButtonText() {
//        return enrollmentButtonText;
//    }
//
//    public void setEnrollmentButtonText(String enrollmentButtonText) {
//        this.enrollmentButtonText = enrollmentButtonText;
//    }
//
//    public String getEnrollmentUrl() {
//        return enrollmentUrl;
//    }
//
//    public void setEnrollmentUrl(String enrollmentUrl) {
//        this.enrollmentUrl = enrollmentUrl;
//    }
//
//    public String getSyllabusButtonText() {
//        return syllabusButtonText;
//    }
//
//    public void setSyllabusButtonText(String syllabusButtonText) {
//        this.syllabusButtonText = syllabusButtonText;
//    }
//
//    public String getShortDescription() {
//        return shortDescription;
//    }
//
//    public void setShortDescription(String shortDescription) {
//        this.shortDescription = shortDescription;
//    }
//
//    public String getFullDescription() {
//        return fullDescription;
//    }
//
//    public void setFullDescription(String fullDescription) {
//        this.fullDescription = fullDescription;
//    }
//
//    public List<String> getLearningOutcomes() {
//        return learningOutcomes;
//    }
//
//    public void setLearningOutcomes(List<String> learningOutcomes) {
//        this.learningOutcomes = learningOutcomes;
//    }
//
//    public List<String> getHighlights() {
//        return highlights;
//    }
//
//    public void setHighlights(List<String> highlights) {
//        this.highlights = highlights;
//    }
//
//    public List<FAQDto> getFaqs() {
//        return faqs;
//    }
//
//    public void setFaqs(List<FAQDto> faqs) {
//        this.faqs = faqs;
//    }
//
//    public List<SyllabusWeekDto> getSyllabusWeeks() {
//        return syllabusWeeks;
//    }
//
//    public void setSyllabusWeeks(List<SyllabusWeekDto> syllabusWeeks) {
//        this.syllabusWeeks = syllabusWeeks;
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
//    public String getThumbnailUrl() {
//        return thumbnailUrl;
//    }
//
//    public void setThumbnailUrl(String thumbnailUrl) {
//        this.thumbnailUrl = thumbnailUrl;
//    }
//
//    public String getInstructorRole() {
//        return instructorRole;
//    }
//
//    public void setInstructorRole(String instructorRole) {
//        this.instructorRole = instructorRole;
//    }
//
//    public String getExperience() {
//        return experience;
//    }
//
//    public void setExperience(String experience) {
//        this.experience = experience;
//    }
//
//    public String getStudentCount() {
//        return studentCount;
//    }
//
//    public void setStudentCount(String studentCount) {
//        this.studentCount = studentCount;
//    }
//
//    public Long getLearnersCount() {
//        return learnersCount;
//    }
//
//    public void setLearnersCount(Long learnersCount) {
//        this.learnersCount = learnersCount;
//    }
//
//    public LocalDate getPublishDate() {
//        return publishDate;
//    }
//
//    public void setPublishDate(LocalDate publishDate) {
//        this.publishDate = publishDate;
//    }
//
//    public Boolean getShowLiveBadge() {
//        return showLiveBadge;
//    }
//
//    public void setShowLiveBadge(Boolean showLiveBadge) {
//        this.showLiveBadge = showLiveBadge;
//    }
//}
package com.lms.course.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class FeaturedProgramRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    private String slug;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Instructor name is required")
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

    // ===== NEW: Basic info =====

    private String bannerUrl;

    private String instructorPhotoUrl;

    private String instructorLinkedIn;

    // ===== NEW: Pricing (Course Details tab) =====

    private BigDecimal originalPrice;

    private Integer discountPercent;

    private String currency;

    private Boolean emiAvailable;

    private Boolean freeTrial;

    // ===== NEW: Course statistics (Course Details tab) =====

    private Integer assignmentsCount;

    private Integer quizzesCount;

    private Integer reviewsCount;

    // ===== NEW: Instructor bio / career info (About tab) =====

    private String instructorBio;

    private List<String> jobRoles;

    private String salaryRange;

    private List<String> hiringCompanies;

    private Boolean placementSupport;

    private String careerAssistance;

    // ===== NEW: Outcomes tab =====

    private List<String> skills;

    private List<ProgramProjectDto> projectsList;

    // ===== NEW: Highlights tab - certificate =====

    private String certificateTitle;

    private String certificateImageUrl;

    private String certificateVerificationUrl;

    // ===== NEW: FAQs tab - SEO =====

    private String metaTitle;

    private String metaDescription;

    private List<String> metaKeywords;

    private String ogImageUrl;

    // ===== NEW: Syllabus tab - display settings =====

    private Boolean showOnHomepage;

    private Boolean isFeatured;

    private Boolean isTrending;

    private Boolean isBestseller;

    private Boolean isPopular;

    private Boolean isRecommended;

    private Boolean isComingSoon;

    // ===== NEW: Draft / publish workflow =====

    private String publishStatus;

    public FeaturedProgramRequestDTO() {
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
    public Integer getLiveSessions() {
        return liveSessions;
    }

    public void setLiveSessions(Integer liveSessions) {
        this.liveSessions = liveSessions;
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

    public List<ProgramProjectDto> getProjectsList() {
        return projectsList;
    }

    public void setProjectsList(List<ProgramProjectDto> projectsList) {
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
}