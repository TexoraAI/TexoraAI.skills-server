
package com.lms.attendance.controller;

import com.lms.attendance.constants.AttendanceFeatureKeys;
import com.lms.attendance.dto.MarkAttendanceRequest;
import com.lms.attendance.dto.MarkOwnSessionRequest;
import com.lms.attendance.dto.BatchAttendanceOverviewResponse;
import com.lms.attendance.dto.BatchAttendanceDetailResponse;
import com.lms.attendance.dto.AttendanceHistoryResponse;
import com.lms.attendance.dto.TrainerSessionHistoryResponse;
import com.lms.attendance.entity.TrainerSessionAttendance;
import com.lms.attendance.service.AttendanceFeatureFlagsService;
import com.lms.attendance.service.AttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trainer/attendance")
// NOTE: class-level @PreAuthorize("hasRole('TRAINER')") REMOVED.
// Role gating happens per-method below AND at the gateway (GatewaySecurityConfig).
public class TrainerAttendanceController {

    private final AttendanceService service;
    private final AttendanceFeatureFlagsService attendanceFeatureFlagsService;

    public TrainerAttendanceController(AttendanceService service,
                                        AttendanceFeatureFlagsService attendanceFeatureFlagsService) {
        this.service = service;
        this.attendanceFeatureFlagsService = attendanceFeatureFlagsService;
    }

    // =======================================================
    // EXISTING — UNTOUCHED (marks STUDENT attendance)
    // =======================================================
    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping("/mark")
    public void mark(@RequestBody MarkAttendanceRequest req) {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String trainerEmail = authentication.getName();
        String organizationId = (String) authentication.getDetails();

        attendanceFeatureFlagsService.enforce(
                organizationId, trainerEmail, AttendanceFeatureKeys.MARK_ATTENDANCE
        );

        service.markAttendance(trainerEmail, req, organizationId);
    }

    // =======================================================
    // NEW (a) — trainer marks their OWN attendance
    // =======================================================
    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping("/session/mark")
    public void markOwnSession(@RequestBody MarkOwnSessionRequest req) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String trainerEmail = authentication.getName();
        String organizationId = (String) authentication.getDetails();

        attendanceFeatureFlagsService.enforce(
                organizationId, trainerEmail, AttendanceFeatureKeys.MARK_TRAINER_SESSION
        );

        service.markOwnSessionAttendance(trainerEmail, req, organizationId);
    }

    // =======================================================
    // NEW (b) — trainer's own history for a month
    // =======================================================
    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/session/history")
    public List<TrainerSessionAttendance> getOwnSessionHistory(
            @RequestParam int year,
            @RequestParam int month
    ) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String trainerEmail = authentication.getName();
        String organizationId = (String) authentication.getDetails();

        attendanceFeatureFlagsService.enforce(
                organizationId, trainerEmail, AttendanceFeatureKeys.GET_TRAINER_SESSION_HISTORY
        );

        return service.getOwnSessionHistory(trainerEmail, year, month);
    }

    // =======================================================
    // NEW (c) — ADMIN overview (strict org match)
    // =======================================================
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/overview")
    public List<BatchAttendanceOverviewResponse> getAdminOverview() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String organizationId = (String) authentication.getDetails();
        String adminEmail = authentication.getName();

        attendanceFeatureFlagsService.enforce(
                organizationId, adminEmail, AttendanceFeatureKeys.GET_ADMIN_OVERVIEW
        );

        return service.getOrgBatchOverview(organizationId);
    }

    // =======================================================
    // NEW (d) — ADMIN batch detail (combined trainer + student)
    // =======================================================
