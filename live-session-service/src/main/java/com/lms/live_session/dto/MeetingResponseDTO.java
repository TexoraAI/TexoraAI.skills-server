//package com.lms.live_session.dto;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import java.time.LocalDateTime;
//
//public class MeetingResponseDTO {
//
//    private Long id;
//    private String title;
//    private String joinCode;
//    private String meetingUrl;
//    private String roomName;
//    private String creatorId;
//    private String creatorRole;
//    private String creatorName;
//    private Long organizationId;
//    private String meetingType;
//    private String meetingStatus;
//    private String timezone;
//    private LocalDateTime scheduledTimeUtc;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//    private LocalDateTime endedAt;
//    private Boolean reusable;
//
//    // Computed per response based on the caller's identity — never stored.
//    // False for anonymous/guest callers.
////    private boolean isHost;
//    @JsonProperty("isHost")
//    private boolean host;
//
//    public MeetingResponseDTO() {}
//
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public String getTitle() { return title; }
//    public void setTitle(String title) { this.title = title; }
//
//    public String getJoinCode() { return joinCode; }
//    public void setJoinCode(String joinCode) { this.joinCode = joinCode; }
//
//    public String getMeetingUrl() { return meetingUrl; }
//    public void setMeetingUrl(String meetingUrl) { this.meetingUrl = meetingUrl; }
//
//    public String getRoomName() { return roomName; }
//    public void setRoomName(String roomName) { this.roomName = roomName; }
//
//    public String getCreatorId() { return creatorId; }
//    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
//
//    public String getCreatorRole() { return creatorRole; }
//    public void setCreatorRole(String creatorRole) { this.creatorRole = creatorRole; }
//
//    public String getCreatorName() { return creatorName; }
//    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
//
//    public Long getOrganizationId() { return organizationId; }
//    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
//
//    public String getMeetingType() { return meetingType; }
//    public void setMeetingType(String meetingType) { this.meetingType = meetingType; }
//
//    public String getMeetingStatus() { return meetingStatus; }
//    public void setMeetingStatus(String meetingStatus) { this.meetingStatus = meetingStatus; }
//
//    public String getTimezone() { return timezone; }
//    public void setTimezone(String timezone) { this.timezone = timezone; }
//
//    public LocalDateTime getScheduledTimeUtc() { return scheduledTimeUtc; }
//    public void setScheduledTimeUtc(LocalDateTime scheduledTimeUtc) { this.scheduledTimeUtc = scheduledTimeUtc; }
//
//    public LocalDateTime getCreatedAt() { return createdAt; }
//    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
//
//    public LocalDateTime getUpdatedAt() { return updatedAt; }
//    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
//
//    public LocalDateTime getEndedAt() { return endedAt; }
//    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
//
//    public Boolean getReusable() { return reusable; }
//    public void setReusable(Boolean reusable) { this.reusable = reusable; }
//
//    public boolean isHost() { return host; }
//    public void setHost(boolean host) { this.host = host; }
//}

package com.lms.live_session.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class MeetingResponseDTO {

    private Long id;
    private String title;
    private String joinCode;
    private String meetingUrl;
    private String roomName;
    private String creatorId;
    private String creatorRole;
    private String creatorName;
    private Long organizationId;
    private String meetingType;
    private String meetingStatus;
    private String timezone;

    // Explicit pattern (no fractional seconds) so the frontend's
    // `new Date(value)` always gets a JS-parseable ISO string. Without
    // this, Jackson's default LocalDateTime output can include 6-9 digit
    // nanosecond fractions (e.g. "2026-07-23T14:30:00.123456789"), which
    // several browsers' Date parser rejects — showing up client-side as
    // "Invalid Date".
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledTimeUtc;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endedAt;

    private Boolean reusable;

    // Computed per response based on the caller's identity — never stored.
    // False for anonymous/guest callers.
//    private boolean isHost;
    @JsonProperty("isHost")
    private boolean host;

    public MeetingResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getJoinCode() { return joinCode; }
    public void setJoinCode(String joinCode) { this.joinCode = joinCode; }

    public String getMeetingUrl() { return meetingUrl; }
    public void setMeetingUrl(String meetingUrl) { this.meetingUrl = meetingUrl; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public String getCreatorRole() { return creatorRole; }
    public void setCreatorRole(String creatorRole) { this.creatorRole = creatorRole; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getMeetingType() { return meetingType; }
    public void setMeetingType(String meetingType) { this.meetingType = meetingType; }

    public String getMeetingStatus() { return meetingStatus; }
    public void setMeetingStatus(String meetingStatus) { this.meetingStatus = meetingStatus; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public LocalDateTime getScheduledTimeUtc() { return scheduledTimeUtc; }
    public void setScheduledTimeUtc(LocalDateTime scheduledTimeUtc) { this.scheduledTimeUtc = scheduledTimeUtc; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }

    public Boolean getReusable() { return reusable; }
    public void setReusable(Boolean reusable) { this.reusable = reusable; }

    public boolean isHost() { return host; }
    public void setHost(boolean host) { this.host = host; }
}