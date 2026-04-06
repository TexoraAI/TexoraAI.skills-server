package com.lms.file.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class FileEventProducer {

    // ✅ Object not String — same as batch producer
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public FileEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendFileUploadedEvent(Long fileId, String title,
                                       Long batchId, String trainerEmail) {
        // ✅ No ObjectMapper — send Map directly like batch producer
        Map<String, Object> payload = new HashMap<>();
        payload.put("fileId", fileId);
        payload.put("title", title);
        payload.put("batchId", batchId);
        payload.put("trainerEmail", trainerEmail);

        Map<String, Object> event = new HashMap<>();
        event.put("type", "FILE_UPLOADED");
        event.put("payload", payload);

        kafkaTemplate.send("file-uploaded", event); // ✅ send object directly
        System.out.println("🔥 FILE EVENT -> FILE_UPLOADED | title=" + title + " | batchId=" + batchId);
    }
}