package com.lms.chat.dto;

import com.lms.chat.entity.Feedback;
import java.time.LocalDateTime;
import java.util.List;

public class FeedbackResponse {

    private Long id;
    private Long batchId;
    private String studentEmail;   // null if anonymous and caller is trainer
    private String trainerEmail;

    private String moodRating;
    private Integer trainerClarityRating;
    private Integer trainerDoubtClearingRating;
    private Integer trainerEnergyRating;
    private Integer trainerTechnicalDepthRating;

    private List<String> contentTags;
    private List<String> improvementTags;

    private String comment;
    private boolean anonymous;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FeedbackResponse from(Feedback f, boolean callerIsTrainer) {
        FeedbackResponse r = new FeedbackResponse();
        r.id           = f.getId();
        r.batchId      = f.getBatchId();
        r.trainerEmail = f.getTrainerEmail();
        r.moodRating   = f.getMoodRating() != null ? f.getMoodRating().name() : null;
        r.trainerClarityRating        = f.getTrainerClarityRating();
        r.trainerDoubtClearingRating  = f.getTrainerDoubtClearingRating();
        r.trainerEnergyRating         = f.getTrainerEnergyRating();
        r.trainerTechnicalDepthRating = f.getTrainerTechnicalDepthRating();
        r.contentTags     = f.getContentTags();
        r.improvementTags = f.getImprovementTags();
        r.comment         = f.getComment();
        r.anonymous       = f.isAnonymous();
        r.status          = f.getStatus() != null ? f.getStatus().name() : null;
        r.createdAt       = f.getCreatedAt();
        r.updatedAt       = f.getUpdatedAt();
        // Hide student identity from trainer if anonymous
        r.studentEmail = (f.isAnonymous() && callerIsTrainer) ? null : f.getStudentEmail();
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}