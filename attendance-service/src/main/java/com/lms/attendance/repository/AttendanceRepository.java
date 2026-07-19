//
//package com.lms.attendance.repository;
//import com.lms.attendance.entity.Attendance;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
//    // ✅ FIXED: Batch-aware lookup
//    Optional<Attendance> findByBatchIdAndStudentUserIdAndAttendanceDate(
//            Long batchId,
//            Long studentUserId,
//            LocalDate attendanceDate
//    );
//    // ✅ Student monthly view
//    @Query("""
//        SELECT a FROM Attendance a
//        WHERE a.studentEmail = :email
//          AND EXTRACT(YEAR FROM a.attendanceDate) = :year
//          AND EXTRACT(MONTH FROM a.attendanceDate) = :month
//        ORDER BY a.attendanceDate
//    """)
//    List<Attendance> findMonthlyByStudentEmail(
//            @Param("email") String email,
//            @Param("year") int year,
//            @Param("month") int month
//    );
//
//    // NEW — all attendance rows for a batch, used by admin/superadmin combined
//    // batch-detail endpoints (grouped by student in the service layer)
//    List<Attendance> findByBatchId(Long batchId);
//}
package com.lms.attendance.repository;
import com.lms.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    // ✅ FIXED: Batch-aware lookup
    Optional<Attendance> findByBatchIdAndStudentUserIdAndAttendanceDate(
            Long batchId,
            Long studentUserId,
            LocalDate attendanceDate
    );
    // ✅ Student monthly view
    @Query("""
        SELECT a FROM Attendance a
        WHERE a.studentEmail = :email
          AND EXTRACT(YEAR FROM a.attendanceDate) = :year
          AND EXTRACT(MONTH FROM a.attendanceDate) = :month
        ORDER BY a.attendanceDate
    """)
    List<Attendance> findMonthlyByStudentEmail(
            @Param("email") String email,
            @Param("year") int year,
            @Param("month") int month
    );

    // NEW — all attendance rows for a batch, used by admin/superadmin combined
    // batch-detail endpoints (grouped by student in the service layer)
    List<Attendance> findByBatchId(Long batchId);

    // NEW — student's own attendance history within a date range (History/Filters feature)
    List<Attendance> findByStudentEmailAndAttendanceDateBetween(
            String studentEmail, LocalDate start, LocalDate end
    );

    // NEW — trainer's own marked (student) attendance within a date range
    List<Attendance> findByTrainerEmailAndAttendanceDateBetween(
            String trainerEmail, LocalDate start, LocalDate end
    );

    // NEW — one batch's attendance within a date range, used by admin/superadmin
    // filtered history/export endpoints (looped per allowed batchId, same pattern as
    // buildOverviewRows/buildBatchDetail already do in AttendanceService)
    List<Attendance> findByBatchIdAndAttendanceDateBetween(
            Long batchId, LocalDate start, LocalDate end
    );
}