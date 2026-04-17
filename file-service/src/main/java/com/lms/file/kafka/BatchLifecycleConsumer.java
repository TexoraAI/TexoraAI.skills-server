//package com.lms.file.kafka;
//
//import com.lms.file.event.BatchLifecycleEvent;
//import com.lms.file.repository.FileClassroomAccessRepository;
//import com.lms.file.repository.FileRepository;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//import jakarta.transaction.Transactional;
//
//@Component
//public class BatchLifecycleConsumer {
//
//    private final FileClassroomAccessRepository accessRepository;
//    private final FileRepository fileRepository;
//
//    public BatchLifecycleConsumer(FileClassroomAccessRepository accessRepository,
//                                  FileRepository fileRepository) {
//        this.accessRepository = accessRepository;
//        this.fileRepository = fileRepository;
//    }
//
//    @KafkaListener(topics = "batch-lifecycle", groupId = "file-service-group")
//    @Transactional
//    public void consume(BatchLifecycleEvent event) {
//
//        String type = event.getType();
//        Long batchId = event.getBatchId();
//
//        System.out.println("📁 FILE LIFECYCLE -> " + type + " | batch=" + batchId);
//
//        switch (type) {
//
//            case "BATCH_DELETED" -> {
//                accessRepository.deleteByBatchId(batchId);
//                fileRepository.deleteAllByBatchId(batchId);
//                System.out.println("🧹 All files removed for deleted batch");
//            }
//
//            case "TRAINER_DELETED" ->
//                System.out.println("Trainer deleted globally");
//
//            case "STUDENT_DELETED" ->
//                System.out.println("Student deleted globally");
//
//            case "BRANCH_DELETED" ->
//                System.out.println("Branch deleted");
//        }
//    }
//}
package com.lms.file.kafka;

import com.lms.file.event.BatchLifecycleEvent;
import com.lms.file.repository.BatchTrainerRepository;
import com.lms.file.repository.FileClassroomAccessRepository;
import com.lms.file.repository.FileRepository;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import jakarta.transaction.Transactional;

@Component
public class BatchLifecycleConsumer {

    private final FileClassroomAccessRepository accessRepository;
    private final FileRepository fileRepository;
    private final BatchTrainerRepository trainerRepository;

    public BatchLifecycleConsumer(FileClassroomAccessRepository accessRepository,
                                  FileRepository fileRepository,
                                  BatchTrainerRepository trainerRepository) {
        this.accessRepository = accessRepository;
        this.fileRepository = fileRepository;
        this.trainerRepository = trainerRepository;
    }

    @KafkaListener(topics = "batch-lifecycle", groupId = "file-service-group")
    @Transactional
    public void consume(BatchLifecycleEvent event) {

        String type = event.getType();

        // ================= BATCH DELETE =================
        if ("BATCH_DELETED".equals(type)) {

            Long batchId = event.getBatchId();

            // 🔥 SAME AS VIDEO SERVICE — FULL CLEAN
            trainerRepository.deleteById(batchId);
            accessRepository.deleteByBatchId(batchId);
            fileRepository.deleteAllByBatchId(batchId);

            System.out.println("🧹 FILE SERVICE FULL BATCH CLEANUP -> " + batchId);
        }

        // ================= BRANCH DELETE =================
        if ("BRANCH_DELETED".equals(type)) {

            System.out.println("📥 FILE SERVICE RECEIVED BRANCH DELETE");

            // ⚠️ Since no batchIds provided → cannot delete automatically
            // 👉 Must be handled by sending multiple BATCH_DELETED events
        }
    }
}