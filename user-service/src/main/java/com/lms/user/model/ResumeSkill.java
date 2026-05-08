package com.lms.user.model;

import jakarta.persistence.*;

@Entity
@Table(name = "resume_skills")
public class ResumeSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "skill_name", nullable = false)
    private String skillName;

    @Column(name = "proficiency_level")
    @Enumerated(EnumType.STRING)
    private ProficiencyLevel proficiencyLevel;

    @Column(name = "display_order")
    private Integer displayOrder;

    public enum ProficiencyLevel {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Resume getResume() { return resume; }
    public void setResume(Resume resume) { this.resume = resume; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public ProficiencyLevel getProficiencyLevel() { return proficiencyLevel; }
    public void setProficiencyLevel(ProficiencyLevel proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}