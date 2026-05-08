package com.lms.progress.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "skill_scores",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_email","batch_id","skill_name"}))
public class SkillScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── identifiers (same pattern as VideoProgress) ──
    @Column(name = "student_email", nullable = false)
    private String studentEmail;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    // trainer who owns this batch (for trainer-view queries)
    @Column(name = "trainer_email")
    private String trainerEmail;

    // e.g. "JavaScript", "React", "SQL", "Python", "CSS"
    @Column(name = "skill_name", nullable = false)
    private String skillName;

    // sub-scores (0-100)
    @Column(name = "quiz_score")
    private double quizScore;          // from QuizProgress

    @Column(name = "assignment_score")
    private double assignmentScore;    // from AssignmentProgress

    @Column(name = "video_score")
    private double videoScore;         // from VideoProgress watchPercentage

    // weighted overall:  quiz 40% + assignment 40% + video 20%
    @Column(name = "overall_score")
    private double overallScore;

    // convenience flags (derived, stored for fast querying)
    @Column(name = "is_weak")
    private boolean isWeak;            // overallScore < 50

    @Column(name = "is_strong")
    private boolean isStrong;          // overallScore >= 70

    @Column(name = "updated_at")
    private Instant updatedAt;

    // ── getters & setters ──

    public Long getId() { return id; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public double getQuizScore() { return quizScore; }
    public void setQuizScore(double quizScore) { this.quizScore = quizScore; }

    public double getAssignmentScore() { return assignmentScore; }
    public void setAssignmentScore(double assignmentScore) { this.assignmentScore = assignmentScore; }

    public double getVideoScore() { return videoScore; }
    public void setVideoScore(double videoScore) { this.videoScore = videoScore; }

    public double getOverallScore() { return overallScore; }
    public void setOverallScore(double overallScore) { this.overallScore = overallScore; }

    public boolean isWeak() { return isWeak; }
    public void setWeak(boolean weak) { isWeak = weak; }

    public boolean isStrong() { return isStrong; }
    public void setStrong(boolean strong) { isStrong = strong; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}