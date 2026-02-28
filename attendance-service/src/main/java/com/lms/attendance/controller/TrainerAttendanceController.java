
package com.lms.attendance.controller;

import com.lms.attendance.dto.MarkAttendanceRequest;
import com.lms.attendance.service.AttendanceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainer/attendance")
@PreAuthorize("hasRole('TRAINER')")
public class TrainerAttendanceController {

    private final AttendanceService service;

    public TrainerAttendanceController(AttendanceService service) {
        this.service = service;
    }

    @PostMapping("/mark")
    public void mark(@RequestBody MarkAttendanceRequest req) {

        String trainerEmail =
                SecurityContextHolder.getContext().getAuthentication().getName();

        service.markAttendance(trainerEmail, req);
    }
}
