package com.lms.live_session.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Returned to the client after a booking is created or retrieved.
 * Combines BookedSlot + EventType name into a flat, serialisable object.
 *
 * FIX 2: Added trainerName and createdAt fields.
 */
public class BookedSlotResponse {

    private Long   id;
    private String bookerName;
    private String bookerEmail;
    private String trainerEmail;

    /** FIX 2: human-readable trainer display name (from BookingPageSettings) */
    private String trainerName;

    private String eventTypeName;
    private Integer durationMinutes;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate bookedDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private String status;

    /** Full join URL: baseUrl/join/{uniqueAccessToken} */
    private String joinLink;

    private String uniqueAccessToken;

    /** Human-readable confirmation message */
    private String message;

    /** Nullable – populated only after trainer confirms */
    private Long liveSessionId;

    /** FIX 2: timestamp when this booking record was created */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    // ── Constructors ──────────────────────────────────────────────────────────

    public BookedSlotResponse() {}

    public BookedSlotResponse(Long id, String bookerName, String bookerEmail,
                              String trainerEmail, String eventTypeName,
                              Integer durationMinutes, LocalDate bookedDate,
                              LocalTime startTime, LocalTime endTime,
                              String status, String joinLink,
                              String uniqueAccessToken, String message,
                              Long liveSessionId) {
        this.id                 = id;
        this.bookerName         = bookerName;
        this.bookerEmail        = bookerEmail;
        this.trainerEmail       = trainerEmail;
        this.eventTypeName      = eventTypeName;
        this.durationMinutes    = durationMinutes;
        this.bookedDate         = bookedDate;
        this.startTime          = startTime;
        this.endTime            = endTime;
        this.status             = status;
        this.joinLink           = joinLink;
        this.uniqueAccessToken  = uniqueAccessToken;
        this.message            = message;
        this.liveSessionId      = liveSessionId;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBookerName() { return bookerName; }
    public void setBookerName(String bookerName) { this.bookerName = bookerName; }

    public String getBookerEmail() { return bookerEmail; }
    public void setBookerEmail(String bookerEmail) { this.bookerEmail = bookerEmail; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    /** FIX 2 */
    public String getTrainerName() { return trainerName; }
    public void setTrainerName(String trainerName) { this.trainerName = trainerName; }

    public String getEventTypeName() { return eventTypeName; }
    public void setEventTypeName(String eventTypeName) { this.eventTypeName = eventTypeName; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public LocalDate getBookedDate() { return bookedDate; }
    public void setBookedDate(LocalDate bookedDate) { this.bookedDate = bookedDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getJoinLink() { return joinLink; }
    public void setJoinLink(String joinLink) { this.joinLink = joinLink; }

    public String getUniqueAccessToken() { return uniqueAccessToken; }
    public void setUniqueAccessToken(String uniqueAccessToken) { this.uniqueAccessToken = uniqueAccessToken; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getLiveSessionId() { return liveSessionId; }
    public void setLiveSessionId(Long liveSessionId) { this.liveSessionId = liveSessionId; }

    /** FIX 2 */
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}