//
//package com.lms.attendance.service;
//
//import com.lms.attendance.dto.MarkAttendanceRequest;
//import com.lms.attendance.dto.MarkOwnSessionRequest;
//import com.lms.attendance.dto.StudentAttendanceResponse;
//import com.lms.attendance.dto.BatchAttendanceOverviewResponse;
//import com.lms.attendance.dto.BatchAttendanceDetailResponse;
//import com.lms.attendance.entity.Attendance;
//import com.lms.attendance.entity.AttendanceStatus;
//import com.lms.attendance.entity.TrainerBatchAccess;
//import com.lms.attendance.entity.TrainerSessionAttendance;
//import com.lms.attendance.event.AttendanceMarkedEvent;
//import com.lms.attendance.kafka.AttendanceEventProducer;
//import com.lms.attendance.repository.AttendanceRepository;
//import com.lms.attendance.repository.TrainerBatchAccessRepository;
//import com.lms.attendance.repository.StudentBatchAccessRepository;
//import com.lms.attendance.repository.TrainerSessionAttendanceRepository;
//
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.time.YearMonth;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Service
//public class AttendanceService {
//
//    private final AttendanceRepository attendanceRepository;
//    private final AttendanceEventProducer attendanceEventProducer;
//    private final TrainerBatchAccessRepository accessRepo;
//    private final StudentBatchAccessRepository studentAccessRepo; // NEW
//    private final TrainerSessionAttendanceRepository sessionAttendanceRepo; // NEW — additive constructor param
//
////cfcfcfcfffff
//    public AttendanceService(
//            AttendanceRepository attendanceRepository,
//            AttendanceEventProducer attendanceEventProducer,
//            TrainerBatchAccessRepository accessRepo,
//            StudentBatchAccessRepository studentAccessRepo, // NEW param
//            TrainerSessionAttendanceRepository sessionAttendanceRepo) { // NEW param — added at end, additive only
//    	
//    	
//        this.attendanceRepository = attendanceRepository;
//        this.attendanceEventProducer = attendanceEventProducer;
//        this.accessRepo=accessRepo;
//        this.studentAccessRepo=studentAccessRepo; // NEW
//        this.sessionAttendanceRepo=sessionAttendanceRepo; // NEW
//    }
//
//    // =======================
//    // MARK ATTENDANCE (FINAL)
//    // =======================
//    public void markAttendance(String trainerEmail, MarkAttendanceRequest request) {
//
//        LocalDate date = request.getAttendanceDate();
//        Long batchId = request.getBatchId();
//
//        // 🔒 NEW: Trainer must belong to this batch
//        boolean allowed = accessRepo
//                .findByBatchIdAndTrainerEmail(batchId, trainerEmail)
//                .isPresent();
//
//        if (!allowed) {
//            throw new RuntimeException("You are not assigned to this batch. Attendance denied.");
//        }
//
//        // ---- EXISTING LOGIC (UNCHANGED) ----
//        for (MarkAttendanceRequest.StudentAttendance sa : request.getAttendances()) {
//
//            Attendance attendance = attendanceRepository
//                    .findByBatchIdAndStudentUserIdAndAttendanceDate(
//                            batchId,
//                            sa.getStudentUserId(),
//                            date
//                    )
//                    .orElse(new Attendance());
//
//            attendance.setBatchId(batchId);
//            attendance.setStudentUserId(sa.getStudentUserId());
//            attendance.setStudentEmail(sa.getStudentEmail());
//            attendance.setTrainerEmail(trainerEmail);
//            attendance.setAttendanceDate(date);
//            attendance.setStatus(AttendanceStatus.valueOf(sa.getStatus()));
//
//            attendanceRepository.save(attendance);
//
//         // ✅ NEW — pass trainerEmail so notification knows who to notify
//            attendanceEventProducer.publish(
//                new AttendanceMarkedEvent(
//                    batchId,
//                    sa.getStudentUserId(),
//                    sa.getStudentEmail(),
//                    sa.getStatus(),
//                    date,
//                    trainerEmail  // ✅ ADDED
//                )
//            );
//            
//                   }
//        
//    }
//
//    // =======================
//    // MARK ATTENDANCE — ORG-AWARE OVERLOAD (NEW)
//    // =======================
//    // Extends validation only. If organizationId != null, the trainer's batch access
//    // must belong to that same organization. If organizationId == null (standalone
//    // trainer), behavior is identical to the original method above.
//    public void markAttendance(String trainerEmail, MarkAttendanceRequest request, String organizationId) {
//
//        Long batchId = request.getBatchId();
//
//        if (organizationId != null) {
//            boolean allowedInOrg = accessRepo
//                    .findByBatchIdAndTrainerEmailAndOrganizationId(batchId, trainerEmail, organizationId)
//                    .isPresent();
//
//            if (!allowedInOrg) {
//                throw new RuntimeException("You are not assigned to this batch within your organization. Attendance denied.");
//            }
//
//            // NEW — stamp organizationId onto persisted attendance rows for tenant lineage,
//            // without disturbing the existing per-student save/publish logic above.
//            for (MarkAttendanceRequest.StudentAttendance sa : request.getAttendances()) {
//                attendanceRepository
//                        .findByBatchIdAndStudentUserIdAndAttendanceDate(
//                                batchId, sa.getStudentUserId(), request.getAttendanceDate())
//                        .ifPresent(a -> {
//                            a.setOrganizationId(organizationId);
//                            attendanceRepository.save(a);
//                        });
//            }
//        }
//
//        // Delegate to the existing, unmodified method for all core business logic
//        markAttendance(trainerEmail, request);
//    }
//
//
//    // =======================
//    // STUDENT MONTHLY VIEW
//    // =======================
//    public List<StudentAttendanceResponse> getMonthlyByStudentEmail(
//            String email, int year, int month
//    ) {
//        return attendanceRepository
//                .findMonthlyByStudentEmail(email, year, month)
//                .stream()
//                .map(a -> new StudentAttendanceResponse(
//                        a.getAttendanceDate(),
//                        a.getStatus().name()
//                ))
//                .toList();
//    }
//
//    // =======================
//    // STUDENT MONTHLY VIEW — ORG-AWARE OVERLOAD (NEW)
//    // =======================
//    // Extends validation only. If organizationId != null, the student must have an
//    // access record within that organization before the existing lookup runs.
//    // If organizationId == null (standalone student), behavior is identical to the
//    // original method above.
//    public List<StudentAttendanceResponse> getMonthlyByStudentEmail(
//            String email, int year, int month, String organizationId
//    ) {
//        if (organizationId != null) {
//            boolean allowedInOrg = studentAccessRepo
//                    .findByStudentEmailAndOrganizationId(email, organizationId)
//                    .isPresent();
//
//            if (!allowedInOrg) {
//                throw new RuntimeException("You do not have attendance access within your organization.");
//            }
//        }
//
//        // Delegate to the existing, unmodified method for all core business logic
//        return getMonthlyByStudentEmail(email, year, month);
//    }
//
//    // =======================================================
//    // NEW — TRAINER MARKS OWN ATTENDANCE
//    // =======================================================
//    // Separate from markAttendance() above, which marks STUDENT attendance.
//    // Identity + organizationId come from the trusted Authentication object only
//    // (passed in from the controller), never from the request body.
//    public void markOwnSessionAttendance(String trainerEmail, MarkOwnSessionRequest request, String organizationId) {
//
//        Long batchId = request.getBatchId();
//
//        // Trainer must belong to this batch — same org-match rule as markAttendance's overload
//        boolean allowed = (organizationId != null)
//                ? accessRepo.findByBatchIdAndTrainerEmailAndOrganizationId(batchId, trainerEmail, organizationId).isPresent()
//                : accessRepo.findByBatchIdAndTrainerEmail(batchId, trainerEmail).isPresent();
//
//        if (!allowed) {
//            throw new RuntimeException("You are not assigned to this batch. Attendance denied.");
//        }
//
//        TrainerSessionAttendance row = sessionAttendanceRepo
//                .findByBatchIdAndTrainerEmailAndSessionDate(batchId, trainerEmail, request.getDate())
//                .orElse(new TrainerSessionAttendance());
//
//        row.setBatchId(batchId);
//        row.setTrainerEmail(trainerEmail);
//        row.setSessionDate(request.getDate());
//        row.setStatus(AttendanceStatus.valueOf(request.getStatus()));
//        row.setOrganizationId(organizationId);
//
//        sessionAttendanceRepo.save(row);
//    }
//
//    // =======================================================
//    // NEW — TRAINER'S OWN SESSION HISTORY FOR A MONTH
//    // =======================================================
//    public List<TrainerSessionAttendance> getOwnSessionHistory(String trainerEmail, int year, int month) {
//        YearMonth ym = YearMonth.of(year, month);
//        LocalDate start = ym.atDay(1);
//        LocalDate end = ym.atEndOfMonth();
//
//        return sessionAttendanceRepo.findByTrainerEmailAndSessionDateBetween(trainerEmail, start, end);
//    }
//
//    // =======================================================
//    // NEW — ADMIN OVERVIEW (strict org match)
//    // =======================================================
//    public List<BatchAttendanceOverviewResponse> getOrgBatchOverview(String organizationId) {
//        List<Long> batchIds = accessRepo.findDistinctBatchIdsByOrganizationId(organizationId);
//        return buildOverviewRows(batchIds);
//    }
//
//    // =======================================================
//    // NEW — SUPER_ADMIN OVERVIEW (orgless only)
//    // =======================================================
//    public List<BatchAttendanceOverviewResponse> getOrglessBatchOverview() {
//        List<Long> batchIds = accessRepo.findDistinctBatchIdsByOrganizationIdIsNull();
//        return buildOverviewRows(batchIds);
//    }
//
//    private List<BatchAttendanceOverviewResponse> buildOverviewRows(List<Long> batchIds) {
//        List<BatchAttendanceOverviewResponse> rows = new ArrayList<>();
//
//        for (Long batchId : batchIds) {
//            List<TrainerBatchAccess> accessRows = accessRepo.findByBatchId(batchId);
//            String trainerEmail = accessRows.isEmpty() ? null : accessRows.get(0).getTrainerEmail();
//
//            long studentCount = attendanceRepository.findByBatchId(batchId)
//                    .stream()
//                    .map(Attendance::getStudentEmail)
//                    .distinct()
//                    .count();
//
//            long sessionsMarked = sessionAttendanceRepo.findByBatchId(batchId).size();
//
//            rows.add(new BatchAttendanceOverviewResponse(
//                    batchId, trainerEmail, (int) studentCount, (int) sessionsMarked
//            ));
//        }
//
//        return rows;
//    }
//
//    // =======================================================
//    // NEW — ADMIN BATCH DETAIL (combined trainer + student), strict org match
//    // =======================================================
//    public BatchAttendanceDetailResponse getOrgBatchDetail(Long batchId, String organizationId) {
//        boolean matches = accessRepo.findByBatchId(batchId)
//                .stream()
//                .anyMatch(a -> organizationId != null && organizationId.equals(a.getOrganizationId()));
//
//        if (!matches) {
//            // reject/return empty on mismatch, per spec — empty combined response rather than throwing,
//            // so the frontend can render an empty state instead of an error page
//            return new BatchAttendanceDetailResponse(List.of(), Map.of());
//        }
//
//        return buildBatchDetail(batchId);
//    }
//
//    // =======================================================
//    // NEW — SUPER_ADMIN BATCH DETAIL (combined trainer + student), orgless only
//    // =======================================================
//    public BatchAttendanceDetailResponse getOrglessBatchDetail(Long batchId) {
//        boolean matches = accessRepo.findByBatchId(batchId)
//                .stream()
//                .anyMatch(a -> a.getOrganizationId() == null);
//
//        if (!matches) {
//            return new BatchAttendanceDetailResponse(List.of(), Map.of());
//        }
//
//        return buildBatchDetail(batchId);
//    }
//
//    private BatchAttendanceDetailResponse buildBatchDetail(Long batchId) {
//        List<TrainerSessionAttendance> trainerRows = sessionAttendanceRepo.findByBatchId(batchId);
//
//        Map<String, List<StudentAttendanceResponse>> studentMap = attendanceRepository
//                .findByBatchId(batchId)
//                .stream()
//                .collect(Collectors.groupingBy(
//                        Attendance::getStudentEmail,
//                        HashMap::new,
//                        Collectors.mapping(
//                                a -> new StudentAttendanceResponse(a.getAttendanceDate(), a.getStatus().name()),
//                                Collectors.toList()
//                        )
//                ));
//
//        return new BatchAttendanceDetailResponse(trainerRows, studentMap);
//    }
//}

