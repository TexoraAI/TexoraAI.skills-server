package com.lms.batch.kafka;

import com.lms.batch.service.BatchService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BranchLifecycleConsumer {

    private final BatchService batchService;

    public BranchLifecycleConsumer(BatchService batchService) {
        this.batchService = batchService;
    }

    @KafkaListener(topics = "batch-lifecycle", groupId = "batch-service-group")
    public void handleLifecycleEvent(Map<String, Object> event) {

        if (event == null || event.get("type") == null)
            return;

        String type = event.get("type").toString();

        if ("BRANCH_DELETED".equals(type)) {

            Object branch = event.get("branchId");
            if (branch == null) return;

            Long branchId = ((Number) branch).longValue();

            System.out.println("📥 BRANCH_DELETED RECEIVED -> " + branchId);

            batchService.deleteAllBatchesUnderBranch(branchId);
        }
    }
}
