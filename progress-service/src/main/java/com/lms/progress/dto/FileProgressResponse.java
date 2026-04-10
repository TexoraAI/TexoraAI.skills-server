package com.lms.progress.dto;
 
import java.time.Instant;
import java.util.List;
 
public class FileProgressResponse {
 
    private Long   progressId;
    private String studentEmail;
    private Long   batchId;                  // batch — NOT courseId
    private List<Long> downloadedFileIds;    // which files downloaded/previewed
    private int    totalFileCount;           // total files in batch
    private double downloadPercentage;       // 0.0 - 100.0
    private Instant updatedAt;
 
    // ── getters & setters ──
 
    public Long getProgressId() { return progressId; }
    public void setProgressId(Long progressId) { this.progressId = progressId; }
 
    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
 
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
 
    public List<Long> getDownloadedFileIds() { return downloadedFileIds; }
    public void setDownloadedFileIds(List<Long> downloadedFileIds) { this.downloadedFileIds = downloadedFileIds; }
 
    public int getTotalFileCount() { return totalFileCount; }
    public void setTotalFileCount(int totalFileCount) { this.totalFileCount = totalFileCount; }
 
    public double getDownloadPercentage() { return downloadPercentage; }
    public void setDownloadPercentage(double downloadPercentage) { this.downloadPercentage = downloadPercentage; }
 
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}