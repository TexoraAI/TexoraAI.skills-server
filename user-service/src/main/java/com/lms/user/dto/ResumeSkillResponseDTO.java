package com.lms.user.dto;

public class ResumeSkillResponseDTO {

    private Long id;
    private String skillName;
    private String proficiencyLevel;
    private Integer displayOrder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getProficiencyLevel() { return proficiencyLevel; }
    public void setProficiencyLevel(String proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}