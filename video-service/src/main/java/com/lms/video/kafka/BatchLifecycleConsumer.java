//package com.lms.video.kafka;
//
//import com.lms.video.repository.StudentBatchMapRepository;
//import com.lms.video.repository.TrainerBatchMapRepository;
//import com.lms.video.repository.VideoRepository;
//
//import jakarta.transaction.Transactional;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//@Service
//public class BatchLifecycleConsumer {
//
//    private final StudentBatchMapRepository studentRepo;
//    private final TrainerBatchMapRepository trainerRepo;
//    private final VideoRepository videoRepo;
//
//    public BatchLifecycleConsumer(StudentBatchMapRepository studentRepo,
//                                  TrainerBatchMapRepository trainerRepo,
//                                  VideoRepository videoRepo) {
//        this.studentRepo = studentRepo;
//        this.trainerRepo = trainerRepo;
//        this.videoRepo = videoRepo;
//    }
//
//    @Transactional
//    @KafkaListener(topics = "batch-lifecycle", groupId = "video-service-group")
//    public void consume(Map<String, Object> event) {
//
//        String type = (String) event.get("type");
//
//        if ("BATCH_DELETED".equals(type)) {
//
//            Long batchId = ((Number) event.get("batchId")).longValue();
//
//            // delete all mappings
//            studentRepo.deleteByBatchId(batchId);
//            trainerRepo.deleteByBatchId(batchId);
//
//            // delete all videos
//            videoRepo.deleteByBatchId(batchId);
//
//            System.out.println("🔥 VIDEO SERVICE FULL BATCH CLEANUP -> " + batchId);
//        }
//        if ("BRANCH_DELETED".equals(type)) {
//            System.out.println("📥 VIDEO RECEIVED BRANCH DELETE");
//        }
//    }
//}
//


package com.lms.video.kafka;

import com.lms.video.repository.StudentBatchMapRepository;
import com.lms.video.repository.TrainerBatchMapRepository;
import com.lms.video.repository.VideoRepository;

import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BatchLifecycleConsumer {

    private final StudentBatchMapRepository studentRepo;
    private final TrainerBatchMapRepository trainerRepo;
    private final VideoRepository videoRepo;

    public BatchLifecycleConsumer(StudentBatchMapRepository studentRepo,
                                  TrainerBatchMapRepository trainerRepo,
                                  VideoRepository videoRepo) {
        this.studentRepo = studentRepo;
        this.trainerRepo = trainerRepo;
        this.videoRepo = videoRepo;
    }

    @Transactional
    @KafkaListener(topics = "batch-lifecycle", groupId = "video-service-group")
    public void consume(Map<String, Object> event) {

        String type = (String) event.get("type");

        System.out.println("🔥 VIDEO LIFECYCLE EVENT -> " + type);

        // ================= BATCH DELETE =================
        if ("BATCH_DELETED".equals(type)) {

            Long batchId = ((Number) event.get("batchId")).longValue();

            // 🔥 FULL CLEANUP (same as attendance)
            studentRepo.deleteByBatchId(batchId);
            trainerRepo.deleteByBatchId(batchId);
            videoRepo.deleteByBatchId(batchId);

            System.out.println("🧹 FULL BATCH DATA REMOVED -> " + batchId);
        }

        // ================= BRANCH DELETE =================
        if ("BRANCH_DELETED".equals(type)) {

            // ⚠️ Expecting list of batchIds from event
            List<Number> batchIdsRaw = (List<Number>) event.get("batchIds");

            if (batchIdsRaw != null && !batchIdsRaw.isEmpty()) {

                for (Number b : batchIdsRaw) {
                    Long batchId = b.longValue();

                    // 🔥 SAME CLEANUP FOR EACH BATCH
                    studentRepo.deleteByBatchId(batchId);
                    trainerRepo.deleteByBatchId(batchId);
                    videoRepo.deleteByBatchId(batchId);

                    System.out.println("🧹 BRANCH CLEANUP BATCH -> " + batchId);
                }
            } else {
                System.out.println("⚠️ No batchIds provided for branch delete");
            }
        }
    }
}