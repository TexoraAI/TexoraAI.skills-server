//
//
//
//package com.lms.video.model;
//
//import jakarta.persistence.*;
//import java.time.Instant;
//
//@Entity
//@Table(name = "videos")
//public class Video {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    // ✅ NEW FIELDS
//    @Column(nullable = false)
//    private String title;
//
//    @Column(length = 1000)
//    private String description;
//
//    private String originalFileName;
//
//    private String storedFileName;
//
//    private String videoUrl;
//
//    private long size;
//
//    @Column(name = "uploaded_at")
//    private Instant uploadedAt = Instant.now();
//
//    public Video() {
//    }
//
//    // ---------- Getters & Setters ----------
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public String getOriginalFileName() {
//        return originalFileName;
//    }
//
//    public void setOriginalFileName(String originalFileName) {
//        this.originalFileName = originalFileName;
//    }
//
//    public String getStoredFileName() {
//        return storedFileName;
//    }
//
//    public void setStoredFileName(String storedFileName) {
//        this.storedFileName = storedFileName;
//    }
//
//    public String getVideoUrl() {
//        return videoUrl;
//    }
//
//    public void setVideoUrl(String videoUrl) {
//        this.videoUrl = videoUrl;
//    }
//
//    public long getSize() {
//        return size;
//    }
//
//    public void setSize(long size) {
//        this.size = size;
//    }
//
//    public Instant getUploadedAt() {
//        return uploadedAt;
//    }
//
//    public void setUploadedAt(Instant uploadedAt) {
//        this.uploadedAt = uploadedAt;
//    }
//}





package com.lms.video.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "videos")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = true) // Set to true first
    private Long batchId;
    
    // ✅ NEW FIELDS
    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    private String originalFileName;

    private String storedFileName;

    private String videoUrl;

    private long size;

    private String uploadedBy;

    
    @Column(name = "uploaded_at")
    private Instant uploadedAt = Instant.now();

    public Video() {
    }

    // ---------- Getters & Setters ----------
   public String UploadedBy()
   {
	   return uploadedBy;
   }
   public void setUploadedBy(String uploadedBy) {
	    this.uploadedBy = uploadedBy;
	}
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}