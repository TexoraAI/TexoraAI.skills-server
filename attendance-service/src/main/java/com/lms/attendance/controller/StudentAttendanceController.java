package com.lms.attendance.controller;

import com.lms.attendance.dto.StudentAttendanceResponse;
import com.lms.attendance.service.AttendanceService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/attendance")
public class StudentAttendanceController {

    private final AttendanceService attendanceService;

    public StudentAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/monthly")
    public List<StudentAttendanceResponse> getMonthlyAttendance(
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication
    ) {
        String studentEmail = authentication.getName();

        return attendanceService.getMonthlyByStudentEmail(
                studentEmail, year, month
        );
    }
}