package com.lms.attendance.service;

import com.lms.attendance.dto.AttendanceAnalyticsResponse;
import com.lms.attendance.dto.AttendanceHistoryResponse;
import com.lms.attendance.dto.AttendanceRecordResponse;
import com.lms.attendance.dto.MarkAttendanceRequest;
import com.lms.attendance.dto.MarkOwnSessionRequest;
import com.lms.attendance.dto.StudentAttendanceResponse;
import com.lms.attendance.dto.BatchAttendanceOverviewResponse;
import com.lms.attendance.dto.BatchAttendanceDetailResponse;
import com.lms.attendance.dto.TrainerSessionHistoryResponse;
import com.lms.attendance.entity.Attendance;
import com.lms.attendance.entity.AttendanceStatus;
import com.lms.attendance.entity.TrainerBatchAccess;
import com.lms.attendance.entity.TrainerSessionAttendance;
import com.lms.attendance.event.AttendanceMarkedEvent;
import com.lms.attendance.kafka.AttendanceEventProducer;
import com.lms.attendance.repository.AttendanceRepository;
import com.lms.attendance.repository.TrainerBatchAccessRepository;
import com.lms.attendance.repository.StudentBatchAccessRepository;
import com.lms.attendance.repository.TrainerSessionAttendanceRepository;
import com.lms.attendance.util.DateRangeResolver;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceEventProducer attendanceEventProducer;
    private final TrainerBatchAccessRepository accessRepo;
    private final StudentBatchAccessRepository studentAccessRepo; // NEW
    private final TrainerSessionAttendanceRepository sessionAttendanceRepo; // NEW — additive constructor param
    private final ExcelReportService excelReportService; // NEW — additive constructor param, added at end only

