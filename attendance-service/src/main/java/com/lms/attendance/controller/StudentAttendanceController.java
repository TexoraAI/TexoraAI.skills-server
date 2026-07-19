//
//
//package com.lms.attendance.controller;
//
//import com.lms.attendance.dto.AttendanceHistoryResponse;
//import com.lms.attendance.dto.StudentAttendanceResponse;
//import com.lms.attendance.service.AttendanceService;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/student/attendance")
//public class StudentAttendanceController {
//
//    private final AttendanceService attendanceService;
//
//    public StudentAttendanceController(AttendanceService attendanceService) {
//        this.attendanceService = attendanceService;
//    }
//
//    // =======================================================
//    // EXISTING — UNTOUCHED
//    // =======================================================
//    @GetMapping("/monthly")
//    public List<StudentAttendanceResponse> getMonthlyAttendance(
//            @RequestParam int year,
//            @RequestParam int month,
//            Authentication authentication
//    ) {
//        String studentEmail = authentication.getName();
//
//        // NEW — trusted organizationId, attached by JwtAuthenticationFilter; never from request params
//        String organizationId = (String) authentication.getDetails();
//
//        return attendanceService.getMonthlyByStudentEmail(
//                studentEmail, year, month, organizationId
//        );
//    }
//
//    // =======================================================
//    // NEW — STUDENT ATTENDANCE HISTORY (date-range filters)
//    // filterType: TODAY | YESTERDAY | LAST_7_DAYS | LAST_14_DAYS | LAST_30_DAYS
//    //             | THIS_WEEK | THIS_MONTH | CUSTOM (needs startDate & endDate)
//    // =======================================================
//    @GetMapping("/history")
//    public AttendanceHistoryResponse getHistory(
//            @RequestParam String filterType,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
//            Authentication authentication
//    ) {
//        String studentEmail = authentication.getName();
//        String organizationId = (String) authentication.getDetails();
//
//        return attendanceService.getStudentAttendanceHistory(
//                studentEmail, organizationId, filterType, startDate, endDate
//        );
//    }
//
//    // =======================================================
//    // NEW — STUDENT ATTENDANCE EXCEL DOWNLOAD (own records only)
//    // =======================================================
//    @GetMapping("/download")
//    public ResponseEntity<byte[]> downloadReport(
//            @RequestParam String filterType,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
//            Authentication authentication
//    ) {
//        String studentEmail = authentication.getName();
//        String organizationId = (String) authentication.getDetails();
//
//        byte[] excel = attendanceService.exportStudentAttendanceExcel(
//                studentEmail, organizationId, filterType, startDate, endDate
//        );
//
//        String filename = "attendance-report-" + studentEmail + ".xlsx";
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
//                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
//                .body(excel);
//    }
//}


package com.lms.attendance.controller;

import com.lms.attendance.constants.AttendanceFeatureKeys;
import com.lms.attendance.dto.AttendanceHistoryResponse;
import com.lms.attendance.dto.StudentAttendanceResponse;
import com.lms.attendance.service.AttendanceFeatureFlagsService;
import com.lms.attendance.service.AttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/student/attendance")
public class StudentAttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceFeatureFlagsService attendanceFeatureFlagsService;

    public StudentAttendanceController(AttendanceService attendanceService,
                                        AttendanceFeatureFlagsService attendanceFeatureFlagsService) {
        this.attendanceService = attendanceService;
        this.attendanceFeatureFlagsService = attendanceFeatureFlagsService;
    }

    // =======================================================
    // EXISTING — UNTOUCHED (feature enforcement added only)
    // =======================================================
    @GetMapping("/monthly")
    public List<StudentAttendanceResponse> getMonthlyAttendance(
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication
    ) {
        String studentEmail = authentication.getName();
        String organizationId = (String) authentication.getDetails();

        attendanceFeatureFlagsService.enforce(
                organizationId, studentEmail, AttendanceFeatureKeys.GET_MONTHLY_ATTENDANCE
        );

        return attendanceService.getMonthlyByStudentEmail(
                studentEmail, year, month, organizationId
        );
    }

    // =======================================================
    // NEW — STUDENT ATTENDANCE HISTORY (date-range filters)
    // =======================================================
    @GetMapping("/history")
    public AttendanceHistoryResponse getHistory(
            @RequestParam String filterType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        String studentEmail = authentication.getName();
        String organizationId = (String) authentication.getDetails();

        attendanceFeatureFlagsService.enforce(
                organizationId, studentEmail, AttendanceFeatureKeys.GET_STUDENT_HISTORY
        );

        return attendanceService.getStudentAttendanceHistory(
                studentEmail, organizationId, filterType, startDate, endDate
        );
    }

    // =======================================================
    // NEW — STUDENT ATTENDANCE EXCEL DOWNLOAD (own records only)
    // =======================================================
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadReport(
            @RequestParam String filterType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        String studentEmail = authentication.getName();
        String organizationId = (String) authentication.getDetails();

        attendanceFeatureFlagsService.enforce(
                organizationId, studentEmail, AttendanceFeatureKeys.DOWNLOAD_STUDENT_REPORT
        );

        byte[] excel = attendanceService.exportStudentAttendanceExcel(
                studentEmail, organizationId, filterType, startDate, endDate
        );

        String filename = "attendance-report-" + studentEmail + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}