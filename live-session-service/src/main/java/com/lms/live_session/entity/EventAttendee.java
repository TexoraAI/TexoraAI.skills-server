package com.lms.live_session.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_attendees")
public class EventAttendee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "attendee_email", nullable = false, length = 255)
    private String attendeeEmail;

    @Column(nullable = false, length = 20)
    private String type; // REQUIRED / OPTIONAL

    @Column(nullable = false, length = 20)
    private String status; // PENDING / ACCEPTED / DECLINED / TENTATIVE

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public EventAttendee() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getAttendeeEmail() { return attendeeEmail; }
    public void setAttendeeEmail(String attendeeEmail) { this.attendeeEmail = attendeeEmail; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}