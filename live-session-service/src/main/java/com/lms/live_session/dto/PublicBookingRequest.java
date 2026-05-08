//package com.lms.live_session.dto;
//
//public class PublicBookingRequest {
//    private Long sessionId;
//    private String fullName;
//    private String email;
//    private String phoneNumber;
//    private String country;
//    private Boolean gdprConsent;
//
//    public Long getSessionId() { return sessionId; }
//    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
//
//    public String getFullName() { return fullName; }
//    public void setFullName(String fullName) { this.fullName = fullName; }
//
//    public String getEmail() { return email; }
//    public void setEmail(String email) { this.email = email; }
//
//    public String getPhoneNumber() { return phoneNumber; }
//    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
//
//    public String getCountry() { return country; }
//    public void setCountry(String country) { this.country = country; }
//
//    public Boolean getGdprConsent() { return gdprConsent; }
//    public void setGdprConsent(Boolean gdprConsent) { this.gdprConsent = gdprConsent; }
//}
package com.lms.live_session.dto;

public class PublicBookingRequest {

    private Long sessionId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String country;
    private Boolean gdprConsent;

    // ✅ NEW CUSTOM FIELDS
    private String topicsOfInterest;
    private String jobRole;
    private String howDidYouHear;
    private String learningGoal;

    // ── Getters & Setters ─────────────────────────────────────────────

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public Boolean getGdprConsent() { return gdprConsent; }
    public void setGdprConsent(Boolean gdprConsent) { this.gdprConsent = gdprConsent; }

    public String getTopicsOfInterest() { return topicsOfInterest; }
    public void setTopicsOfInterest(String topicsOfInterest) { this.topicsOfInterest = topicsOfInterest; }

    public String getJobRole() { return jobRole; }
    public void setJobRole(String jobRole) { this.jobRole = jobRole; }

    public String getHowDidYouHear() { return howDidYouHear; }
    public void setHowDidYouHear(String howDidYouHear) { this.howDidYouHear = howDidYouHear; }

    public String getLearningGoal() { return learningGoal; }
    public void setLearningGoal(String learningGoal) { this.learningGoal = learningGoal; }
}