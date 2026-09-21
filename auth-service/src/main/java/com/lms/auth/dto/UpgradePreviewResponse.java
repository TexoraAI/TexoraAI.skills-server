package com.lms.auth.dto;
import java.time.LocalDate;

public class UpgradePreviewResponse {
    private String orgId;
    private String currentPlan;
    private String targetPlan;
    private int price;
    private boolean valid;
    private String reason;
    private int durationMonths;
    private LocalDate expiresAt;

    public UpgradePreviewResponse() {}

    public UpgradePreviewResponse(String orgId, String currentPlan, String targetPlan,
                                   int price, boolean valid, String reason) {
        this.orgId = orgId;
        this.currentPlan = currentPlan;
        this.targetPlan = targetPlan;
        this.price = price;
        this.valid = valid;
        this.reason = reason;
    }

    public UpgradePreviewResponse(String orgId, String currentPlan, String targetPlan,
                                   int price, boolean valid, String reason,
                                   int durationMonths, LocalDate expiresAt) {
        this.orgId = orgId;
        this.currentPlan = currentPlan;
        this.targetPlan = targetPlan;
        this.price = price;
        this.valid = valid;
        this.reason = reason;
        this.durationMonths = durationMonths;
        this.expiresAt = expiresAt;
    }

    public String getOrgId() { return orgId; }
    public void setOrgId(String orgId) { this.orgId = orgId; }
    public String getCurrentPlan() { return currentPlan; }
    public void setCurrentPlan(String currentPlan) { this.currentPlan = currentPlan; }
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