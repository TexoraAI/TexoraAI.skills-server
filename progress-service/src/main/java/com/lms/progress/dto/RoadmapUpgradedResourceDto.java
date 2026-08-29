package com.lms.progress.dto;

public class RoadmapUpgradedResourceDto {

    private Long id;
    private String type;
    private String title;
    private String sourceUrl;
    private String filePath;
    private String durationOrLength;
    private Boolean completed;
    private Integer quizScore;
    // Raw OpenAI-generated quiz JSON: {"questions":[{"question","options","correctOptionIndex"}]}
    // Only populated when type = QUIZ. Needed so the frontend can render real
    // questions instead of asking the user to type in their own score.
    private String quizContentJson;
    // Full AI-generated article body (plain text, 400-800 words). Only
    // populated when type = ARTICLE. Needed so the frontend can render a
    // real in-page reader instead of just a title.
    private String contentBody;
    // True when a real PDF has been rendered and stored for this resource
    // (type = PDF only). Lets the frontend decide whether to open PdfModal
    // vs. fall back to the plain "Mark done" row, without shipping the
    // (Base64-encoded) PDF text itself in every roadmap fetch - the actual
    // bytes come from the dedicated GET /resource/{id}/pdf endpoint instead.
    private Boolean hasPdf;

    public RoadmapUpgradedResourceDto() {
    }

    public RoadmapUpgradedResourceDto(Long id,
                                       String type,
                                       String title,
                                       String sourceUrl,
                                       String filePath,
                                       String durationOrLength,
                                       Boolean completed,
                                       Integer quizScore,
                                       String quizContentJson,
                                       String contentBody,
                                       Boolean hasPdf) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.sourceUrl = sourceUrl;
        this.filePath = filePath;
        this.durationOrLength = durationOrLength;
        this.completed = completed;
        this.quizScore = quizScore;
        this.quizContentJson = quizContentJson;
        this.contentBody = contentBody;
        this.hasPdf = hasPdf;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDurationOrLength() {
        return durationOrLength;
    }

    public void setDurationOrLength(String durationOrLength) {
        this.durationOrLength = durationOrLength;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Integer getQuizScore() {
        return quizScore;
    }

    public void setQuizScore(Integer quizScore) {
        this.quizScore = quizScore;
    }

    public String getQuizContentJson() {
        return quizContentJson;
    }

    public void setQuizContentJson(String quizContentJson) {
        this.quizContentJson = quizContentJson;
    }

    public String getContentBody() {
        return contentBody;
    }

    public void setContentBody(String contentBody) {
        this.contentBody = contentBody;
    }

    public Boolean getHasPdf() {
        return hasPdf;
    }

    public void setHasPdf(Boolean hasPdf) {
        this.hasPdf = hasPdf;
    }
}