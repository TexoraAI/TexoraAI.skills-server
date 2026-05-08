package com.lms.user.dto;

public class LinkedInScrapeRequestDTO {

    /**
     * Full LinkedIn profile URL
     * Example: https://www.linkedin.com/in/shaikkrosurushareef
     */
    private String linkedInUrl;

    /**
     * Optional: target job title to tailor the resume summary
     * Example: "Senior Java Developer"
     * If null, uses the person's current LinkedIn headline
     */
    private String jobTitle;

    /**
     * Optional: extra skills to append (comma separated)
     * Example: "Docker, Kubernetes, AWS"
     */
    private String extraSkills;

    /**
     * Optional: resume template name
     * Default: "classic"
     */
    private String templateName;
    
    private String base64Pdf;
    private String fileName;

    // Default Constructor
    public LinkedInScrapeRequestDTO() {
    }

    // Parameterized Constructor
    public LinkedInScrapeRequestDTO(String linkedInUrl, String jobTitle, String extraSkills, String templateName) {
        this.linkedInUrl = linkedInUrl;
        this.jobTitle = jobTitle;
        this.extraSkills = extraSkills;
        this.templateName = templateName;
    }

    // Getters and Setters
    public String getLinkedInUrl() {
        return linkedInUrl;
    }

    public void setLinkedInUrl(String linkedInUrl) {
        this.linkedInUrl = linkedInUrl;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getExtraSkills() {
        return extraSkills;
    }

    public void setExtraSkills(String extraSkills) {
        this.extraSkills = extraSkills;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
    public String getBase64Pdf() { return base64Pdf; }
    public void setBase64Pdf(String base64Pdf) { this.base64Pdf = base64Pdf; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}