//package com.lms.course.kafka;
//
//import com.lms.course.dto.ContentEvent;
//
//import java.util.Map;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//public class ContentEventProducer {
//
//    // ✅ Object not String — same as batch producer
//    private final KafkaTemplate<String, Object> kafkaTemplate;
//
//    @Value("${topics.content}")
//    private String topic;
//
//    public ContentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
//        this.kafkaTemplate = kafkaTemplate;
//    }
//
//    // ✅ send ContentEvent object directly — no ObjectMapper
//    public void sendEvent(ContentEvent event) {
//        kafkaTemplate.send(topic, event);
//        System.out.println("📤 Sent CONTENT Event → " + event);
//    }
//}


package com.lms.course.kafka;

import com.lms.course.dto.ContentEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ContentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${topics.content}")
    private String topic;

    public ContentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // ── used by ContentService.create() ──────────────────────────────────────
    public void sendEvent(ContentEvent event) {
        kafkaTemplate.send(topic, event);
        System.out.println("📤 Sent CONTENT Event → " + event);
    }

    // ── used by ContentService.delete() ──────────────────────────────────────
    // Tells video-service / file-service to delete the physical file + DB row
    // whose stream/download URL matches the one stored in ContentItem.
    public void publishContentDeleted(Long contentId,
                                      Long courseId,
                                      String url,
                                      String contentType) {
        ContentEvent event = new ContentEvent(
            "CONTENT_DELETED",
            java.util.Map.of(
                "contentId",   contentId,
                "courseId",    courseId,
                "url",         url,         // e.g. "http://.../course-videos/stream/abc.mp4"
                "contentType", contentType  // "VIDEO" or "PDF"
            )
        );
        kafkaTemplate.send(topic, event);
        kafkaTemplate.flush();
        System.out.println("📤 Sent CONTENT_DELETED → contentId=" + contentId
                + " url=" + url + " type=" + contentType);
    }

    // ── used by ContentService.update() ──────────────────────────────────────
    // Tells video-service / file-service:
    //   oldUrl  → the physical file to DELETE from disk + DB
    //   newUrl  → the new URL already saved by video/file service upload
    // This fires ONLY when the URL actually changed (i.e. a new file was uploaded).
    public void publishContentUpdated(Long contentId,
                                      Long courseId,
                                      String oldUrl,
                                      String newUrl,
                                      String contentType) {
        ContentEvent event = new ContentEvent(
            "CONTENT_UPDATED",
            java.util.Map.of(
                "contentId",   contentId,
                "courseId",    courseId,
                "oldUrl",      oldUrl,
                "newUrl",      newUrl,
                "contentType", contentType
            )
        );
        kafkaTemplate.send(topic, event);
        kafkaTemplate.flush();
        System.out.println("📤 Sent CONTENT_UPDATED → contentId=" + contentId
                + " oldUrl=" + oldUrl + " newUrl=" + newUrl);
    }
}