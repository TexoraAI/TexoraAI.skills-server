
package com.lms.progress.model;
 
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
 
@Entity
@Table(name = "video_progress")
public class VideoProgress {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    // student who watched
    private String studentEmail;
 
    // batch the video belongs to (videos belong to batch, NOT course)
    private Long batchId;
 
    // list of video IDs student has watched
    @ElementCollection
    @CollectionTable(name = "watched_video_ids", joinColumns = @JoinColumn(name = "video_progress_id"))
    @Column(name = "video_id")
    private List<Long> watchedVideoIds;
 
    // total videos in this batch
    private int totalVideoCount;
 
    // 0.0 to 100.0
    private double watchPercentage;
 
    private Instant updatedAt;
 
    // ── getters & setters ──
 
    public Long getId() { return id; }
 
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