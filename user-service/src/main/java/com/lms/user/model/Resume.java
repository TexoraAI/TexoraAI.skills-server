package com.lms.user.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "template_name")
    private String templateName;

    @Column(name = "resume_score")
    private Integer resumeScore;

    @Column(name = "is_ats_friendly")
    private Boolean isAtsFriendly;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Personal Info
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "github_url")
    private String githubUrl;

    @Column(name = "portfolio_url")
    private String portfolioUrl;

    @Column(name = "profile_summary", columnDefinition = "TEXT")
    private String profileSummary;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkExperience> workExperiences = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Education> educations = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> projects = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Certification> certifications = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public Integer getResumeScore() { return resumeScore; }
    public void setResumeScore(Integer resumeScore) { this.resumeScore = resumeScore; }

    public Boolean getIsAtsFriendly() { return isAtsFriendly; }
    public void setIsAtsFriendly(Boolean isAtsFriendly) { this.isAtsFriendly = isAtsFriendly; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }

    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }

    public String getPortfolioUrl() { return portfolioUrl; }
    public void setPortfolioUrl(String portfolioUrl) { this.portfolioUrl = portfolioUrl; }

    public String getProfileSummary() { return profileSummary; }
    public void setProfileSummary(String profileSummary) { this.profileSummary = profileSummary; }

    public List<WorkExperience> getWorkExperiences() { return workExperiences; }
    public void setWorkExperiences(List<WorkExperience> workExperiences) { this.workExperiences = workExperiences; }

    public List<Education> getEducations() { return educations; }
    public void setEducations(List<Education> educations) { this.educations = educations; }

    public List<ResumeSkill> getSkills() { return skills; }
    public void setSkills(List<ResumeSkill> skills) { this.skills = skills; }

    public List<Project> getProjects() { return projects; }
    public void setProjects(List<Project> projects) { this.projects = projects; }

    public List<Certification> getCertifications() { return certifications; }
    public void setCertifications(List<Certification> certifications) { this.certifications = certifications; }
}