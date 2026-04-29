package com.lms.assessment.dto;

import com.lms.assessment.model.CodingProblem.Difficulty;
import java.time.LocalDateTime;
import java.util.List;

public class CodingProblemResponse {

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
    private List<TestCaseResponse> visibleTestCases;

    // ── Constructors ──────────────────────────────
    public CodingProblemResponse() {}

    private CodingProblemResponse(Builder b) {
        this.id               = b.id;
        this.title            = b.title;
        this.description      = b.description;
        this.inputFormat      = b.inputFormat;
        this.outputFormat     = b.outputFormat;
        this.constraints      = b.constraints;
        this.sampleInput      = b.sampleInput;
        this.sampleOutput     = b.sampleOutput;
        this.difficulty       = b.difficulty;
        this.trainerEmail     = b.trainerEmail;
        this.totalMarks       = b.totalMarks;
        this.isActive         = b.isActive;
        this.createdAt        = b.createdAt;
        this.updatedAt        = b.updatedAt;
        this.visibleTestCases = b.visibleTestCases;
    }

    // ── Getters ───────────────────────────────────
    public Long getId()                              { return id; }
    public String getTitle()                         { return title; }
    public String getDescription()                   { return description; }
    public String getInputFormat()                   { return inputFormat; }
    public String getOutputFormat()                  { return outputFormat; }
    public String getConstraints()                   { return constraints; }
    public String getSampleInput()                   { return sampleInput; }
    public String getSampleOutput()                  { return sampleOutput; }
    public Difficulty getDifficulty()                { return difficulty; }
    public String getTrainerEmail()                  { return trainerEmail; }
    public Integer getTotalMarks()                   { return totalMarks; }
    public Boolean getIsActive()                     { return isActive; }
    public LocalDateTime getCreatedAt()              { return createdAt; }
    public LocalDateTime getUpdatedAt()              { return updatedAt; }
    public List<TestCaseResponse> getVisibleTestCases() { return visibleTestCases; }

    // ── Setters ───────────────────────────────────
    public void setId(Long id)                                          { this.id = id; }
    public void setTitle(String title)                                  { this.title = title; }
    public void setDescription(String description)                      { this.description = description; }
    public void setInputFormat(String inputFormat)                      { this.inputFormat = inputFormat; }
    public void setOutputFormat(String outputFormat)                    { this.outputFormat = outputFormat; }
    public void setConstraints(String constraints)                      { this.constraints = constraints; }
    public void setSampleInput(String sampleInput)                      { this.sampleInput = sampleInput; }
    public void setSampleOutput(String sampleOutput)                    { this.sampleOutput = sampleOutput; }
    public void setDifficulty(Difficulty difficulty)                    { this.difficulty = difficulty; }
    public void setTrainerEmail(String trainerEmail)                    { this.trainerEmail = trainerEmail; }
    public void setTotalMarks(Integer totalMarks)                       { this.totalMarks = totalMarks; }
    public void setIsActive(Boolean isActive)                           { this.isActive = isActive; }
    public void setCreatedAt(LocalDateTime createdAt)                   { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)                   { this.updatedAt = updatedAt; }
    public void setVisibleTestCases(List<TestCaseResponse> visibleTestCases) { this.visibleTestCases = visibleTestCases; }

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
        private List<TestCaseResponse> visibleTestCases;

        public Builder id(Long id)                                           { this.id = id; return this; }
        public Builder title(String title)                                   { this.title = title; return this; }
        public Builder description(String description)                       { this.description = description; return this; }
        public Builder inputFormat(String inputFormat)                       { this.inputFormat = inputFormat; return this; }
        public Builder outputFormat(String outputFormat)                     { this.outputFormat = outputFormat; return this; }
        public Builder constraints(String constraints)                       { this.constraints = constraints; return this; }
        public Builder sampleInput(String sampleInput)                       { this.sampleInput = sampleInput; return this; }
        public Builder sampleOutput(String sampleOutput)                     { this.sampleOutput = sampleOutput; return this; }
        public Builder difficulty(Difficulty difficulty)                     { this.difficulty = difficulty; return this; }
        public Builder trainerEmail(String trainerEmail)                     { this.trainerEmail = trainerEmail; return this; }
        public Builder totalMarks(Integer totalMarks)                        { this.totalMarks = totalMarks; return this; }
        public Builder isActive(Boolean isActive)                            { this.isActive = isActive; return this; }
        public Builder createdAt(LocalDateTime createdAt)                    { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt)                    { this.updatedAt = updatedAt; return this; }
        public Builder visibleTestCases(List<TestCaseResponse> visibleTestCases) { this.visibleTestCases = visibleTestCases; return this; }

        public CodingProblemResponse build() { return new CodingProblemResponse(this); }
    }
}