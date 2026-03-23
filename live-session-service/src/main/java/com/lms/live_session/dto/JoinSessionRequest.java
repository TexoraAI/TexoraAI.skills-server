package com.lms.live_session.dto;

public class JoinSessionRequest {

    private Long sessionId;
    private Long studentId;

    public Long getSessionId() { return sessionId; }

    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getStudentId() { return studentId; }

    public void setStudentId(Long studentId) { this.studentId = studentId; }
}