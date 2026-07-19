package com.lms.assessment.dto;

public class QuizAdminReportResponse {

    private Long id;
    private String title;
    private String trainerEmail;
    private Long batchId;
    private String quizType;
    private String difficulty;
    private Integer totalMarks;
    private int questionCount;
    private long attemptCount;

    // Nullable — only populated (with "Standalone" fallback) for the /superadmin endpoint
    private String organizationId;

    public QuizAdminReportResponse() {
    }

    public QuizAdminReportResponse(Long id, String title, String trainerEmail, Long batchId,
                                    String quizType, String difficulty, Integer totalMarks,
                                    int questionCount, long attemptCount, String organizationId) {
        this.id = id;
        this.title = title;
        this.trainerEmail = trainerEmail;
        this.batchId = batchId;
        this.quizType = quizType;
        this.difficulty = difficulty;
        this.totalMarks = totalMarks;
        this.questionCount = questionCount;
        this.attemptCount = attemptCount;
        this.organizationId = organizationId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getQuizType() { return quizType; }
    public void setQuizType(String quizType) { this.quizType = quizType; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }

    public int getQuestionCount() { return questionCount; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }

    public long getAttemptCount() { return attemptCount; }
    public void setAttemptCount(long attemptCount) { this.attemptCount = attemptCount; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
}