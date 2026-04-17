package com.lms.chat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "student_email", nullable = false)
    private String studentEmail;

    @Column(name = "trainer_email", nullable = false)
    private String trainerEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "mood_rating", nullable = false)
    private MoodRating moodRating;

    @Column(name = "trainer_clarity_rating")
    private Integer trainerClarityRating;

    @Column(name = "trainer_doubt_clearing_rating")
    private Integer trainerDoubtClearingRating;

    @Column(name = "trainer_energy_rating")
    private Integer trainerEnergyRating;

    @Column(name = "trainer_technical_depth_rating")
    private Integer trainerTechnicalDepthRating;

    @ElementCollection
    @CollectionTable(name = "feedback_content_tags",
                     joinColumns = @JoinColumn(name = "feedback_id"))
    @Column(name = "tag")
    private List<String> contentTags;

    @ElementCollection
    @CollectionTable(name = "feedback_improvement_tags",
                     joinColumns = @JoinColumn(name = "feedback_id"))
    @Column(name = "tag")
    private List<String> improvementTags;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "is_anonymous", nullable = false)
    private boolean anonymous;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FeedbackStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = FeedbackStatus.SUBMITTED;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum MoodRating { POOR, OKAY, FINE, GOOD, AMAZING }
    public enum FeedbackStatus { SUBMITTED, REVIEWED, ARCHIVED }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public MoodRating getMoodRating() { return moodRating; }
    public void setMoodRating(MoodRating moodRating) { this.moodRating = moodRating; }

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

    public FeedbackStatus getStatus() { return status; }
    public void setStatus(FeedbackStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}