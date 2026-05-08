//package com.lms.live_session.entity;
//
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name="session_participants")
//public class SessionParticipant {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private Long sessionId;
//    private Long studentId;
//
//    private LocalDateTime joinTime;
//    private LocalDateTime leaveTime;
//
//    private Integer watchPercentage;
//    private Integer chatMessages;
//
//    public SessionParticipant() {}
//
//    public Long getId() { return id; }
//
//    public Long getSessionId() { return sessionId; }
//
//    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
//
//    public Long getStudentId() { return studentId; }
//
//    public void setStudentId(Long studentId) { this.studentId = studentId; }
//
//    public LocalDateTime getJoinTime() { return joinTime; }
//
//    public void setJoinTime(LocalDateTime joinTime) { this.joinTime = joinTime; }
//
//    public LocalDateTime getLeaveTime() { return leaveTime; }
//
//    public void setLeaveTime(LocalDateTime leaveTime) { this.leaveTime = leaveTime; }
//
//    public Integer getWatchPercentage() { return watchPercentage; }
//
//    public void setWatchPercentage(Integer watchPercentage) { this.watchPercentage = watchPercentage; }
//
//    public Integer getChatMessages() { return chatMessages; }
//
//    public void setChatMessages(Integer chatMessages) { this.chatMessages = chatMessages; }
//}


// entity/SessionParticipant.java
package com.lms.live_session.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "session_participants")
public class SessionParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sessionId;
    private Long batchId;

    private String studentEmail;   // ✅ email instead of studentId
    private String trainerEmail;   // ✅ email instead of trainerId

    private LocalDateTime joinTime;
    private LocalDateTime leaveTime;
    private Integer watchPercentage;
    private Integer chatMessages;
    private String status;         // "JOINED" | "LEFT" | "ACTIVE"

    @PrePersist
    public void prePersist() {
        this.joinTime = LocalDateTime.now();
        this.status   = "JOINED";
        if (this.chatMessages == null) this.chatMessages = 0;
    }

    public SessionParticipant() {}

    // ── Getters & Setters ──────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public LocalDateTime getJoinTime() { return joinTime; }
    public void setJoinTime(LocalDateTime joinTime) { this.joinTime = joinTime; }

    public LocalDateTime getLeaveTime() { return leaveTime; }
    public void setLeaveTime(LocalDateTime leaveTime) { this.leaveTime = leaveTime; }

    public Integer getWatchPercentage() { return watchPercentage; }
    public void setWatchPercentage(Integer watchPercentage) { this.watchPercentage = watchPercentage; }

    public Integer getChatMessages() { return chatMessages; }
    public void setChatMessages(Integer chatMessages) { this.chatMessages = chatMessages; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}