package com.lms.user.dto;

public class AIResumeRequestDTO {

    public static class GenerateRequest {
        private String name;
        private String email;
        private String linkedinUrl;
        private String jobTitle;
        private String yearsOfExperience;
        private String skills;
        private String templateName;

        public GenerateRequest() {}
        public GenerateRequest(String name, String email, String linkedinUrl,
                String jobTitle, String yearsOfExperience,
                String skills, String templateName) {
this.name = name;
this.email = email;
this.linkedinUrl = linkedinUrl;
this.jobTitle = jobTitle;
this.yearsOfExperience = yearsOfExperience;
this.skills = skills;
this.templateName = templateName;
}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getLinkedinUrl() { return linkedinUrl; }
        public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }
        public String getJobTitle() { return jobTitle; }
        public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
        public String getYearsOfExperience() { return yearsOfExperience; }
        public void setYearsOfExperience(String yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }
        public String getSkills() { return skills; }
        public void setSkills(String skills) { this.skills = skills; }
        public String getTemplateName() {
            return templateName;
        }

        public void setTemplateName(String templateName) {
            this.templateName = templateName;
        }
    }

    public static class ParsePdfRequest {
        private String base64Pdf;
        private String fileName;

        public ParsePdfRequest() {}
        public String getBase64Pdf() { return base64Pdf; }
        public void setBase64Pdf(String base64Pdf) { this.base64Pdf = base64Pdf; }
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
    }

    public static class AtsTipsRequest {
        private String  jobTitle;
        private String  profileSummary;
        private String  skillNames;
        private int     workExperienceCount;
        private int     educationCount;
        private int     projectCount;
        private int     certificationCount;
        private boolean hasLinkedin;
        private boolean hasGithub;

        public AtsTipsRequest() {}

        public String getJobTitle() { return jobTitle; }
        public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
        public String getProfileSummary() { return profileSummary; }
        public void setProfileSummary(String profileSummary) { this.profileSummary = profileSummary; }
        public String getSkillNames() { return skillNames; }
        public void setSkillNames(String skillNames) { this.skillNames = skillNames; }
        public int getWorkExperienceCount() { return workExperienceCount; }
        public void setWorkExperienceCount(int workExperienceCount) { this.workExperienceCount = workExperienceCount; }
        public int getEducationCount() { return educationCount; }
        public void setEducationCount(int educationCount) { this.educationCount = educationCount; }
        public int getProjectCount() { return projectCount; }
        public void setProjectCount(int projectCount) { this.projectCount = projectCount; }
        public int getCertificationCount() { return certificationCount; }
        public void setCertificationCount(int certificationCount) { this.certificationCount = certificationCount; }
        public boolean isHasLinkedin() { return hasLinkedin; }
        public void setHasLinkedin(boolean hasLinkedin) { this.hasLinkedin = hasLinkedin; }
        public boolean isHasGithub() { return hasGithub; }
        public void setHasGithub(boolean hasGithub) { this.hasGithub = hasGithub; }
    }
}