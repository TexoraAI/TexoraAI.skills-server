package com.lms.live_session.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Payload sent by the public booking form (no auth required).
 * trainerEmail and eventTypeId are resolved server-side from the URL slug,
 * but are stored here so BookingService has a single flat object to work with.
 */
public class BookedSlotRequest {

    // ── Resolved server-side from URL path (slug → trainerEmail) ─────────────
    private String trainerEmail;
    private Long   eventTypeId;

    // ── Supplied by the booker in the form ───────────────────────────────────
    private String bookerName;
    private String bookerEmail;

    /** Optional – set when the booker is a logged-in ILM ORA student */
    private Long bookerUserId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate bookedDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    private String timezone = "Asia/Kolkata";

    private String notes;

    // ── Constructors ──────────────────────────────────────────────────────────

    public BookedSlotRequest() {}

    public BookedSlotRequest(String trainerEmail, Long eventTypeId,
                             String bookerName, String bookerEmail,
                             Long bookerUserId, LocalDate bookedDate,
                             LocalTime startTime, String timezone, String notes) {
        this.trainerEmail  = trainerEmail;
        this.eventTypeId   = eventTypeId;
        this.bookerName    = bookerName;
        this.bookerEmail   = bookerEmail;
        this.bookerUserId  = bookerUserId;
        this.bookedDate    = bookedDate;
        this.startTime     = startTime;
        this.timezone      = timezone;
        this.notes         = notes;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public Long getEventTypeId() { return eventTypeId; }
    public void setEventTypeId(Long eventTypeId) { this.eventTypeId = eventTypeId; }

    public String getBookerName() { return bookerName; }
    public void setBookerName(String bookerName) { this.bookerName = bookerName; }

    public String getBookerEmail() { return bookerEmail; }
    public void setBookerEmail(String bookerEmail) { this.bookerEmail = bookerEmail; }

    public Long getBookerUserId() { return bookerUserId; }
    public void setBookerUserId(Long bookerUserId) { this.bookerUserId = bookerUserId; }

    public LocalDate getBookedDate() { return bookedDate; }
    public void setBookedDate(LocalDate bookedDate) { this.bookedDate = bookedDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}