package com.lms.assessment.model;

import jakarta.persistence.*;

@Entity
@Table(name = "test_cases", indexes = {
    @Index(name = "idx_tc_problem_id", columnList = "problem_id")
})
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private CodingProblem problem;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String input;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String expectedOutput;

    // Hidden test cases are not shown to students
    @Column(nullable = false)
    private Boolean isHidden;

    // Marks awarded for passing this test case
    @Column(nullable = false)
    private Integer marks;

//    @Column(nullable = false)
//    private Integer orderIndex;
 // To this:
    @Column(nullable = false, columnDefinition = "integer default 0")
    private int orderIndex;
    // ── Constructors ──────────────────────────────
    public TestCase() {}

    // ── Getters ───────────────────────────────────
    public Long getId()                    { return id; }
    public CodingProblem getProblem()      { return problem; }
    public String getInput()               { return input; }
    public String getExpectedOutput()      { return expectedOutput; }
    public Boolean getIsHidden()           { return isHidden; }
    public Integer getMarks()              { return marks; }
    public Integer getOrderIndex()         { return orderIndex; }

    // ── Setters ───────────────────────────────────
    public void setId(Long id)                           { this.id = id; }
    public void setProblem(CodingProblem problem)        { this.problem = problem; }
    public void setInput(String input)                   { this.input = input; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
    public void setIsHidden(Boolean isHidden)            { this.isHidden = isHidden; }
    public void setMarks(Integer marks)                  { this.marks = marks; }
    public void setOrderIndex(Integer orderIndex)        { this.orderIndex = orderIndex; }

    // ── Builder ───────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private CodingProblem problem;
        private String input;
        private String expectedOutput;
        private Boolean isHidden;
        private Integer marks;
        private Integer orderIndex;

        public Builder id(Long id)                           { this.id = id; return this; }
        public Builder problem(CodingProblem problem)        { this.problem = problem; return this; }
        public Builder input(String input)                   { this.input = input; return this; }
        public Builder expectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; return this; }
        public Builder isHidden(Boolean isHidden)            { this.isHidden = isHidden; return this; }
        public Builder marks(Integer marks)                  { this.marks = marks; return this; }
        public Builder orderIndex(Integer orderIndex)        { this.orderIndex = orderIndex; return this; }

        public TestCase build() {
            TestCase t = new TestCase();
            t.id = id; t.problem = problem; t.input = input;
            t.expectedOutput = expectedOutput; t.isHidden = isHidden;
            t.marks = marks; t.orderIndex = orderIndex;
            return t;
        }
    }
}