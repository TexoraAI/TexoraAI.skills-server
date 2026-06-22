//package com.lms.user.dto;
//
//import java.util.List;
//
//public class TrainerProfileResponse {
//    private String linkedinUrl;
//    private String country;
//    private String audienceSize;
//    private String fullTimeRole;
//    private String courseTopic;
//    private List<String> platforms;
//
//    public String getLinkedinUrl() { return linkedinUrl; }
//    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }
//    public String getCountry() { return country; }
//    public void setCountry(String country) { this.country = country; }
//    public String getAudienceSize() { return audienceSize; }
//    public void setAudienceSize(String audienceSize) { this.audienceSize = audienceSize; }
//    public String getFullTimeRole() { return fullTimeRole; }
//    public void setFullTimeRole(String fullTimeRole) { this.fullTimeRole = fullTimeRole; }
//    public String getCourseTopic() { return courseTopic; }
//    public void setCourseTopic(String courseTopic) { this.courseTopic = courseTopic; }
//    public List<String> getPlatforms() { return platforms; }
//    public void setPlatforms(List<String> platforms) { this.platforms = platforms; }
//}

package com.lms.user.dto;

import java.io.Serializable;
import java.util.List;

public class TrainerProfileResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String linkedinUrl;
    private String country;
    private String audienceSize;
    private String fullTimeRole;
    private String courseTopic;
    private List<String> platforms;

    public TrainerProfileResponse() {}

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getAudienceSize() { return audienceSize; }
    public void setAudienceSize(String audienceSize) { this.audienceSize = audienceSize; }
    public String getFullTimeRole() { return fullTimeRole; }
    public void setFullTimeRole(String fullTimeRole) { this.fullTimeRole = fullTimeRole; }
    public String getCourseTopic() { return courseTopic; }
    public void setCourseTopic(String courseTopic) { this.courseTopic = courseTopic; }
    public List<String> getPlatforms() { return platforms; }
    public void setPlatforms(List<String> platforms) { this.platforms = platforms; }
}