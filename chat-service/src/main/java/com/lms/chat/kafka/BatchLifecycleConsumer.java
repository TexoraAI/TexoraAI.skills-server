package com.lms.chat.kafka;

import com.lms.chat.repository.ChatClassroomAccessRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.Map;

@Service
public class BatchLifecycleConsumer {

    private final ChatClassroomAccessRepository repo;

    public BatchLifecycleConsumer(ChatClassroomAccessRepository repo) {
        this.repo = repo;
    }
   @Transactional
    @KafkaListener(topics = "batch-lifecycle", groupId = "chat-service-group")
    public void consume(Map<String, Object> event) {

        String type = (String) event.get("type");

        if ("BATCH_DELETED".equals(type)) {
            Long batchId = ((Number) event.get("batchId")).longValue();
            repo.deleteByBatchId(batchId);
            System.out.println("🧹 CHAT ACCESS REMOVED FOR BATCH " + batchId);
        }
   if ("BRANCH_DELETED".equals(type)) {
       System.out.println("📥 VIDEO RECEIVED BRANCH DELETE");
   }
  }
}
