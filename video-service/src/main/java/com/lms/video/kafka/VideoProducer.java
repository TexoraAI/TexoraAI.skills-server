package com.lms.video.kafka;
 // create this or use Map
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class VideoProducer {

    // ✅ SAME as batch service — Object not String
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public VideoProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendVideoUploadedEvent(String fileName, String title, Long batchId) {
        // ✅ No ObjectMapper — send Map directly like batch service
        Map<String, Object> payload = new HashMap<>();
        payload.put("fileName", fileName);
        payload.put("title", title);
        payload.put("batchId", batchId);

        Map<String, Object> event = new HashMap<>();
        event.put("type", "VIDEO_UPLOADED");
        event.put("payload", payload);

        kafkaTemplate.send("video-uploaded", event); // ✅ send object directly
        System.out.println("🔥 VIDEO EVENT -> VIDEO_UPLOADED | file=" + fileName + " | batchId=" + batchId);
    }

    public void sendVideoDeletedEvent(String fileName) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "VIDEO_DELETED");
        event.put("fileName", fileName);

        kafkaTemplate.send("video-deleted", event); // ✅ same pattern
        System.out.println("🔥 VIDEO EVENT -> VIDEO_DELETED | file=" + fileName);
    }
}