//    @PreAuthorize("hasRole('ADMIN')")
//    @GetMapping("/admin/batch/{batchId}")
//    public BatchAttendanceDetailResponse getAdminBatchDetail(@PathVariable Long batchId) {
//        var authentication = SecurityContextHolder.getContext().getAuthentication();
//        String organizationId = (String) authentication.getDetails();
//        String adminEmail = authentication.getName();
//
//        attendanceFeatureFlagsService.enforce(
//                organizationId, adminEmail, AttendanceFeatureKeys.GET_ADMIN_BATCH_DETAIL
//        );
//
//        return service.getOrgBatchDetail(batchId, organizationId);
//    }
 // AFTER
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/batch/{batchId}")
    public BatchAttendanceDetailResponse getAdminBatchDetail(
            @PathVariable Long batchId,
            @RequestParam(defaultValue = "THIS_MONTH") String filterType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String organizationId = (String) authentication.getDetails();

        return service.getOrgBatchDetail(batchId, organizationId, filterType, startDate, endDate);
    }

    // =======================================================
    // NEW (e) — SUPER_ADMIN overview (orgless only) — NOT ENFORCED
    // =======================================================
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/superadmin/overview")
    public List<BatchAttendanceOverviewResponse> getSuperAdminOverview() {
        return service.getOrglessBatchOverview();
    }

    // =======================================================
    // NEW (f) — SUPER_ADMIN batch detail (orgless only) — NOT ENFORCED
    // =======================================================
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
//    @GetMapping("/superadmin/batch/{batchId}")
//    public BatchAttendanceDetailResponse getSuperAdminBatchDetail(@PathVariable Long batchId) {
//        return service.getOrglessBatchDetail(batchId);
//    }
 // AFTER
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/superadmin/batch/{batchId}")
    public BatchAttendanceDetailResponse getSuperAdminBatchDetail(
            @PathVariable Long batchId,
            @RequestParam(defaultValue = "THIS_MONTH") String filterType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return service.getOrglessBatchDetail(batchId, filterType, startDate, endDate);
    }
    // ---------------- TRAINER (own data) ----------------

    // NEW (g) — trainer's own marked (student) attendance history, filtered, optional batchId
    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/history")
    public AttendanceHistoryResponse getTrainerHistory(
            @RequestParam String filterType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long batchId
    ) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String trainerEmail = authentication.getName();
        String organizationId = (String) authentication.getDetails();

        attendanceFeatureFlagsService.enforce(
                organizationId, trainerEmail, AttendanceFeatureKeys.GET_TRAINER_HISTORY
        );

        return service.getTrainerMarkedHistory(trainerEmail, filterType, startDate, endDate, batchId);
    }

    // NEW (h) — trainer's own session (self) attendance history, filtered, optional batchId
    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/session/history/filter")
    public TrainerSessionHistoryResponse getTrainerSessionHistoryFiltered(
            @RequestParam String filterType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long batchId
    ) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String trainerEmail = authentication.getName();
        String organizationId = (String) authentication.getDetails();

        attendanceFeatureFlagsService.enforce(
                organizationId, trainerEmail, AttendanceFeatureKeys.GET_TRAINER_SESSION_HISTORY_FILTER
        );

        return service.getTrainerSessionHistoryFiltered(trainerEmail, filterType, startDate, endDate, batchId);
    }

    // NEW (i) — trainer Excel download
    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadTrainerReport(
            @RequestParam String filterType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false, defaultValue = "STUDENT") String type
    ) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String trainerEmail = authentication.getName();
        String organizationId = (String) authentication.getDetails();

        attendanceFeatureFlagsService.enforce(
                organizationId, trainerEmail, AttendanceFeatureKeys.DOWNLOAD_TRAINER_REPORT
        );

        byte[] excel = service.exportTrainerAttendanceExcel(trainerEmail, filterType, startDate, endDate, batchId, type);
        String filename = "trainer-attendance-report-" + trainerEmail + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    // ---------------- ADMIN ----------------

    // NEW (j) — admin attendance history
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/history")
    public AttendanceHistoryResponse getAdminHistory(
            @RequestParam String filterType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) String trainerEmail,
            @RequestParam(required = false) String studentEmail
    ) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String organizationId = (String) authentication.getDetails();
        String adminEmail = authentication.getName();

        attendanceFeatureFlagsService.enforce(
                organizationId, adminEmail, AttendanceFeatureKeys.GET_ADMIN_HISTORY
        );

        return service.getAdminAttendanceHistory(
                organizationId, filterType, startDate, endDate, batchId, trainerEmail, studentEmail);
    }

    // NEW (k) — admin Excel download
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/download")
    public ResponseEntity<byte[]> downloadAdminReport(
            @RequestParam String filterType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) String trainerEmail,
            @RequestParam(required = false) String studentEmail,
            @RequestParam(required = false, defaultValue = "STUDENT") String type
    ) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String organizationId = (String) authentication.getDetails();
        String generatedBy = authentication.getName();

        attendanceFeatureFlagsService.enforce(
                organizationId, generatedBy, AttendanceFeatureKeys.DOWNLOAD_ADMIN_REPORT
        );

        byte[] excel = service.exportAdminAttendanceExcel(
                organizationId, generatedBy, filterType, startDate, endDate, batchId, trainerEmail, studentEmail, type);
        String filename = "admin-attendance-report.xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    // ---------------- SUPER ADMIN — NOT ENFORCED ----------------

    // NEW (l) — super-admin attendance history, orgless only — UNTOUCHED
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/superadmin/history")
    public AttendanceHistoryResponse getSuperAdminHistory(
            @RequestParam String filterType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) String trainerEmail,
            @RequestParam(required = false) String studentEmail
    ) {
        return service.getSuperAdminAttendanceHistory(filterType, startDate, endDate, batchId, trainerEmail, studentEmail);
    }

    // NEW (m) — super-admin Excel download, orgless only — UNTOUCHED
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/superadmin/download")
    public ResponseEntity<byte[]> downloadSuperAdminReport(
            @RequestParam String filterType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) String trainerEmail,
            @RequestParam(required = false) String studentEmail,
            @RequestParam(required = false, defaultValue = "STUDENT") String type
    ) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String generatedBy = authentication.getName();

        byte[] excel = service.exportSuperAdminAttendanceExcel(
                generatedBy, filterType, startDate, endDate, batchId, trainerEmail, studentEmail, type);
        String filename = "superadmin-attendance-report.xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}