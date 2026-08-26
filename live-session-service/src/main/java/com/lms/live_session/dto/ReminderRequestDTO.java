package com.lms.live_session.dto;

public class ReminderRequestDTO {

    private Long eventId;
    private Long scheduleId;
    private String reminderTime;

    public ReminderRequestDTO() {
    }

    public ReminderRequestDTO(Long eventId, Long scheduleId, String reminderTime) {
        this.eventId = eventId;
        this.scheduleId = scheduleId;
        this.reminderTime = reminderTime;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }
}