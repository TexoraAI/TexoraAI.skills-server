package com.lms.live_session.dto;
import java.time.LocalDateTime;
public class MeetingJoinRequestDTO {

	private Long requestId;
	private String guestIdentity;
	private String guestName;
	private String guestEmail;   // ADD
	private String status;
	private LocalDateTime requestedAt;

	public MeetingJoinRequestDTO() {}

	public MeetingJoinRequestDTO(Long requestId, String guestIdentity, String guestName, String guestEmail, String status) {
	    this.requestId = requestId;
	    this.guestIdentity = guestIdentity;
	    this.guestName = guestName;
	    this.guestEmail = guestEmail;
	    this.status = status;
	   
	}
	public MeetingJoinRequestDTO(Long requestId, String guestIdentity, String guestName, String guestEmail, String status, LocalDateTime requestedAt) {
	    this.requestId = requestId;
	    this.guestIdentity = guestIdentity;
	    this.guestName = guestName;
	    this.guestEmail = guestEmail;
	    this.status = status;
	    this.requestedAt = requestedAt;
	}
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public String getGuestIdentity() { return guestIdentity; }
    public void setGuestIdentity(String guestIdentity) { this.guestIdentity = guestIdentity; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    
    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }
}