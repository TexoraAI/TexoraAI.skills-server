package com.lms.progress.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * One row per resource (video/article/pdf/quiz) inside a module.
 *
 * Note: quizContentJson is not in the original field list from the spec's
 * entity section, but section 6 explicitly calls for "a new quizContentJson
 * field on RoadmapUpgradedResource" to store the raw OpenAI-generated quiz
 * JSON (questions + correct answers) when type = QUIZ. Added here so the
 * quiz generation flow has somewhere to persist it.
 *
 * contentBody follows that exact same pattern: it stores the full
 * AI-generated article body (400-800 words of plain text) when
 * type = ARTICLE, so the frontend can render a real in-page reader instead
 * of just a title. Not in the original spec either - added for the same
 * reason quizContentJson was.
 *
 * pdfContent follows the same pattern again: it stores a real generated
 * PDF document when type = PDF, so the frontend can display an actual
 * document instead of just a title suggestion. It is Base64-encoded PDF
 * bytes stored as plain TEXT rather than a byte[]/@Lob column, because
 * @Lob/binary large object columns aren't supported on this project's
 * database setup. The bytes are decoded back to raw binary only when
 * actually serving the file, in RoadmapUpgradedService.getResourcePdf().
 */
@Entity
@Table(name = "roadmap_upgraded_resource")
public class RoadmapUpgradedResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "module_id")
//    private RoadmapUpgradedModule module;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private RoadmapUpgradedTopic topic;

    private String type;

    private String title;

    private String sourceUrl;

    private String filePath;

    private String durationOrLength;

    private Boolean completed;

    private Integer quizScore;

    private LocalDateTime completedAt;

    @Column(columnDefinition = "TEXT")
    private String quizContentJson;

    @Column(columnDefinition = "TEXT")
    private String contentBody;

    // Base64-encoded PDF bytes, stored as plain TEXT (byte[]/@Lob is not
    // supported on this DB setup). Only populated when type = PDF. See
    // RoadmapUpgradedService.generatePdfResource() (writer) and
    // RoadmapUpgradedService.getResourcePdf() (reader/decoder).
    @Column(columnDefinition = "TEXT")
    private String pdfContent;

    public RoadmapUpgradedResource() {
    }

    public RoadmapUpgradedResource(Long id,
    		RoadmapUpgradedTopic topic,
                                    String type,
                                    String title,
                                    String sourceUrl,
                                    String filePath,
                                    String durationOrLength,
                                    Boolean completed,
                                    Integer quizScore,
                                    LocalDateTime completedAt,
                                    String quizContentJson,
                                    String contentBody,
                                    String pdfContent) {
        this.id = id;
        this.topic = topic;
        this.type = type;
        this.title = title;
        this.sourceUrl = sourceUrl;
        this.filePath = filePath;
        this.durationOrLength = durationOrLength;
        this.completed = completed;
        this.quizScore = quizScore;
        this.completedAt = completedAt;
        this.quizContentJson = quizContentJson;
        this.contentBody = contentBody;
        this.pdfContent = pdfContent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoadmapUpgradedTopic getTopic() { return topic; }
    public void setTopic(RoadmapUpgradedTopic topic) { this.topic = topic; }

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

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
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

    public String getPdfContent() {
        return pdfContent;
    }

    public void setPdfContent(String pdfContent) {
        this.pdfContent = pdfContent;
    }
}