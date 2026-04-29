package com.lms.assessment.dto;

import com.lms.assessment.model.CodingProblem.Difficulty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;

public class CodingProblemRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String inputFormat;
    private String outputFormat;
    private String constraints;
    private String sampleInput;
    private String sampleOutput;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    @NotNull(message = "Total marks is required")
    @Min(value = 1, message = "Total marks must be at least 1")
    private Integer totalMarks;

    @Valid
    private List<TestCaseRequest> testCases = new ArrayList<>();

    // ── Constructors ──────────────────────────────
    public CodingProblemRequest() {}

    // ── Getters ───────────────────────────────────
    public String getTitle()                        { return title; }
    public String getDescription()                  { return description; }
    public String getInputFormat()                  { return inputFormat; }
    public String getOutputFormat()                 { return outputFormat; }
    public String getConstraints()                  { return constraints; }
    public String getSampleInput()                  { return sampleInput; }
    public String getSampleOutput()                 { return sampleOutput; }
    public Difficulty getDifficulty()               { return difficulty; }
    public Integer getTotalMarks()                  { return totalMarks; }
    public List<TestCaseRequest> getTestCases()     { return testCases; }

    // ── Setters ───────────────────────────────────
    public void setTitle(String title)                           { this.title = title; }
    public void setDescription(String description)               { this.description = description; }
    public void setInputFormat(String inputFormat)               { this.inputFormat = inputFormat; }
    public void setOutputFormat(String outputFormat)             { this.outputFormat = outputFormat; }
    public void setConstraints(String constraints)               { this.constraints = constraints; }
    public void setSampleInput(String sampleInput)               { this.sampleInput = sampleInput; }
    public void setSampleOutput(String sampleOutput)             { this.sampleOutput = sampleOutput; }
    public void setDifficulty(Difficulty difficulty)             { this.difficulty = difficulty; }
    public void setTotalMarks(Integer totalMarks)                { this.totalMarks = totalMarks; }
    public void setTestCases(List<TestCaseRequest> testCases)    { this.testCases = testCases; }
}