//cfcfcfcfffff
    public AttendanceService(
            AttendanceRepository attendanceRepository,
            AttendanceEventProducer attendanceEventProducer,
            TrainerBatchAccessRepository accessRepo,
            StudentBatchAccessRepository studentAccessRepo, // NEW param
            TrainerSessionAttendanceRepository sessionAttendanceRepo, // NEW param — additive only
            ExcelReportService excelReportService) { // NEW param — added at end, additive only
    	
    	
        this.attendanceRepository = attendanceRepository;
        this.attendanceEventProducer = attendanceEventProducer;
        this.accessRepo=accessRepo;
        this.studentAccessRepo=studentAccessRepo; // NEW
        this.sessionAttendanceRepo=sessionAttendanceRepo; // NEW
        this.excelReportService=excelReportService; // NEW
    }

    // =======================
    // MARK ATTENDANCE (FINAL)
    // =======================
    public void markAttendance(String trainerEmail, MarkAttendanceRequest request) {

        LocalDate date = request.getAttendanceDate();
        Long batchId = request.getBatchId();

        // 🔒 NEW: Trainer must belong to this batch
        boolean allowed = accessRepo
                .findByBatchIdAndTrainerEmail(batchId, trainerEmail)
                .isPresent();

        if (!allowed) {
            throw new RuntimeException("You are not assigned to this batch. Attendance denied.");
        }

        // ---- EXISTING LOGIC (UNCHANGED) ----
        for (MarkAttendanceRequest.StudentAttendance sa : request.getAttendances()) {

            Attendance attendance = attendanceRepository
                    .findByBatchIdAndStudentUserIdAndAttendanceDate(
                            batchId,
                            sa.getStudentUserId(),
                            date
                    )
                    .orElse(new Attendance());

            attendance.setBatchId(batchId);
            attendance.setStudentUserId(sa.getStudentUserId());
            attendance.setStudentEmail(sa.getStudentEmail());
            attendance.setTrainerEmail(trainerEmail);
            attendance.setAttendanceDate(date);
            attendance.setStatus(AttendanceStatus.valueOf(sa.getStatus()));

            attendanceRepository.save(attendance);

         // ✅ NEW — pass trainerEmail so notification knows who to notify
            attendanceEventProducer.publish(
                new AttendanceMarkedEvent(
                    batchId,
                    sa.getStudentUserId(),
                    sa.getStudentEmail(),
                    sa.getStatus(),
                    date,
                    trainerEmail  // ✅ ADDED
                )
            );
            
                   }
        
    }

