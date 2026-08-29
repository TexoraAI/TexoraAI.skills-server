package com.lms.progress.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "roadmap_video_cache",
       uniqueConstraints = @UniqueConstraint(columnNames = "search_key"))
public class RoadmapUpgradedVideoCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "search_key", nullable = false, length = 512)
    private String searchKey;

    @Column(name = "video_id", length = 32)
    private String videoId; // null = known "no results" (cached negative)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSearchKey() { return searchKey; }
    public void setSearchKey(String searchKey) { this.searchKey = searchKey; }
    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}