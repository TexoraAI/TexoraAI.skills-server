package com.lms.course.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "syllabus_sessions")
public class SyllabusSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private SyllabusModule module;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionType type;

    private String duration;

    private Integer orderIndex;

    private String videoId;
    @Column(columnDefinition = "TEXT")
    private String videoTitle;

    @Column(columnDefinition = "TEXT")
    private String videoDescription;
    @Column(columnDefinition = "TEXT")
    private String videoUrl;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String videoThumbnailUrl;

    @Column(nullable = true)
    private Integer videoDurationSeconds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionVideoStatus videoStatus = SessionVideoStatus.NONE;

    private Instant videoStatusUpdatedAt;

    // ── NEW: Reading-session file feature (additive, mirrors video fields) ──
    private String fileId;

    @Column(columnDefinition = "TEXT")
    private String fileUrl;

    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionFileStatus fileStatus = SessionFileStatus.NONE;

    public SyllabusSession() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SyllabusModule getModule() {
        return module;
    }

    public void setModule(SyllabusModule module) {
        this.module = module;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public SessionType getType() {
        return type;
    }

    public void setType(SessionType type) {
        this.type = type;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoThumbnailUrl() {
        return videoThumbnailUrl;
    }

    public void setVideoThumbnailUrl(String videoThumbnailUrl) {
        this.videoThumbnailUrl = videoThumbnailUrl;
    }

    public Integer getVideoDurationSeconds() {
        return videoDurationSeconds;
    }

    public void setVideoDurationSeconds(Integer videoDurationSeconds) {
        this.videoDurationSeconds = videoDurationSeconds;
    }

    public SessionVideoStatus getVideoStatus() {
        return videoStatus;
    }

    public void setVideoStatus(SessionVideoStatus videoStatus) {
        this.videoStatus = videoStatus;
    }

    public Instant getVideoStatusUpdatedAt() {
        return videoStatusUpdatedAt;
    }

    public void setVideoStatusUpdatedAt(Instant videoStatusUpdatedAt) {
        this.videoStatusUpdatedAt = videoStatusUpdatedAt;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getVideoDescription() {
        return videoDescription;
    }

    public void setVideoDescription(String videoDescription) {
        this.videoDescription = videoDescription;
    }

    // ── NEW getters/setters for file fields ──
    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public SessionFileStatus getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(SessionFileStatus fileStatus) {
        this.fileStatus = fileStatus;
    }
}