package com.lms.course.dto;

/**
 * Payload for PATCH /api/banners/{id}/publish and schedule actions
 * (PublishCard.jsx "Publish Now" / "Confirm schedule" buttons).
 */
public class BannerStudioStatusUpdateDTO {

    /** draft | scheduled | active | expired */
    private String status;

    private String startDate; // yyyy-MM-dd, used when scheduling
    private String startTime; // HH:mm, used when scheduling

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
}