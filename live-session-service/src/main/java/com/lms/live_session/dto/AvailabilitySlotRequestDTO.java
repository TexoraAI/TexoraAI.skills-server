package com.lms.live_session.dto;

import java.time.LocalTime;

public class AvailabilitySlotRequestDTO {

    private String dayOfWeek; // MON/TUE/WED/THU/FRI/SAT/SUN or full names
    private LocalTime startTime;
    private LocalTime endTime;
    private String timezone;
    private Boolean isRecurring;

    public AvailabilitySlotRequestDTO() {
    }

    public AvailabilitySlotRequestDTO(String dayOfWeek, LocalTime startTime, LocalTime endTime,
                                       String timezone, Boolean isRecurring) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timezone = timezone;
        this.isRecurring = isRecurring;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }
}