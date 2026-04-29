package com.lms.assessment.dto;

public class JudgeResult {

    private Long testCaseId;
    private String input;           // null if hidden
    private String expectedOutput;  // null if hidden
    private String actualOutput;
    private Boolean passed;
    private Integer weightage;
    private String verdict;         // PASSED / WRONG_ANSWER / RUNTIME_ERROR / TIMEOUT

    // ── Constructors ──────────────────────────────
    public JudgeResult() {}

    private JudgeResult(Builder b) {
        this.testCaseId     = b.testCaseId;
        this.input          = b.input;
        this.expectedOutput = b.expectedOutput;
        this.actualOutput   = b.actualOutput;
        this.passed         = b.passed;
        this.weightage      = b.weightage;
        this.verdict        = b.verdict;
    }

    // ── Getters ───────────────────────────────────
    public Long getTestCaseId()       { return testCaseId; }
    public String getInput()          { return input; }
    public String getExpectedOutput() { return expectedOutput; }
    public String getActualOutput()   { return actualOutput; }
    public Boolean getPassed()        { return passed; }
    public Integer getWeightage()     { return weightage; }
    public String getVerdict()        { return verdict; }

    // ── Setters ───────────────────────────────────
    public void setTestCaseId(Long testCaseId)          { this.testCaseId = testCaseId; }
    public void setInput(String input)                  { this.input = input; }
    public void setExpectedOutput(String expected)      { this.expectedOutput = expected; }
    public void setActualOutput(String actualOutput)    { this.actualOutput = actualOutput; }
    public void setPassed(Boolean passed)               { this.passed = passed; }
    public void setWeightage(Integer weightage)         { this.weightage = weightage; }
    public void setVerdict(String verdict)              { this.verdict = verdict; }

    // ── Builder ───────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long testCaseId;
        private String input;
        private String expectedOutput;
        private String actualOutput;
        private Boolean passed;
        private Integer weightage;
        private String verdict;

        public Builder testCaseId(Long testCaseId)        { this.testCaseId = testCaseId; return this; }
        public Builder input(String input)                { this.input = input; return this; }
        public Builder expectedOutput(String expected)    { this.expectedOutput = expected; return this; }
        public Builder actualOutput(String actualOutput)  { this.actualOutput = actualOutput; return this; }
        public Builder passed(Boolean passed)             { this.passed = passed; return this; }
        public Builder weightage(Integer weightage)       { this.weightage = weightage; return this; }
        public Builder verdict(String verdict)            { this.verdict = verdict; return this; }

        public JudgeResult build() { return new JudgeResult(this); }
    }
}