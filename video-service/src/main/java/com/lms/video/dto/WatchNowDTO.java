
package com.lms.video.dto;

public class WatchNowDTO {

    private String quote;
    private String externalVideoUrl;
    private String personName;
    private String personRole;
    private String status;
    private int sortOrder;

    // ===== GETTERS & SETTERS =====

    public String getQuote() { return quote; }
    public void setQuote(String quote) { this.quote = quote; }

    public String getExternalVideoUrl() { return externalVideoUrl; }
    public void setExternalVideoUrl(String externalVideoUrl) { this.externalVideoUrl = externalVideoUrl; }

    public String getPersonName() { return personName; }
    public void setPersonName(String personName) { this.personName = personName; }

    public String getPersonRole() { return personRole; }
    public void setPersonRole(String personRole) { this.personRole = personRole; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}