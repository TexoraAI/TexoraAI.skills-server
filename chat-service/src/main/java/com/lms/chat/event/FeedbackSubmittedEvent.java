package com.lms.chat.event;

import java.time.LocalDateTime;
import java.util.List;

public class FeedbackSubmittedEvent {

    private Long feedbackId;
    private Long batchId;
    private Long studentId;   // null if anonymous
    private Long trainerId;
    private Long sessionId;

    private String moodRating;
    private Integer trainerClarityRating;
    private Integer trainerDoubtClearingRating;
    private Integer trainerEnergyRating;
    private Integer trainerTechnicalDepthRating;

    private List<String> contentTags;
    private List<String> improvementTags;

    private boolean anonymous;
    private LocalDateTime submittedAt;

    // ── Getters & Setters ──────────────────────────────────────────

    public Long getFeedbackId() { return feedbackId; }
    public void setFeedbackId(Long feedbackId) { this.feedbackId = feedbackId; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getTrainerId() { return trainerId; }
    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getMoodRating() { return moodRating; }
    public void setMoodRating(String moodRating) { this.moodRating = moodRating; }

    public Integer getTrainerClarityRating() { return trainerClarityRating; }
    public void setTrainerClarityRating(Integer trainerClarityRating) { this.trainerClarityRating = trainerClarityRating; }

    public Integer getTrainerDoubtClearingRating() { return trainerDoubtClearingRating; }
    public void setTrainerDoubtClearingRating(Integer trainerDoubtClearingRating) { this.trainerDoubtClearingRating = trainerDoubtClearingRating; }

    public Integer getTrainerEnergyRating() { return trainerEnergyRating; }
    public void setTrainerEnergyRating(Integer trainerEnergyRating) { this.trainerEnergyRating = trainerEnergyRating; }

    public Integer getTrainerTechnicalDepthRating() { return trainerTechnicalDepthRating; }
    public void setTrainerTechnicalDepthRating(Integer trainerTechnicalDepthRating) { this.trainerTechnicalDepthRating = trainerTechnicalDepthRating; }

    public List<String> getContentTags() { return contentTags; }
    public void setContentTags(List<String> contentTags) { this.contentTags = contentTags; }

    public List<String> getImprovementTags() { return improvementTags; }
    public void setImprovementTags(List<String> improvementTags) { this.improvementTags = improvementTags; }

    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}