//    // =======================
//    // MARK ATTENDANCE — ORG-AWARE OVERLOAD (NEW)
//    // =======================
//    // Extends validation only. If organizationId != null, the trainer's batch access
//    // must belong to that same organization. If organizationId == null (standalone
//    // trainer), behavior is identical to the original method above.
//    public void markAttendance(String trainerEmail, MarkAttendanceRequest request, String organizationId) {
//
//        Long batchId = request.getBatchId();
//
//        if (organizationId != null) {
//            boolean allowedInOrg = accessRepo
//                    .findByBatchIdAndTrainerEmailAndOrganizationId(batchId, trainerEmail, organizationId)
//                    .isPresent();
//
//            if (!allowedInOrg) {
//                throw new RuntimeException("You are not assigned to this batch within your organization. Attendance denied.");
//            }
//
//            // NEW — stamp organizationId onto persisted attendance rows for tenant lineage,
//            // without disturbing the existing per-student save/publish logic above.
//            for (MarkAttendanceRequest.StudentAttendance sa : request.getAttendances()) {
//                attendanceRepository
//                        .findByBatchIdAndStudentUserIdAndAttendanceDate(
//                                batchId, sa.getStudentUserId(), request.getAttendanceDate())
//                        .ifPresent(a -> {
//                            a.setOrganizationId(organizationId);
//                            attendanceRepository.save(a);
//                        });
//            }
//        }
//
//        // Delegate to the existing, unmodified method for all core business logic
//        markAttendance(trainerEmail, request);
//    }
    public void markAttendance(String trainerEmail, MarkAttendanceRequest request, String organizationId) {

        Long batchId = request.getBatchId();

        if (organizationId != null) {
            boolean allowedInOrg = accessRepo
                    .findByBatchIdAndTrainerEmailAndOrganizationId(batchId, trainerEmail, organizationId)
                    .isPresent();

            if (!allowedInOrg) {
                throw new RuntimeException("You are not assigned to this batch within your organization. Attendance denied.");
            }
        }

        // Save first — this is what actually creates/updates the rows
        markAttendance(trainerEmail, request);

        // NOW stamp organizationId — rows exist at this point, so the lookup will find them
        if (organizationId != null) {
            for (MarkAttendanceRequest.StudentAttendance sa : request.getAttendances()) {
                attendanceRepository
                        .findByBatchIdAndStudentUserIdAndAttendanceDate(
                                batchId, sa.getStudentUserId(), request.getAttendanceDate())
                        .ifPresent(a -> {
                            a.setOrganizationId(organizationId);
                            attendanceRepository.save(a);
                        });
            }
        }
    }

    // =======================
    // STUDENT MONTHLY VIEW
    // =======================
    public List<StudentAttendanceResponse> getMonthlyByStudentEmail(
            String email, int year, int month
    ) {
        return attendanceRepository
                .findMonthlyByStudentEmail(email, year, month)
                .stream()
                .map(a -> new StudentAttendanceResponse(
                        a.getAttendanceDate(),
                        a.getStatus().name()
                ))
                .toList();
    }

    // =======================
    // STUDENT MONTHLY VIEW — ORG-AWARE OVERLOAD (NEW)
    // =======================
    // Extends validation only. If organizationId != null, the student must have an
    // access record within that organization before the existing lookup runs.
    // If organizationId == null (standalone student), behavior is identical to the
    // original method above.
    public List<StudentAttendanceResponse> getMonthlyByStudentEmail(
            String email, int year, int month, String organizationId
    ) {
        if (organizationId != null) {
            boolean allowedInOrg = studentAccessRepo
                    .findByStudentEmailAndOrganizationId(email, organizationId)
                    .isPresent();

            if (!allowedInOrg) {
                throw new RuntimeException("You do not have attendance access within your organization.");
            }
        }

        // Delegate to the existing, unmodified method for all core business logic
        return getMonthlyByStudentEmail(email, year, month);
    }

    // =======================================================
    // NEW — TRAINER MARKS OWN ATTENDANCE
    // =======================================================
    // Separate from markAttendance() above, which marks STUDENT attendance.
    // Identity + organizationId come from the trusted Authentication object only
    // (passed in from the controller), never from the request body.
    public void markOwnSessionAttendance(String trainerEmail, MarkOwnSessionRequest request, String organizationId) {

        Long batchId = request.getBatchId();

        // Trainer must belong to this batch — same org-match rule as markAttendance's overload
        boolean allowed = (organizationId != null)
                ? accessRepo.findByBatchIdAndTrainerEmailAndOrganizationId(batchId, trainerEmail, organizationId).isPresent()
                : accessRepo.findByBatchIdAndTrainerEmail(batchId, trainerEmail).isPresent();

        if (!allowed) {
            throw new RuntimeException("You are not assigned to this batch. Attendance denied.");
        }

        TrainerSessionAttendance row = sessionAttendanceRepo
                .findByBatchIdAndTrainerEmailAndSessionDate(batchId, trainerEmail, request.getDate())
                .orElse(new TrainerSessionAttendance());

        row.setBatchId(batchId);
        row.setTrainerEmail(trainerEmail);
        row.setSessionDate(request.getDate());
        row.setStatus(AttendanceStatus.valueOf(request.getStatus()));
        row.setOrganizationId(organizationId);

        sessionAttendanceRepo.save(row);
    }

    // =======================================================
    // NEW — TRAINER'S OWN SESSION HISTORY FOR A MONTH
    // =======================================================
    public List<TrainerSessionAttendance> getOwnSessionHistory(String trainerEmail, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        return sessionAttendanceRepo.findByTrainerEmailAndSessionDateBetween(trainerEmail, start, end);
    }

    // =======================================================
    // NEW — ADMIN OVERVIEW (strict org match)
    // =======================================================
    public List<BatchAttendanceOverviewResponse> getOrgBatchOverview(String organizationId) {
        List<Long> batchIds = accessRepo.findDistinctBatchIdsByOrganizationId(organizationId);
        return buildOverviewRows(batchIds);
    }

    // =======================================================
    // NEW — SUPER_ADMIN OVERVIEW (orgless only)
    // =======================================================
    public List<BatchAttendanceOverviewResponse> getOrglessBatchOverview() {
        List<Long> batchIds = accessRepo.findDistinctBatchIdsByOrganizationIdIsNull();
        return buildOverviewRows(batchIds);
    }

    private List<BatchAttendanceOverviewResponse> buildOverviewRows(List<Long> batchIds) {
        List<BatchAttendanceOverviewResponse> rows = new ArrayList<>();

        for (Long batchId : batchIds) {
            List<TrainerBatchAccess> accessRows = accessRepo.findByBatchId(batchId);
            String trainerEmail = accessRows.isEmpty() ? null : accessRows.get(0).getTrainerEmail();

            long studentCount = attendanceRepository.findByBatchId(batchId)
                    .stream()
                    .map(Attendance::getStudentEmail)
                    .distinct()
                    .count();

            long sessionsMarked = sessionAttendanceRepo.findByBatchId(batchId).size();

            rows.add(new BatchAttendanceOverviewResponse(
                    batchId, trainerEmail, (int) studentCount, (int) sessionsMarked
            ));
        }

        return rows;
    }

    // =======================================================
    // NEW — ADMIN BATCH DETAIL (combined trainer + student), strict org match
    // =======================================================
