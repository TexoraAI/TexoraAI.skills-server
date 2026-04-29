package com.lms.assessment.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "coding_problems", indexes = {
    @Index(name = "idx_cp_trainer_email", columnList = "trainerEmail"),
    @Index(name = "idx_cp_difficulty",    columnList = "difficulty")
})
public class CodingProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String inputFormat;

    @Column(columnDefinition = "TEXT")
    private String outputFormat;

 // In CodingProblem.java — change this field:

    @Column(name = "problem_constraints", columnDefinition = "TEXT")
    private String constraints;

    @Column(columnDefinition = "TEXT")
    private String sampleInput;

    @Column(columnDefinition = "TEXT")
    private String sampleOutput;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @Column(nullable = false)
    private String trainerEmail;

    @Column(nullable = false)
    private Integer totalMarks;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestCase> testCases = new ArrayList<>();

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isActive == null) this.isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────
    public CodingProblem() {}

    // ── Getters ───────────────────────────────────
    public Long getId()                  { return id; }
    public String getTitle()             { return title; }
    public String getDescription()       { return description; }
    public String getInputFormat()       { return inputFormat; }
    public String getOutputFormat()      { return outputFormat; }
    public String getConstraints()       { return constraints; }
    public String getSampleInput()       { return sampleInput; }
    public String getSampleOutput()      { return sampleOutput; }
    public Difficulty getDifficulty()    { return difficulty; }
    public String getTrainerEmail()      { return trainerEmail; }
    public Integer getTotalMarks()       { return totalMarks; }
    public Boolean getIsActive()         { return isActive; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public LocalDateTime getUpdatedAt()  { return updatedAt; }
    public List<TestCase> getTestCases() { return testCases; }

    // ── Setters ───────────────────────────────────
    public void setId(Long id)                        { this.id = id; }
    public void setTitle(String title)                { this.title = title; }
    public void setDescription(String description)    { this.description = description; }
    public void setInputFormat(String inputFormat)    { this.inputFormat = inputFormat; }
    public void setOutputFormat(String outputFormat)  { this.outputFormat = outputFormat; }
    public void setConstraints(String constraints)    { this.constraints = constraints; }
    public void setSampleInput(String sampleInput)    { this.sampleInput = sampleInput; }
    public void setSampleOutput(String sampleOutput)  { this.sampleOutput = sampleOutput; }
    public void setDifficulty(Difficulty difficulty)  { this.difficulty = difficulty; }
    public void setTrainerEmail(String trainerEmail)  { this.trainerEmail = trainerEmail; }
    public void setTotalMarks(Integer totalMarks)     { this.totalMarks = totalMarks; }
    public void setIsActive(Boolean isActive)         { this.isActive = isActive; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setTestCases(List<TestCase> testCases){ this.testCases = testCases; }

    // ── Builder ───────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String title;
        private String description;
        private String inputFormat;
        private String outputFormat;
        private String constraints;
        private String sampleInput;
        private String sampleOutput;
        private Difficulty difficulty;
        private String trainerEmail;
        private Integer totalMarks;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id)                        { this.id = id; return this; }
        public Builder title(String title)                { this.title = title; return this; }
        public Builder description(String description)    { this.description = description; return this; }
        public Builder inputFormat(String inputFormat)    { this.inputFormat = inputFormat; return this; }
        public Builder outputFormat(String outputFormat)  { this.outputFormat = outputFormat; return this; }
        public Builder constraints(String constraints)    { this.constraints = constraints; return this; }
        public Builder sampleInput(String sampleInput)   { this.sampleInput = sampleInput; return this; }
        public Builder sampleOutput(String sampleOutput) { this.sampleOutput = sampleOutput; return this; }
        public Builder difficulty(Difficulty difficulty)  { this.difficulty = difficulty; return this; }
        public Builder trainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; return this; }
        public Builder totalMarks(Integer totalMarks)    { this.totalMarks = totalMarks; return this; }
        public Builder isActive(Boolean isActive)        { this.isActive = isActive; return this; }
        public Builder createdAt(LocalDateTime v)        { this.createdAt = v; return this; }
        public Builder updatedAt(LocalDateTime v)        { this.updatedAt = v; return this; }

        public CodingProblem build() {
            CodingProblem p = new CodingProblem();
            p.id = id; p.title = title; p.description = description;
            p.inputFormat = inputFormat; p.outputFormat = outputFormat;
            p.constraints = constraints; p.sampleInput = sampleInput;
            p.sampleOutput = sampleOutput; p.difficulty = difficulty;
            p.trainerEmail = trainerEmail; p.totalMarks = totalMarks;
            p.isActive = isActive; p.createdAt = createdAt; p.updatedAt = updatedAt;
            return p;
        }
    }
}