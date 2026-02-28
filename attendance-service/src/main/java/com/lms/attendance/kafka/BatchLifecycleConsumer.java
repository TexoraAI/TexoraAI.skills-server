package com.lms.attendance.kafka;

import com.lms.attendance.repository.AttendanceRepository;
import com.lms.attendance.repository.TrainerBatchAccessRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BatchLifecycleConsumer {

    private final AttendanceRepository attendanceRepo;
    private final TrainerBatchAccessRepository accessRepo;

    public BatchLifecycleConsumer(AttendanceRepository attendanceRepo,
                                  TrainerBatchAccessRepository accessRepo) {
        this.attendanceRepo = attendanceRepo;
        this.accessRepo = accessRepo;
    }

    @KafkaListener(topics = "batch-lifecycle", groupId = "attendance-service-group")
    public void consume(Map<String, Object> event) {

        String type = (String) event.get("type");
        Long batchId = event.get("batchId") == null ? null :
                ((Number) event.get("batchId")).longValue();

        System.out.println("🔥 ATTENDANCE LIFECYCLE -> " + type);

        if ("BATCH_DELETED".equals(type)) {
            attendanceRepo.deleteAll(
                    attendanceRepo.findAll().stream()
                            .filter(a -> a.getBatchId().equals(batchId))
                            .toList()
            );
            accessRepo.deleteByBatchId(batchId);
        }
    }
}
