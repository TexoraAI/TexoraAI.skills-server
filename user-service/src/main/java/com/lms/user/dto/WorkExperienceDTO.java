package com.lms.user.dto;
 
import java.time.LocalDateTime;
import java.util.List;
class WorkExperienceDTO {
    private Long id;
    private String companyName;
    private String position;
    private String startDate;
    private String endDate;
    private Boolean isCurrent;
    private String location;
    private String description;
    private Integer displayOrder;
 
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public Boolean getIsCurrent() { return isCurrent; }
    public void setIsCurrent(Boolean isCurrent) { this.isCurrent = isCurrent; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}