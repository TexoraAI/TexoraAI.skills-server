package com.lms.video.dto;

public class WatchNowStatsDTO {

    private long totalCourses;
    private long publishedCourses;
    private long draftCourses;
    private long totalLearners;

    public WatchNowStatsDTO() {
    }

    public WatchNowStatsDTO(long totalCourses,
                            long publishedCourses,
                            long draftCourses,
                            long totalLearners) {
        this.totalCourses = totalCourses;
        this.publishedCourses = publishedCourses;
        this.draftCourses = draftCourses;
        this.totalLearners = totalLearners;
    }

    public long getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(long totalCourses) {
        this.totalCourses = totalCourses;
    }

    public long getPublishedCourses() {
        return publishedCourses;
    }

    public void setPublishedCourses(long publishedCourses) {
        this.publishedCourses = publishedCourses;
    }

    public long getDraftCourses() {
        return draftCourses;
    }

    public void setDraftCourses(long draftCourses) {
        this.draftCourses = draftCourses;
    }

    public long getTotalLearners() {
        return totalLearners;
    }

    public void setTotalLearners(long totalLearners) {
        this.totalLearners = totalLearners;
    }
}