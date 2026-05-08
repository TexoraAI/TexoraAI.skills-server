package com.lms.user.model;

import jakarta.persistence.*;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "project_name", nullable = false)
    private String projectName;

    @Column(name = "tech_stack")
    private String techStack;

    @Column(name = "project_url")
    private String projectUrl;

    @Column(name = "start_date")
    private String startDate;

    @Column(name = "end_date")
    private String endDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Resume getResume() { return resume; }
    public void setResume(Resume resume) { this.resume = resume; }

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