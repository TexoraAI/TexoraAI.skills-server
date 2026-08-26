package com.lms.live_session.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;

public class EventSummaryDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;

    private Integer eventCount;
    private List<EventResponseDTO> events;

    public EventSummaryDTO() {}

    public EventSummaryDTO(LocalDate date, Integer eventCount, List<EventResponseDTO> events) {
        this.date = date;
        this.eventCount = eventCount;
        this.events = events;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Integer getEventCount() { return eventCount; }
    public void setEventCount(Integer eventCount) { this.eventCount = eventCount; }

    public List<EventResponseDTO> getEvents() { return events; }
    public void setEvents(List<EventResponseDTO> events) { this.events = events; }
}