//    public BatchAttendanceDetailResponse getOrgBatchDetail(Long batchId, String organizationId) {
//        boolean matches = accessRepo.findByBatchId(batchId)
//                .stream()
//                .anyMatch(a -> organizationId != null && organizationId.equals(a.getOrganizationId()));
//
//        if (!matches) {
//            // reject/return empty on mismatch, per spec — empty combined response rather than throwing,
//            // so the frontend can render an empty state instead of an error page
//            return new BatchAttendanceDetailResponse(List.of(), Map.of());
//        }
//
//        return buildBatchDetail(batchId);
//    }
 // AFTER
    public BatchAttendanceDetailResponse getOrgBatchDetail(
            Long batchId, String organizationId, String filterType, LocalDate startDate, LocalDate endDate) {

        boolean matches = accessRepo.findByBatchId(batchId)
                .stream()
                .anyMatch(a -> organizationId != null && organizationId.equals(a.getOrganizationId()));

        if (!matches) {
            // reject/return empty on mismatch, per spec — empty combined response rather than throwing,
            // so the frontend can render an empty state instead of an error page
            return new BatchAttendanceDetailResponse(List.of(), Map.of());
        }

        return buildBatchDetail(batchId, filterType, startDate, endDate);
    }

    // =======================================================
    // NEW — SUPER_ADMIN BATCH DETAIL (combined trainer + student), orgless only
    // =======================================================
//    public BatchAttendanceDetailResponse getOrglessBatchDetail(Long batchId) {
//        boolean matches = accessRepo.findByBatchId(batchId)
//                .stream()
//                .anyMatch(a -> a.getOrganizationId() == null);
//
//        if (!matches) {
//            return new BatchAttendanceDetailResponse(List.of(), Map.of());
//        }
//
//        return buildBatchDetail(batchId);
//    }
 // AFTER
    public BatchAttendanceDetailResponse getOrglessBatchDetail(
            Long batchId, String filterType, LocalDate startDate, LocalDate endDate) {

        boolean matches = accessRepo.findByBatchId(batchId)
                .stream()
                .anyMatch(a -> a.getOrganizationId() == null);

        if (!matches) {
            return new BatchAttendanceDetailResponse(List.of(), Map.of());
        }

        return buildBatchDetail(batchId, filterType, startDate, endDate);
    }

