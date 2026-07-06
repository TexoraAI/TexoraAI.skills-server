package com.lms.course.dto;

public class MentorFeedbackStatsResponse {

    private long total;
    private long active;
    private long inactive;
    private long featured;
    private double avgRating;

    public MentorFeedbackStatsResponse() {
    }

    public MentorFeedbackStatsResponse(long total, long active, long inactive, long featured, double avgRating) {
        this.total = total;
        this.active = active;
        this.inactive = inactive;
        this.featured = featured;
        this.avgRating = avgRating;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getActive() {
        return active;
    }

    public void setActive(long active) {
        this.active = active;
    }

    public long getInactive() {
        return inactive;
    }

    public void setInactive(long inactive) {
        this.inactive = inactive;
    }

    public long getFeatured() {
        return featured;
    }

    public void setFeatured(long featured) {
        this.featured = featured;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }
}