//package com.lms.live_session.dto;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//
//public class CreateSessionRequest {
//
//    private String title;
//    private String description;
//    private Long trainerId;
//    private Long batchId;
//    private LocalDate date;
//    private LocalTime time;
//    private Integer duration;
//
//    public String getTitle() { return title; }
//
//    public void setTitle(String title) { this.title = title; }
//
//    public String getDescription() { return description; }
//
//    public void setDescription(String description) { this.description = description; }
//
//    public Long getTrainerId() { return trainerId; }
//
//    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }
//
//    public Long getBatchId() { return batchId; }
//
//    public void setBatchId(Long batchId) { this.batchId = batchId; }
//
//    public LocalDate getDate() { return date; }
//
//    public void setDate(LocalDate date) { this.date = date; }
//
//    public LocalTime getTime() { return time; }
//
//    public void setTime(LocalTime time) { this.time = time; }
//
//    public Integer getDuration() { return duration; }
//
//    public void setDuration(Integer duration) { this.duration = duration; }
//}

package com.lms.live_session.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class CreateSessionRequest {

    private String title;
    private String description;
    private String trainerEmail;           // ✅ was trainerId (Long)
    private Long batchId;
    private LocalDate date;
    private LocalTime time;
    private Integer duration;
    private Boolean chatEnabled;
    private Boolean autoRecord;
    private Boolean notifyStudents;
    
    private String meetingType;       // "CUSTOM" or "EXTERNAL"
    private String externalMeetingUrl;
    private Boolean isPublished;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTrainerEmail() { return trainerEmail; }       // ✅ changed
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public Boolean getChatEnabled() { return chatEnabled; }
    public void setChatEnabled(Boolean chatEnabled) { this.chatEnabled = chatEnabled; }

    public Boolean getAutoRecord() { return autoRecord; }
    public void setAutoRecord(Boolean autoRecord) { this.autoRecord = autoRecord; }

    public Boolean getNotifyStudents() { return notifyStudents; }
    public void setNotifyStudents(Boolean notifyStudents) { this.notifyStudents = notifyStudents; }
    
    
    public String getMeetingType() { return meetingType; }
    public void setMeetingType(String t) { this.meetingType = t; }

    public String getExternalMeetingUrl() { return externalMeetingUrl; }
    public void setExternalMeetingUrl(String url) { this.externalMeetingUrl = url; }

    public Boolean getIsPublished() { return isPublished; }
    public void setIsPublished(Boolean p) { this.isPublished = p; }
}