package com.lms.user.dto;
 
import java.time.LocalDateTime;
import java.util.List;
class ProjectDTO {
    private Long id;
    private String projectName;
    private String techStack;
    private String projectUrl;
    private String startDate;
    private String endDate;
    private String description;
    private Integer displayOrder;
 
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getTechStack() { return techStack; }
    public void setTechStack(String techStack) { this.techStack = techStack; }
    public String getProjectUrl() { return projectUrl; }
    public void setProjectUrl(String projectUrl) { this.projectUrl = projectUrl; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}