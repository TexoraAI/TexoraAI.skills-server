package com.lms.video.dto;

public class WatchNowStatsDTO {

    private long totalStories;
    private long publishedStories;
    private long draftStories;
    private long uploadedVideos;
    private long externalVideos;

    public WatchNowStatsDTO() {}

    public WatchNowStatsDTO(long totalStories, long publishedStories, long draftStories,
                             long uploadedVideos, long externalVideos) {
        this.totalStories = totalStories;
        this.publishedStories = publishedStories;
        this.draftStories = draftStories;
        this.uploadedVideos = uploadedVideos;
        this.externalVideos = externalVideos;
    }

    public long getTotalStories() { return totalStories; }
    public void setTotalStories(long totalStories) { this.totalStories = totalStories; }

    public long getPublishedStories() { return publishedStories; }
    public void setPublishedStories(long publishedStories) { this.publishedStories = publishedStories; }

    public long getDraftStories() { return draftStories; }
    public void setDraftStories(long draftStories) { this.draftStories = draftStories; }

    public long getUploadedVideos() { return uploadedVideos; }
    public void setUploadedVideos(long uploadedVideos) { this.uploadedVideos = uploadedVideos; }

    public long getExternalVideos() { return externalVideos; }
    public void setExternalVideos(long externalVideos) { this.externalVideos = externalVideos; }
}