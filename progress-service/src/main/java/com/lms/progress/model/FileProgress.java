
package com.lms.progress.model;
 
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
 
@Entity
@Table(name = "file_progress")
public class FileProgress {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    // student who downloaded/previewed
    private String studentEmail;
 
    // batch the file belongs to (files belong to batch — NOT course)
    private Long batchId;
 
    // list of file IDs student has downloaded/previewed
    @ElementCollection
    @CollectionTable(name = "downloaded_file_ids", joinColumns = @JoinColumn(name = "file_progress_id"))
    @Column(name = "file_id")
    private List<Long> downloadedFileIds;
 
    // total files in this batch
    private int totalFileCount;
 
    // 0.0 to 100.0
    private double downloadPercentage;
 
    private Instant updatedAt;
 
    // ── getters & setters ──
 
    public Long getId() { return id; }
 
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
 
