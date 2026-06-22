//package com.lms.user.model;
//
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "resume_skills")
//public class ResumeSkill {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "resume_id", nullable = false)
//    private Resume resume;
//
//    @Column(name = "skill_name", nullable = false)
//    private String skillName;
//
//    @Column(name = "proficiency_level")
//    @Enumerated(EnumType.STRING)
//    private ProficiencyLevel proficiencyLevel;
//
//    @Column(name = "display_order")
//    private Integer displayOrder;
//
//    public enum ProficiencyLevel {
//        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
//    }
//
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public Resume getResume() { return resume; }
//    public void setResume(Resume resume) { this.resume = resume; }
//
//    public String getSkillName() { return skillName; }
//    public void setSkillName(String skillName) { this.skillName = skillName; }
//
//    public ProficiencyLevel getProficiencyLevel() { return proficiencyLevel; }
//    public void setProficiencyLevel(ProficiencyLevel proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }
//
//    public Integer getDisplayOrder() { return displayOrder; }
//    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
//}
package com.lms.user.model;

import jakarta.persistence.*;

// WHY: Skill proficiency data used for ATS scoring and job-skill matching in search-service
@Entity
@Table(name = "resume_skills",
    indexes = {
        // WHY: Skills are always fetched per resume — this is the only access pattern
        @Index(name = "idx_resume_skills_resume_id", columnList = "resume_id"),
        @Index(name = "idx_resume_skills_resume_order", columnList = "resume_id, display_order")
    })
public class ResumeSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "skill_name", nullable = false)
    private String skillName;

    // WHY: Enum string stored so ATS scoring logic can weight EXPERT > ADVANCED > etc.
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