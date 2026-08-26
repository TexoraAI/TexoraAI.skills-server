package com.lms.live_session.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class EventAttendeeDTO {

    private String attendeeEmail;
    private String type;
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public EventAttendeeDTO() {}

    public EventAttendeeDTO(String attendeeEmail, String type, String status, LocalDateTime createdAt) {
        this.attendeeEmail = attendeeEmail;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getAttendeeEmail() { return attendeeEmail; }
    public void setAttendeeEmail(String attendeeEmail) { this.attendeeEmail = attendeeEmail; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}