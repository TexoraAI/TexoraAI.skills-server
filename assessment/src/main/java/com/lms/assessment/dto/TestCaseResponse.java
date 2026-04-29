package com.lms.assessment.dto;

public class TestCaseResponse {

    private Long id;
    private String input;
    private String expectedOutput;
    private Boolean isHidden;
    private Integer weightage;

    // ── Constructors ──────────────────────────────
    public TestCaseResponse() {}

    private TestCaseResponse(Builder b) {
        this.id             = b.id;
        this.input          = b.input;
        this.expectedOutput = b.expectedOutput;
        this.isHidden       = b.isHidden;
        this.weightage      = b.weightage;
    }

    // ── Getters ───────────────────────────────────
    public Long getId()              { return id; }
    public String getInput()         { return input; }
    public String getExpectedOutput(){ return expectedOutput; }
    public Boolean getIsHidden()     { return isHidden; }
    public Integer getWeightage()    { return weightage; }

    // ── Setters ───────────────────────────────────
    public void setId(Long id)                        { this.id = id; }
    public void setInput(String input)                { this.input = input; }
    public void setExpectedOutput(String expected)    { this.expectedOutput = expected; }
    public void setIsHidden(Boolean isHidden)         { this.isHidden = isHidden; }
    public void setWeightage(Integer weightage)       { this.weightage = weightage; }

    // ── Builder ───────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String input;
        private String expectedOutput;
        private Boolean isHidden;
        private Integer weightage;

        public Builder id(Long id)                       { this.id = id; return this; }
        public Builder input(String input)               { this.input = input; return this; }
        public Builder expectedOutput(String expected)   { this.expectedOutput = expected; return this; }
        public Builder isHidden(Boolean isHidden)        { this.isHidden = isHidden; return this; }
        public Builder weightage(Integer weightage)      { this.weightage = weightage; return this; }

        public TestCaseResponse build() { return new TestCaseResponse(this); }
    }
}