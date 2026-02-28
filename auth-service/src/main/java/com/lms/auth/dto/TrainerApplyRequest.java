package com.lms.auth.dto;

import java.util.List;

public class TrainerApplyRequest {

    private String fullName;
    private String email;
    private String password;

    private String linkedinUrl;
    private String country;

    private List<String> platforms; // checkbox list
    private String audienceSize;

    private String fullTimeRole; // Yes/No
    private String courseTopic;

    public TrainerApplyRequest() {
    }

    // Getters & Setters

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<String> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<String> platforms) {
        this.platforms = platforms;
    }

    public String getAudienceSize() {
        return audienceSize;
    }

    public void setAudienceSize(String audienceSize) {
        this.audienceSize = audienceSize;
    }

    public String getFullTimeRole() {
        return fullTimeRole;
    }

    public void setFullTimeRole(String fullTimeRole) {
        this.fullTimeRole = fullTimeRole;
    }

    public String getCourseTopic() {
        return courseTopic;
    }

    public void setCourseTopic(String courseTopic) {
        this.courseTopic = courseTopic;
    }
}
