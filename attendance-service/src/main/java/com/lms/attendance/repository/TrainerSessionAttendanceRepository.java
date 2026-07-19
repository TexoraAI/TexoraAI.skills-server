//package com.lms.attendance.repository;
//
//import com.lms.attendance.entity.TrainerSessionAttendance;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//
//public interface TrainerSessionAttendanceRepository
//        extends JpaRepository<TrainerSessionAttendance, Long> {
//
//    Optional<TrainerSessionAttendance> findByBatchIdAndTrainerEmailAndSessionDate(
//            Long batchId,
//            String trainerEmail,
//            LocalDate sessionDate
//    );
//
//    // trainer's own history for a given month range
//    List<TrainerSessionAttendance> findByTrainerEmailAndSessionDateBetween(
//            String trainerEmail,
//            LocalDate start,
//            LocalDate end
//    );
//
//    // admin / super-admin cross-trainer views for a given month range
//    List<TrainerSessionAttendance> findBySessionDateBetween(
//            LocalDate start,
//            LocalDate end
//    );
//
//    // used by admin/superadmin batch-detail endpoints to pull one batch's trainer rows
//    List<TrainerSessionAttendance> findByBatchId(Long batchId);
//}

package com.lms.attendance.repository;

import com.lms.attendance.entity.TrainerSessionAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainerSessionAttendanceRepository
        extends JpaRepository<TrainerSessionAttendance, Long> {

    Optional<TrainerSessionAttendance> findByBatchIdAndTrainerEmailAndSessionDate(
            Long batchId,
            String trainerEmail,
            LocalDate sessionDate
    );

    // trainer's own history for a given month range
    List<TrainerSessionAttendance> findByTrainerEmailAndSessionDateBetween(
            String trainerEmail,
            LocalDate start,
            LocalDate end
    );

    // admin / super-admin cross-trainer views for a given month range
    List<TrainerSessionAttendance> findBySessionDateBetween(
            LocalDate start,
            LocalDate end
    );

    // used by admin/superadmin batch-detail endpoints to pull one batch's trainer rows
    List<TrainerSessionAttendance> findByBatchId(Long batchId);

    // NEW — one batch's trainer-session rows within a date range, used by admin/superadmin
    // filtered history/export endpoints (looped per allowed batchId)
    List<TrainerSessionAttendance> findByBatchIdAndSessionDateBetween(
            Long batchId, LocalDate start, LocalDate end
    );
}