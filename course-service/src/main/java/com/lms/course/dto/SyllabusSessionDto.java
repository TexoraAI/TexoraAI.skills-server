package com.lms.course.dto;

import com.lms.course.model.SessionFileStatus;
import com.lms.course.model.SessionType;
import com.lms.course.model.SessionVideoStatus;

public class SyllabusSessionDto {

    private Long id;
    private String title;
    private SessionType type;
    private String duration;
    private Integer orderIndex;
    private String videoId;
    private String videoTitle;
    private String videoDescription;
    private String videoUrl;
    private String videoThumbnailUrl;
    private Integer videoDurationSeconds;
    private SessionVideoStatus videoStatus;

    // ── NEW: additive file fields (no changes to existing constructor,
    // so existing call sites elsewhere keep compiling unmodified) ──
    private String fileId;
    private String fileUrl;
    private String fileName;
    private SessionFileStatus fileStatus;

    private Boolean locked;

    public SyllabusSessionDto() {
    }

    public SyllabusSessionDto(Long id, String title, SessionType type, String duration, Integer orderIndex,
            String videoId, String videoTitle, String videoDescription, String videoUrl,
            String videoThumbnailUrl, Integer videoDurationSeconds,
            SessionVideoStatus videoStatus, Boolean locked) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.duration = duration;
        this.orderIndex = orderIndex;
        this.videoId = videoId;
        this.videoTitle = videoTitle;
        this.videoDescription = videoDescription;
        this.videoUrl = videoUrl;
        this.videoThumbnailUrl = videoThumbnailUrl;
        this.videoDurationSeconds = videoDurationSeconds;
        this.videoStatus = videoStatus;
        this.locked = locked;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    // ── NEW ──
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