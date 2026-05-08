package com.lms.user.dto;

import java.util.List;

public class ResumeRequestDTO {

    private String title;
    private String templateName;

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

    // Nested request DTOs (each is its own file)
    private List<WorkExperienceRequestDTO> workExperiences;
    private List<EducationRequestDTO> educations;
    private List<ResumeSkillRequestDTO> skills;
    private List<ProjectRequestDTO> projects;
    private List<CertificationRequestDTO> certifications;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

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

    public List<WorkExperienceRequestDTO> getWorkExperiences() { return workExperiences; }
    public void setWorkExperiences(List<WorkExperienceRequestDTO> workExperiences) { this.workExperiences = workExperiences; }

    public List<EducationRequestDTO> getEducations() { return educations; }
    public void setEducations(List<EducationRequestDTO> educations) { this.educations = educations; }

    public List<ResumeSkillRequestDTO> getSkills() { return skills; }
    public void setSkills(List<ResumeSkillRequestDTO> skills) { this.skills = skills; }

    public List<ProjectRequestDTO> getProjects() { return projects; }
    public void setProjects(List<ProjectRequestDTO> projects) { this.projects = projects; }

    public List<CertificationRequestDTO> getCertifications() { return certifications; }
    public void setCertifications(List<CertificationRequestDTO> certifications) { this.certifications = certifications; }
}