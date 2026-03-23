package com.lms.live_session.controller;

import com.lms.live_session.entity.SessionParticipant;
import com.lms.live_session.service.AttendanceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/join")
    public SessionParticipant joinSession(
            @RequestParam Long sessionId,
            @RequestParam Long studentId) {

        return attendanceService.joinSession(sessionId, studentId);
    }

    @PostMapping("/leave")
    public SessionParticipant leaveSession(
            @RequestParam Long participantId) {

        return attendanceService.leaveSession(participantId);
    }

    @GetMapping("/session/{sessionId}")
    public List<SessionParticipant> getSessionParticipants(
            @PathVariable Long sessionId) {

        return attendanceService.getSessionParticipants(sessionId);
    }
}