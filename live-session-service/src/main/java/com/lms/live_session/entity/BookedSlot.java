package com.lms.live_session.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "booked_slots")
public class BookedSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventTypeId;

    @Column(nullable = false)
    private String trainerEmail;

    @Column(nullable = false)
    private String bookerName;

    @Column(nullable = false)
    private String bookerEmail;

    /** Nullable – set only when the booker is a registered ILM ORA student */
    @Column
    private Long bookerUserId;

    @Column(nullable = false)
    private LocalDate bookedDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private String timezone = "Asia/Kolkata";

    /**
     * Lifecycle: PENDING → CONFIRMED → COMPLETED
     *                     ↘ CANCELLED
     */
    @Column(nullable = false)
    private String status = "PENDING";

    /**
     * Set after trainer confirms – links to an auto-created LiveSession row.
     * Nullable until confirmation.
     */
    @Column
    private Long liveSessionId;

    @Column(length = 2000)
    private String notes;

    /** UUID token – used to generate the booker's personal join link */
    @Column(unique = true, nullable = false)
    private String uniqueAccessToken;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────────────────────────

    public BookedSlot() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEventTypeId() { return eventTypeId; }
    public void setEventTypeId(Long eventTypeId) { this.eventTypeId = eventTypeId; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

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

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getLiveSessionId() { return liveSessionId; }
    public void setLiveSessionId(Long liveSessionId) { this.liveSessionId = liveSessionId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getUniqueAccessToken() { return uniqueAccessToken; }
    public void setUniqueAccessToken(String uniqueAccessToken) { this.uniqueAccessToken = uniqueAccessToken; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}