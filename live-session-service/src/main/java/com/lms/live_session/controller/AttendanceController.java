package com.lms.live_session.controller;

import com.lms.live_session.entity.SessionParticipant;
import com.lms.live_session.service.AttendanceService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // ✅ JOIN SESSION (email from JWT)
    @PostMapping("/join")
    public SessionParticipant joinSession(
            @RequestParam Long sessionId,
            Authentication auth) {

        String studentEmail = auth.getName();   // ✅ from JWT

        return attendanceService.joinSession(sessionId, studentEmail);
    }

    // ✅ LEAVE SESSION (email from JWT)
    @PostMapping("/leave")
    public SessionParticipant leaveSession(
            @RequestParam Long sessionId,
            Authentication auth) {

        String studentEmail = auth.getName();   // ✅ from JWT

        return attendanceService.leaveSession(sessionId, studentEmail);
    }

    // ✅ GET ALL PARTICIPANTS
    @GetMapping("/session/{sessionId}")
    public List<SessionParticipant> getSessionParticipants(
            @PathVariable Long sessionId) {

        return attendanceService.getSessionParticipants(sessionId);
    }
}