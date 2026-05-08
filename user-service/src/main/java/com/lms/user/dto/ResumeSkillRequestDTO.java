package com.lms.user.dto;

public class ResumeSkillRequestDTO {

    private String skillName;
    private String proficiencyLevel;  // BEGINNER | INTERMEDIATE | ADVANCED | EXPERT
    private Integer displayOrder;

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getProficiencyLevel() { return proficiencyLevel; }
    public void setProficiencyLevel(String proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}