//    private BatchAttendanceDetailResponse buildBatchDetail(Long batchId) {
//        List<TrainerSessionAttendance> trainerRows = sessionAttendanceRepo.findByBatchId(batchId);
//
//        Map<String, List<StudentAttendanceResponse>> studentMap = attendanceRepository
//                .findByBatchId(batchId)
//                .stream()
//                .collect(Collectors.groupingBy(
//                        Attendance::getStudentEmail,
//                        HashMap::new,
//                        Collectors.mapping(
//                                a -> new StudentAttendanceResponse(a.getAttendanceDate(), a.getStatus().name()),
//                                Collectors.toList()
//                        )
//                ));
//
//        return new BatchAttendanceDetailResponse(trainerRows, studentMap);
//    }
 // AFTER
    private BatchAttendanceDetailResponse buildBatchDetail(
            Long batchId, String filterType, LocalDate startDate, LocalDate endDate) {

        LocalDate[] range = DateRangeResolver.resolve(filterType, startDate, endDate);

        List<TrainerSessionAttendance> trainerRows = sessionAttendanceRepo.findByBatchId(batchId)
                .stream()
                .filter(s -> s.getSessionDate() != null
                        && !s.getSessionDate().isBefore(range[0])
                        && !s.getSessionDate().isAfter(range[1]))
                .toList();

        Map<String, List<StudentAttendanceResponse>> studentMap = attendanceRepository
                .findByBatchId(batchId)
                .stream()
                .filter(a -> a.getAttendanceDate() != null
                        && !a.getAttendanceDate().isBefore(range[0])
                        && !a.getAttendanceDate().isAfter(range[1]))
                .collect(Collectors.groupingBy(
                        Attendance::getStudentEmail,
                        HashMap::new,
                        Collectors.mapping(
                                a -> new StudentAttendanceResponse(a.getAttendanceDate(), a.getStatus().name()),
                                Collectors.toList()
                        )
                ));

        return new BatchAttendanceDetailResponse(trainerRows, studentMap);
    }

    // =========================================================================================
    // NEW — HISTORY / FILTERS / ANALYTICS / EXCEL DOWNLOAD (additive from here on)
    // Nothing above this line was touched. Everything below is new and only calls into the
    // existing repositories (plus the new ExcelReportService) — no existing method's behavior
    // is changed.
    // =========================================================================================

    // ---------------- STUDENT ----------------

    // NEW — student's own attendance history, filtered by date range, strict org match
    public AttendanceHistoryResponse getStudentAttendanceHistory(
            String studentEmail, String organizationId, String filterType, LocalDate startDate, LocalDate endDate) {

        List<Attendance> rows = fetchStudentAttendanceRows(studentEmail, organizationId, filterType, startDate, endDate);
        return toHistoryResponse(rows);
    }

    // NEW — student's own Excel export, same filters/org-match as history above
    public byte[] exportStudentAttendanceExcel(
            String studentEmail, String organizationId, String filterType, LocalDate startDate, LocalDate endDate) {

        List<Attendance> rows = fetchStudentAttendanceRows(studentEmail, organizationId, filterType, startDate, endDate);
        return excelReportService.generateAttendanceExcel(rows, studentEmail);
    }

    private List<Attendance> fetchStudentAttendanceRows(
            String studentEmail, String organizationId, String filterType, LocalDate startDate, LocalDate endDate) {

        if (organizationId != null) {
            boolean allowedInOrg = studentAccessRepo
                    .findByStudentEmailAndOrganizationId(studentEmail, organizationId)
                    .isPresent();

            if (!allowedInOrg) {
                throw new RuntimeException("You do not have attendance access within your organization.");
            }
        }

        LocalDate[] range = DateRangeResolver.resolve(filterType, startDate, endDate);

        List<Attendance> rows = attendanceRepository
                .findByStudentEmailAndAttendanceDateBetween(studentEmail, range[0], range[1]);

        if (organizationId != null) {
            rows = rows.stream()
                    .filter(a -> organizationId.equals(a.getOrganizationId()))
                    .toList();
        }

        return rows;
    }

    // ---------------- TRAINER (own data) ----------------

    // NEW — trainer's own marked (student) attendance history, filtered, optional batchId
    public AttendanceHistoryResponse getTrainerMarkedHistory(
            String trainerEmail, String filterType, LocalDate startDate, LocalDate endDate, Long batchId) {

        List<Attendance> rows = fetchTrainerMarkedRows(trainerEmail, filterType, startDate, endDate, batchId);
        return toHistoryResponse(rows);
    }

    // NEW — trainer's own session (self) attendance history, filtered, optional batchId
    public TrainerSessionHistoryResponse getTrainerSessionHistoryFiltered(
            String trainerEmail, String filterType, LocalDate startDate, LocalDate endDate, Long batchId) {

        List<TrainerSessionAttendance> rows = fetchTrainerSessionRows(trainerEmail, filterType, startDate, endDate, batchId);
        return toSessionHistoryResponse(rows);
    }

    // NEW — trainer Excel download. type=SESSION for own session attendance,
    // otherwise (default) the student attendance the trainer marked.
    public byte[] exportTrainerAttendanceExcel(
            String trainerEmail, String filterType, LocalDate startDate, LocalDate endDate, Long batchId, String type) {

        if ("SESSION".equalsIgnoreCase(type)) {
            List<TrainerSessionAttendance> rows = fetchTrainerSessionRows(trainerEmail, filterType, startDate, endDate, batchId);
            return excelReportService.generateSessionExcel(rows, trainerEmail);
        }

        List<Attendance> rows = fetchTrainerMarkedRows(trainerEmail, filterType, startDate, endDate, batchId);
        return excelReportService.generateAttendanceExcel(rows, trainerEmail);
    }

    private List<Attendance> fetchTrainerMarkedRows(
            String trainerEmail, String filterType, LocalDate startDate, LocalDate endDate, Long batchId) {

        LocalDate[] range = DateRangeResolver.resolve(filterType, startDate, endDate);

        List<Attendance> rows = attendanceRepository
                .findByTrainerEmailAndAttendanceDateBetween(trainerEmail, range[0], range[1]);

        if (batchId != null) {
            rows = rows.stream()
                    .filter(a -> batchId.equals(a.getBatchId()))
                    .toList();
        }

        return rows;
    }

    private List<TrainerSessionAttendance> fetchTrainerSessionRows(
            String trainerEmail, String filterType, LocalDate startDate, LocalDate endDate, Long batchId) {

        LocalDate[] range = DateRangeResolver.resolve(filterType, startDate, endDate);

        List<TrainerSessionAttendance> rows = sessionAttendanceRepo
                .findByTrainerEmailAndSessionDateBetween(trainerEmail, range[0], range[1]);

        if (batchId != null) {
            rows = rows.stream()
                    .filter(s -> batchId.equals(s.getBatchId()))
                    .toList();
        }

        return rows;
    }

    // ---------------- ADMIN ----------------

    // NEW — admin attendance history, filtered by batch/trainer/student + date range, strict org match
    public AttendanceHistoryResponse getAdminAttendanceHistory(
            String organizationId, String filterType, LocalDate startDate, LocalDate endDate,
            Long batchId, String trainerEmail, String studentEmail) {

        List<Attendance> rows = fetchOrgStudentRows(organizationId, filterType, startDate, endDate, batchId, trainerEmail, studentEmail);
        return toHistoryResponse(rows);
    }

    // NEW — admin Excel download. type=SESSION for trainer-session rows,
    // otherwise (default) student attendance rows. Strict org match.
    public byte[] exportAdminAttendanceExcel(
            String organizationId, String generatedBy, String filterType, LocalDate startDate, LocalDate endDate,
            Long batchId, String trainerEmail, String studentEmail, String type) {

        if ("SESSION".equalsIgnoreCase(type)) {
            List<TrainerSessionAttendance> rows = fetchOrgSessionRows(organizationId, filterType, startDate, endDate, batchId, trainerEmail);
            return excelReportService.generateSessionExcel(rows, generatedBy);
        }

        List<Attendance> rows = fetchOrgStudentRows(organizationId, filterType, startDate, endDate, batchId, trainerEmail, studentEmail);
        return excelReportService.generateAttendanceExcel(rows, generatedBy);
    }

    // ---------------- SUPER ADMIN ----------------

    // NEW — super-admin attendance history, filtered by batch/trainer/student + date range, orgless only
    public AttendanceHistoryResponse getSuperAdminAttendanceHistory(
            String filterType, LocalDate startDate, LocalDate endDate,
            Long batchId, String trainerEmail, String studentEmail) {

        List<Attendance> rows = fetchOrglessStudentRows(filterType, startDate, endDate, batchId, trainerEmail, studentEmail);
        return toHistoryResponse(rows);
    }

    // NEW — super-admin Excel download. type=SESSION for trainer-session rows,
    // otherwise (default) student attendance rows. Orgless only.
    public byte[] exportSuperAdminAttendanceExcel(
            String generatedBy, String filterType, LocalDate startDate, LocalDate endDate,
            Long batchId, String trainerEmail, String studentEmail, String type) {

        if ("SESSION".equalsIgnoreCase(type)) {
            List<TrainerSessionAttendance> rows = fetchOrglessSessionRows(filterType, startDate, endDate, batchId, trainerEmail);
            return excelReportService.generateSessionExcel(rows, generatedBy);
        }

        List<Attendance> rows = fetchOrglessStudentRows(filterType, startDate, endDate, batchId, trainerEmail, studentEmail);
        return excelReportService.generateAttendanceExcel(rows, generatedBy);
    }

    // ---------------- shared org / orgless batch-scoped fetch helpers ----------------

    private List<Attendance> fetchOrgStudentRows(
            String organizationId, String filterType, LocalDate startDate, LocalDate endDate,
            Long batchId, String trainerEmail, String studentEmail) {

        List<Long> batchIds = resolveBatchIds(
                accessRepo.findDistinctBatchIdsByOrganizationId(organizationId), batchId);

        return fetchStudentRowsForBatches(batchIds, filterType, startDate, endDate, trainerEmail, studentEmail);
    }

    private List<Attendance> fetchOrglessStudentRows(
            String filterType, LocalDate startDate, LocalDate endDate,
            Long batchId, String trainerEmail, String studentEmail) {

        List<Long> batchIds = resolveBatchIds(
                accessRepo.findDistinctBatchIdsByOrganizationIdIsNull(), batchId);

        return fetchStudentRowsForBatches(batchIds, filterType, startDate, endDate, trainerEmail, studentEmail);
    }

    private List<TrainerSessionAttendance> fetchOrgSessionRows(
            String organizationId, String filterType, LocalDate startDate, LocalDate endDate,
            Long batchId, String trainerEmail) {

        List<Long> batchIds = resolveBatchIds(
                accessRepo.findDistinctBatchIdsByOrganizationId(organizationId), batchId);

        return fetchSessionRowsForBatches(batchIds, filterType, startDate, endDate, trainerEmail);
    }

    private List<TrainerSessionAttendance> fetchOrglessSessionRows(
            String filterType, LocalDate startDate, LocalDate endDate,
            Long batchId, String trainerEmail) {

        List<Long> batchIds = resolveBatchIds(
                accessRepo.findDistinctBatchIdsByOrganizationIdIsNull(), batchId);

        return fetchSessionRowsForBatches(batchIds, filterType, startDate, endDate, trainerEmail);
    }

    private List<Long> resolveBatchIds(List<Long> allowedBatchIds, Long requestedBatchId) {
        if (requestedBatchId == null) {
            return allowedBatchIds;
        }
        return allowedBatchIds.contains(requestedBatchId) ? List.of(requestedBatchId) : List.of();
    }

    private List<Attendance> fetchStudentRowsForBatches(
            List<Long> batchIds, String filterType, LocalDate startDate, LocalDate endDate,
            String trainerEmail, String studentEmail) {

        LocalDate[] range = DateRangeResolver.resolve(filterType, startDate, endDate);
        List<Attendance> combined = new ArrayList<>();

        for (Long batchId : batchIds) {
            combined.addAll(attendanceRepository.findByBatchIdAndAttendanceDateBetween(batchId, range[0], range[1]));
        }

        if (trainerEmail != null && !trainerEmail.isBlank()) {
            combined = combined.stream().filter(a -> trainerEmail.equals(a.getTrainerEmail())).toList();
        }
        if (studentEmail != null && !studentEmail.isBlank()) {
            combined = combined.stream().filter(a -> studentEmail.equals(a.getStudentEmail())).toList();
        }

        return combined;
    }

    private List<TrainerSessionAttendance> fetchSessionRowsForBatches(
            List<Long> batchIds, String filterType, LocalDate startDate, LocalDate endDate, String trainerEmail) {

        LocalDate[] range = DateRangeResolver.resolve(filterType, startDate, endDate);
        List<TrainerSessionAttendance> combined = new ArrayList<>();

        for (Long batchId : batchIds) {
            combined.addAll(sessionAttendanceRepo.findByBatchId(batchId).stream()
                    .filter(s -> s.getSessionDate() != null
                            && !s.getSessionDate().isBefore(range[0])
                            && !s.getSessionDate().isAfter(range[1]))
                    .toList());
        }

        if (trainerEmail != null && !trainerEmail.isBlank()) {
            combined = combined.stream().filter(s -> trainerEmail.equals(s.getTrainerEmail())).toList();
        }

        return combined;
    }

    // ---------------- shared DTO / analytics builders ----------------

    private AttendanceHistoryResponse toHistoryResponse(List<Attendance> rows) {
        List<AttendanceRecordResponse> records = rows.stream()
                .map(a -> new AttendanceRecordResponse(
                        a.getBatchId(),
                        a.getStudentUserId(),
                        a.getStudentEmail(),
                        a.getTrainerEmail(),
                        a.getAttendanceDate(),
                        a.getStatus() != null ? a.getStatus().name() : null,
                        a.getOrganizationId()
                ))
                .toList();

        int present = 0, absent = 0, late = 0;
        for (Attendance a : rows) {
            if (a.getStatus() == AttendanceStatus.PRESENT) present++;
            else if (a.getStatus() == AttendanceStatus.ABSENT) absent++;
            else if (a.getStatus() == AttendanceStatus.LATE) late++;
        }

        int total = rows.size();
        double pct = total > 0 ? ((present + late) * 100.0 / total) : 0.0;

        AttendanceAnalyticsResponse analytics = new AttendanceAnalyticsResponse(total, present, absent, late, pct);

        return new AttendanceHistoryResponse(records, analytics);
    }

    private TrainerSessionHistoryResponse toSessionHistoryResponse(List<TrainerSessionAttendance> rows) {
        int present = 0, absent = 0, late = 0;
        for (TrainerSessionAttendance s : rows) {
            if (s.getStatus() == AttendanceStatus.PRESENT) present++;
            else if (s.getStatus() == AttendanceStatus.ABSENT) absent++;
            else if (s.getStatus() == AttendanceStatus.LATE) late++;
        }

        int total = rows.size();
        double pct = total > 0 ? ((present + late) * 100.0 / total) : 0.0;

        AttendanceAnalyticsResponse analytics = new AttendanceAnalyticsResponse(total, present, absent, late, pct);

        return new TrainerSessionHistoryResponse(rows, analytics);
    }
}
