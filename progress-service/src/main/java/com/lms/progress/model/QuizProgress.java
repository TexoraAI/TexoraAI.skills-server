package com.lms.progress.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "quiz_progress")
public class QuizProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentEmail;
    private Long batchId;
    // 🔥 FIX HERE (EAGER)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "completed_quiz_ids",
            joinColumns = @JoinColumn(name = "quiz_progress_id"))
    @Column(name = "quiz_id")
    private List<Long> completedQuizIds;

    private int totalQuizzes;
    private double percentage;
    private Instant updatedAt;

    // ===== GETTERS & SETTERS =====

    public Long getId() { return id; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public List<Long> getCompletedQuizIds() { return completedQuizIds; }
    public void setCompletedQuizIds(List<Long> completedQuizIds) { this.completedQuizIds = completedQuizIds; }

    public int getTotalQuizzes() { return totalQuizzes; }
    public void setTotalQuizzes(int totalQuizzes) { this.totalQuizzes = totalQuizzes; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}