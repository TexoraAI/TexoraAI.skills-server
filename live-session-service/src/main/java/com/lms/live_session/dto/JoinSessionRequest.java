
package com.lms.live_session.dto;

public class JoinSessionRequest {

    private Long sessionId;
    private String studentEmail;           // ✅ was studentId (Long)

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getStudentEmail() { return studentEmail; }       // ✅ changed
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
}