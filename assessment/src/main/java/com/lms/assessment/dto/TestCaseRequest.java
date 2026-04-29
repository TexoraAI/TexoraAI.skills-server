package com.lms.assessment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TestCaseRequest {

    private String input;   // can be blank for no-input problems

    @NotBlank(message = "Expected output is required")
    private String expectedOutput;

    @NotNull(message = "Hidden flag is required")
    private Boolean isHidden;

    @NotNull(message = "Weightage is required")
    @Min(value = 1, message = "Weightage must be at least 1")
    private Integer weightage;

    // ── Constructors ──────────────────────────────
    public TestCaseRequest() {}

    // ── Getters ───────────────────────────────────
    public String getInput()          { return input; }
    public String getExpectedOutput() { return expectedOutput; }
    public Boolean getIsHidden()      { return isHidden; }
    public Integer getWeightage()     { return weightage; }

    // ── Setters ───────────────────────────────────
    public void setInput(String input)                   { this.input = input; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
    public void setIsHidden(Boolean isHidden)            { this.isHidden = isHidden; }
    public void setWeightage(Integer weightage)          { this.weightage = weightage; }
}