package com.lms.chat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback_summary")
public class FeedbackSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trainer_email", nullable = false)
    private String trainerEmail;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "total_feedback_count", nullable = false)
    private int totalFeedbackCount;

    @Column(name = "avg_mood_score")
    private Double avgMoodScore;

    @Column(name = "avg_clarity_rating")
    private Double avgClarityRating;

    @Column(name = "avg_doubt_clearing_rating")
    private Double avgDoubtClearingRating;

    @Column(name = "avg_energy_rating")
    private Double avgEnergyRating;

    @Column(name = "avg_technical_depth_rating")
    private Double avgTechnicalDepthRating;

    @Column(name = "overall_avg_rating")
    private Double overallAvgRating;

    @Column(name = "last_computed_at")
    private LocalDateTime lastComputedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() { this.lastComputedAt = LocalDateTime.now(); }

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