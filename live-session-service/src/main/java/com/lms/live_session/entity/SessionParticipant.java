package com.lms.live_session.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="session_participants")
public class SessionParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sessionId;
    private Long studentId;

    private LocalDateTime joinTime;
    private LocalDateTime leaveTime;

    private Integer watchPercentage;
    private Integer chatMessages;

    public SessionParticipant() {}

    public Long getId() { return id; }

    public Long getSessionId() { return sessionId; }

    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getStudentId() { return studentId; }

    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public LocalDateTime getJoinTime() { return joinTime; }

    public void setJoinTime(LocalDateTime joinTime) { this.joinTime = joinTime; }

    public LocalDateTime getLeaveTime() { return leaveTime; }

    public void setLeaveTime(LocalDateTime leaveTime) { this.leaveTime = leaveTime; }

    public Integer getWatchPercentage() { return watchPercentage; }

    public void setWatchPercentage(Integer watchPercentage) { this.watchPercentage = watchPercentage; }

    public Integer getChatMessages() { return chatMessages; }

    public void setChatMessages(Integer chatMessages) { this.chatMessages = chatMessages; }
}