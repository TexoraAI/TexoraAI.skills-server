package com.lms.user.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ResumeResponseDTO {

    private Long id;
    private Long userId;
    private String title;
    private String templateName;
    private Integer resumeScore;
    private Boolean isAtsFriendly;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Personal Info
    private String firstName;
    private String lastName;
    private String jobTitle;
    private String email;
    private String phone;
    private String city;
    private String country;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;
    private String profileSummary;

    // Nested response DTOs (each is its own file)
    private List<WorkExperienceResponseDTO> workExperiences;
    private List<EducationResponseDTO> educations;
    private List<ResumeSkillResponseDTO> skills;
    private List<ProjectResponseDTO> projects;
    private List<CertificationResponseDTO> certifications;

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

    public List<WorkExperienceResponseDTO> getWorkExperiences() { return workExperiences; }
    public void setWorkExperiences(List<WorkExperienceResponseDTO> workExperiences) { this.workExperiences = workExperiences; }

    public List<EducationResponseDTO> getEducations() { return educations; }
    public void setEducations(List<EducationResponseDTO> educations) { this.educations = educations; }

    public List<ResumeSkillResponseDTO> getSkills() { return skills; }
    public void setSkills(List<ResumeSkillResponseDTO> skills) { this.skills = skills; }

    public List<ProjectResponseDTO> getProjects() { return projects; }
    public void setProjects(List<ProjectResponseDTO> projects) { this.projects = projects; }

    public List<CertificationResponseDTO> getCertifications() { return certifications; }
    public void setCertifications(List<CertificationResponseDTO> certifications) { this.certifications = certifications; }
}