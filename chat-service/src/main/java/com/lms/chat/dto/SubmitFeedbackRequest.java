package com.lms.chat.dto;

import java.util.List;

public class SubmitFeedbackRequest {

    private Long batchId;
    private String studentEmail;   // resolved from JWT in service, but kept for internal routing
    private String trainerEmail;   // passed by frontend from context

    private String moodRating;     // POOR | OKAY | FINE | GOOD | AMAZING

    private Integer trainerClarityRating;
    private Integer trainerDoubtClearingRating;
    private Integer trainerEnergyRating;
    private Integer trainerTechnicalDepthRating;

    private List<String> contentTags;
    private List<String> improvementTags;

    private String comment;
    private boolean anonymous;

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public String getMoodRating() { return moodRating; }
    public void setMoodRating(String moodRating) { this.moodRating = moodRating; }

    public Integer getTrainerClarityRating() { return trainerClarityRating; }
    public void setTrainerClarityRating(Integer v) { this.trainerClarityRating = v; }

    public Integer getTrainerDoubtClearingRating() { return trainerDoubtClearingRating; }
    public void setTrainerDoubtClearingRating(Integer v) { this.trainerDoubtClearingRating = v; }

    public Integer getTrainerEnergyRating() { return trainerEnergyRating; }
    public void setTrainerEnergyRating(Integer v) { this.trainerEnergyRating = v; }

    public Integer getTrainerTechnicalDepthRating() { return trainerTechnicalDepthRating; }
    public void setTrainerTechnicalDepthRating(Integer v) { this.trainerTechnicalDepthRating = v; }

    public List<String> getContentTags() { return contentTags; }
    public void setContentTags(List<String> contentTags) { this.contentTags = contentTags; }

    public List<String> getImprovementTags() { return improvementTags; }
    public void setImprovementTags(List<String> improvementTags) { this.improvementTags = improvementTags; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }
}