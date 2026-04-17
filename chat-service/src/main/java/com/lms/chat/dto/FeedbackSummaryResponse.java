package com.lms.chat.dto;

import com.lms.chat.entity.FeedbackSummary;
import java.time.LocalDateTime;

public class FeedbackSummaryResponse {

    private Long id;
    private String trainerEmail;
    private Long batchId;
    private int totalFeedbackCount;
    private Double avgMoodScore;
    private Double avgClarityRating;
    private Double avgDoubtClearingRating;
    private Double avgEnergyRating;
    private Double avgTechnicalDepthRating;
    private Double overallAvgRating;
    private LocalDateTime lastComputedAt;

    public static FeedbackSummaryResponse from(FeedbackSummary s) {
        FeedbackSummaryResponse r = new FeedbackSummaryResponse();
        r.id                     = s.getId();
        r.trainerEmail           = s.getTrainerEmail();
        r.batchId                = s.getBatchId();
        r.totalFeedbackCount     = s.getTotalFeedbackCount();
        r.avgMoodScore           = s.getAvgMoodScore();
        r.avgClarityRating       = s.getAvgClarityRating();
        r.avgDoubtClearingRating = s.getAvgDoubtClearingRating();
        r.avgEnergyRating        = s.getAvgEnergyRating();
        r.avgTechnicalDepthRating = s.getAvgTechnicalDepthRating();
        r.overallAvgRating       = s.getOverallAvgRating();
        r.lastComputedAt         = s.getLastComputedAt();
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public int getTotalFeedbackCount() { return totalFeedbackCount; }
    public void setTotalFeedbackCount(int totalFeedbackCount) { this.totalFeedbackCount = totalFeedbackCount; }

    public Double getAvgMoodScore() { return avgMoodScore; }
    public void setAvgMoodScore(Double avgMoodScore) { this.avgMoodScore = avgMoodScore; }

    public Double getAvgClarityRating() { return avgClarityRating; }
    public void setAvgClarityRating(Double avgClarityRating) { this.avgClarityRating = avgClarityRating; }

    public Double getAvgDoubtClearingRating() { return avgDoubtClearingRating; }
    public void setAvgDoubtClearingRating(Double avgDoubtClearingRating) { this.avgDoubtClearingRating = avgDoubtClearingRating; }

    public Double getAvgEnergyRating() { return avgEnergyRating; }
    public void setAvgEnergyRating(Double avgEnergyRating) { this.avgEnergyRating = avgEnergyRating; }

    public Double getAvgTechnicalDepthRating() { return avgTechnicalDepthRating; }
    public void setAvgTechnicalDepthRating(Double avgTechnicalDepthRating) { this.avgTechnicalDepthRating = avgTechnicalDepthRating; }

    public Double getOverallAvgRating() { return overallAvgRating; }
    public void setOverallAvgRating(Double overallAvgRating) { this.overallAvgRating = overallAvgRating; }

    public LocalDateTime getLastComputedAt() { return lastComputedAt; }
    public void setLastComputedAt(LocalDateTime lastComputedAt) { this.lastComputedAt = lastComputedAt; }
}