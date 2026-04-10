package com.lms.progress.dto;

import java.time.Instant;
import java.util.List;

public class QuizProgressResponse {

    private Long id;
    private String studentEmail;
    private Long batchId;
    private List<Long> completedQuizIds;
    private int totalQuizzes;
    private double percentage;
    private Instant updatedAt;

    // ===== GETTERS & SETTERS =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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