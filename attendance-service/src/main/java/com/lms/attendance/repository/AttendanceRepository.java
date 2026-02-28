

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
}
