package com.lms.assessment.dto;

import com.lms.assessment.model.CodeSubmission.ExecutionStatus;
import java.time.LocalDateTime;
import java.util.List;

public class SubmissionJudgeResponse {

    private Long submissionId;
    private Long problemId;
    private String studentEmail;
    private String batchId;
    private String language;
    private ExecutionStatus executionStatus;
    private Integer totalMarks;
    private Integer marksObtained;
    private Integer testCasesPassed;
    private Integer totalTestCases;
    private String overallVerdict;          // ACCEPTED / PARTIAL / REJECTED
    private List<JudgeResult> judgeResults;
    private Long executionTimeMs;
    private LocalDateTime submittedAt;

    // ── Constructors ──────────────────────────────
    public SubmissionJudgeResponse() {}

    private SubmissionJudgeResponse(Builder b) {
        this.submissionId    = b.submissionId;
        this.problemId       = b.problemId;
        this.studentEmail    = b.studentEmail;
        this.batchId         = b.batchId;
        this.language        = b.language;
        this.executionStatus = b.executionStatus;
        this.totalMarks      = b.totalMarks;
        this.marksObtained   = b.marksObtained;
        this.testCasesPassed = b.testCasesPassed;
        this.totalTestCases  = b.totalTestCases;
        this.overallVerdict  = b.overallVerdict;
        this.judgeResults    = b.judgeResults;
        this.executionTimeMs = b.executionTimeMs;
        this.submittedAt     = b.submittedAt;
    }

    // ── Getters ───────────────────────────────────
    public Long getSubmissionId()              { return submissionId; }
    public Long getProblemId()                 { return problemId; }
    public String getStudentEmail()            { return studentEmail; }
    public String getBatchId()                 { return batchId; }
    public String getLanguage()                { return language; }
    public ExecutionStatus getExecutionStatus(){ return executionStatus; }
    public Integer getTotalMarks()             { return totalMarks; }
    public Integer getMarksObtained()          { return marksObtained; }
    public Integer getTestCasesPassed()        { return testCasesPassed; }
    public Integer getTotalTestCases()         { return totalTestCases; }
    public String getOverallVerdict()          { return overallVerdict; }
    public List<JudgeResult> getJudgeResults() { return judgeResults; }
    public Long getExecutionTimeMs()           { return executionTimeMs; }
    public LocalDateTime getSubmittedAt()      { return submittedAt; }

    // ── Setters ───────────────────────────────────
    public void setSubmissionId(Long submissionId)              { this.submissionId = submissionId; }
    public void setProblemId(Long problemId)                    { this.problemId = problemId; }
    public void setStudentEmail(String studentEmail)            { this.studentEmail = studentEmail; }
    public void setBatchId(String batchId)                      { this.batchId = batchId; }
    public void setLanguage(String language)                    { this.language = language; }
    public void setExecutionStatus(ExecutionStatus status)      { this.executionStatus = status; }
    public void setTotalMarks(Integer totalMarks)               { this.totalMarks = totalMarks; }
    public void setMarksObtained(Integer marksObtained)         { this.marksObtained = marksObtained; }
    public void setTestCasesPassed(Integer testCasesPassed)     { this.testCasesPassed = testCasesPassed; }
    public void setTotalTestCases(Integer totalTestCases)       { this.totalTestCases = totalTestCases; }
    public void setOverallVerdict(String overallVerdict)        { this.overallVerdict = overallVerdict; }
    public void setJudgeResults(List<JudgeResult> judgeResults) { this.judgeResults = judgeResults; }
    public void setExecutionTimeMs(Long executionTimeMs)        { this.executionTimeMs = executionTimeMs; }
    public void setSubmittedAt(LocalDateTime submittedAt)       { this.submittedAt = submittedAt; }

    // ── Builder ───────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long submissionId;
        private Long problemId;
        private String studentEmail;
        private String batchId;
        private String language;
        private ExecutionStatus executionStatus;
        private Integer totalMarks;
        private Integer marksObtained;
        private Integer testCasesPassed;
        private Integer totalTestCases;
        private String overallVerdict;
        private List<JudgeResult> judgeResults;
        private Long executionTimeMs;
        private LocalDateTime submittedAt;

        public Builder submissionId(Long submissionId)              { this.submissionId = submissionId; return this; }
        public Builder problemId(Long problemId)                    { this.problemId = problemId; return this; }
        public Builder studentEmail(String studentEmail)            { this.studentEmail = studentEmail; return this; }
        public Builder batchId(String batchId)                      { this.batchId = batchId; return this; }
        public Builder language(String language)                    { this.language = language; return this; }
        public Builder executionStatus(ExecutionStatus status)      { this.executionStatus = status; return this; }
        public Builder totalMarks(Integer totalMarks)               { this.totalMarks = totalMarks; return this; }
        public Builder marksObtained(Integer marksObtained)         { this.marksObtained = marksObtained; return this; }
        public Builder testCasesPassed(Integer testCasesPassed)     { this.testCasesPassed = testCasesPassed; return this; }
        public Builder totalTestCases(Integer totalTestCases)       { this.totalTestCases = totalTestCases; return this; }
        public Builder overallVerdict(String overallVerdict)        { this.overallVerdict = overallVerdict; return this; }
        public Builder judgeResults(List<JudgeResult> judgeResults) { this.judgeResults = judgeResults; return this; }
        public Builder executionTimeMs(Long executionTimeMs)        { this.executionTimeMs = executionTimeMs; return this; }
        public Builder submittedAt(LocalDateTime submittedAt)       { this.submittedAt = submittedAt; return this; }

        public SubmissionJudgeResponse build() { return new SubmissionJudgeResponse(this); }
    }
}