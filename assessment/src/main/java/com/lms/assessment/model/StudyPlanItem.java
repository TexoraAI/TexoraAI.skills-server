package com.lms.assessment.model;

import jakarta.persistence.*;

@Entity
@Table(name = "study_plan_items")
public class StudyPlanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private StudyPlanSection section;

    /* Reference to existing CodingProblem by ID */
    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(name = "problem_title")
    private String problemTitle;   // denormalised for fast reads

    @Column(name = "problem_difficulty")
    private String problemDifficulty;

    @Column(name = "problem_total_marks")
    private Integer problemTotalMarks;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    // ── Getters & Setters ──────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StudyPlanSection getSection() { return section; }
    public void setSection(StudyPlanSection section) { this.section = section; }

    public Long getProblemId() { return problemId; }
    public void setProblemId(Long problemId) { this.problemId = problemId; }

    public String getProblemTitle() { return problemTitle; }
    public void setProblemTitle(String problemTitle) { this.problemTitle = problemTitle; }

    public String getProblemDifficulty() { return problemDifficulty; }
    public void setProblemDifficulty(String problemDifficulty) { this.problemDifficulty = problemDifficulty; }

    public Integer getProblemTotalMarks() { return problemTotalMarks; }
    public void setProblemTotalMarks(Integer problemTotalMarks) { this.problemTotalMarks = problemTotalMarks; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}