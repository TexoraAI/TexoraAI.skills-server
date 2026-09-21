package com.lms.auth.dto;

import java.time.LocalDate;

public class ResumePlanPreviewResponse {
    private Long userId;
    private String currentResumePlan;
    private String targetPlan;
    private int price;
    private boolean valid;
    private String reason;
    private int durationMonths;
    private LocalDate expiresAt;

    public ResumePlanPreviewResponse() {}

    public ResumePlanPreviewResponse(Long userId, String currentResumePlan, String targetPlan,
                                      int price, boolean valid, String reason) {
        this.userId = userId;
        this.currentResumePlan = currentResumePlan;
        this.targetPlan = targetPlan;
        this.price = price;
        this.valid = valid;
        this.reason = reason;
    }

    public ResumePlanPreviewResponse(Long userId, String currentResumePlan, String targetPlan,
                                      int price, boolean valid, String reason,
                                      int durationMonths, LocalDate expiresAt) {
        this.userId = userId;
        this.currentResumePlan = currentResumePlan;
        this.targetPlan = targetPlan;
        this.price = price;
        this.valid = valid;
        this.reason = reason;
        this.durationMonths = durationMonths;
        this.expiresAt = expiresAt;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getCurrentResumePlan() { return currentResumePlan; }
    public void setCurrentResumePlan(String currentResumePlan) { this.currentResumePlan = currentResumePlan; }
    public String getTargetPlan() { return targetPlan; }
    public void setTargetPlan(String targetPlan) { this.targetPlan = targetPlan; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public int getDurationMonths() { return durationMonths; }
    public void setDurationMonths(int durationMonths) { this.durationMonths = durationMonths; }
    public LocalDate getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDate expiresAt) { this.expiresAt = expiresAt; }
}