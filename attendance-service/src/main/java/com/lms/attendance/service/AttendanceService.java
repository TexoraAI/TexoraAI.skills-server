package com.lms.attendance.service;

import com.lms.attendance.dto.MarkAttendanceRequest;
import com.lms.attendance.dto.StudentAttendanceResponse;
import com.lms.attendance.entity.Attendance;
import com.lms.attendance.entity.AttendanceStatus;
import com.lms.attendance.event.AttendanceMarkedEvent;
import com.lms.attendance.kafka.AttendanceEventProducer;
import com.lms.attendance.repository.AttendanceRepository;
import com.lms.attendance.repository.TrainerBatchAccessRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceEventProducer attendanceEventProducer;
    private final TrainerBatchAccessRepository accessRepo;


    public AttendanceService(
            AttendanceRepository attendanceRepository,
            AttendanceEventProducer attendanceEventProducer,
            TrainerBatchAccessRepository accessRepo) {
    	
    	
        this.attendanceRepository = attendanceRepository;
        this.attendanceEventProducer = attendanceEventProducer;
        this.accessRepo=accessRepo;
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

            // Kafka event (unchanged)
            attendanceEventProducer.publish(
                    new AttendanceMarkedEvent(
                            batchId,
                            sa.getStudentUserId(),
                            sa.getStudentEmail(),
                            sa.getStatus(),
                            date
                    )
            );
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
}

