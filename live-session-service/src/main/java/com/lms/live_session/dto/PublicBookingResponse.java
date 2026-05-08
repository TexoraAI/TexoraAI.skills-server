package com.lms.live_session.dto;

public class PublicBookingResponse {
    private Long id;
    private Long sessionId;
    private String fullName;
    private String email;
    private String accessToken;
    private String status;
    private String message;

    public PublicBookingResponse(Long id, Long sessionId, String fullName, String email,
                                String accessToken, String status, String message) {
        this.id = id;
        this.sessionId = sessionId;
        this.fullName = fullName;
        this.email = email;
        this.accessToken = accessToken;
        this.status = status;
        this.message = message;
    }

    public Long getId() { return id; }
    public Long getSessionId() { return sessionId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getAccessToken() { return accessToken; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
}