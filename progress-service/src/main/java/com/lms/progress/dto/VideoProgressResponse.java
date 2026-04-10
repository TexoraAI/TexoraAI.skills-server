package com.lms.progress.dto;
 
import java.time.Instant;
import java.util.List;
 
public class VideoProgressResponse {
 
    private Long   progressId;
    private String studentEmail;
    private Long   batchId;               // batch — NOT courseId
    private List<Long> watchedVideoIds;   // which videos watched
    private int    totalVideoCount;       // total videos in batch
    private double watchPercentage;       // 0.0 - 100.0
    private Instant updatedAt;
 
    // ── getters & setters ──
 
    public Long getProgressId() { return progressId; }
    public void setProgressId(Long progressId) { this.progressId = progressId; }
 
    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
 
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
 
    public List<Long> getWatchedVideoIds() { return watchedVideoIds; }
    public void setWatchedVideoIds(List<Long> watchedVideoIds) { this.watchedVideoIds = watchedVideoIds; }
 
    public int getTotalVideoCount() { return totalVideoCount; }
    public void setTotalVideoCount(int totalVideoCount) { this.totalVideoCount = totalVideoCount; }
 
    public double getWatchPercentage() { return watchPercentage; }
    public void setWatchPercentage(double watchPercentage) { this.watchPercentage = watchPercentage; }
 
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}