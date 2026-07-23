package com.lms.live_session.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "meeting_join_requests")
public class MeetingJoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meeting_id", nullable = false)
    private Long meetingId;

    // Opaque per-request identity handed to the guest's browser. It doubles
    // as (a) the bearer credential the guest uses to poll its own status /
    // fetch its own token later, and (b) the LiveKit participant identity
    // once admitted. Nobody can guess another guest's by walking request ids.
    @Column(name = "guest_identity", nullable = false, updatable = false, length = 36)
    private String guestIdentity;

    @Column(name = "guest_name", nullable = false)
    private String guestName;
    
   

    // ADD:
    @Column(name = "guest_email")
    private String guestEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private JoinRequestStatus status;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @PrePersist
    protected void onCreate() {
        this.requestedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = JoinRequestStatus.PENDING;
        }
    }

    public MeetingJoinRequest() {}

    public Long getId() { return id; }

    public Long getMeetingId() { return meetingId; }
    public void setMeetingId(Long meetingId) { this.meetingId = meetingId; }

    public String getGuestIdentity() { return guestIdentity; }
    public void setGuestIdentity(String guestIdentity) { this.guestIdentity = guestIdentity; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    
   
    // ADD:
    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }

    public JoinRequestStatus getStatus() { return status; }
    public void setStatus(JoinRequestStatus status) { this.status = status; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }
}