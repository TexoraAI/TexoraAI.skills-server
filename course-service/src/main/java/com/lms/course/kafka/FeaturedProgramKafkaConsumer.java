package com.lms.course.kafka;

import com.lms.course.model.SessionFileStatus;
import com.lms.course.model.SessionVideoStatus;
import com.lms.course.model.SyllabusSession;
import com.lms.course.repository.SyllabusSessionRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FeaturedProgramKafkaConsumer {

    private final SyllabusSessionRepository syllabusSessionRepository;

    public FeaturedProgramKafkaConsumer(SyllabusSessionRepository syllabusSessionRepository) {
        this.syllabusSessionRepository = syllabusSessionRepository;
    }

    // ── NEW: now subscribes to BOTH the video-status topic (unchanged) and the
    // new featured-file-status topic, in one listener method — keeps course-service
    // at 2 total Kafka classes (Producer + this Consumer), per the constraint. ──
    @KafkaListener(
            topics = {"${topics.featured-video-status}", "${topics.featured-file-status}"},
            groupId = "course-service-group"
    )
    public void consume(Map<String, Object> message) {
        System.out.println("🔔 COURSE FeaturedProgramKafkaConsumer received: " + message);
        try {
            String type = (String) message.get("type");
            if (type == null) {
                System.out.println("⚠️ featured-status: type is null, skipping");
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) message.get("payload");
            if (payload == null) {
                System.out.println("⚠️ featured-status: payload is null, skipping");
                return;
            }

            Object sessionIdRaw = payload.get("sessionId");
            if (sessionIdRaw == null) {
                System.out.println("⚠️ featured-status: sessionId is null, skipping");
                return;
            }
            Long sessionId = Long.valueOf(sessionIdRaw.toString());

            switch (type) {

                case "VIDEO_READY" -> {
                    syllabusSessionRepository.findById(sessionId).ifPresentOrElse(
                        session -> {
                            Object videoId = payload.get("videoId");
                            Object url = payload.get("url");
                            Object thumbnailUrl = payload.get("thumbnailUrl");
                            Object durationSeconds = payload.get("durationSeconds");
                            Object title = payload.get("title");
                            Object description = payload.get("description");

                            session.setVideoId(videoId != null ? videoId.toString() : null);
                            session.setVideoUrl(url != null ? url.toString() : null);
                            session.setVideoThumbnailUrl(thumbnailUrl != null ? thumbnailUrl.toString() : null);
                            session.setVideoDurationSeconds(
                                    durationSeconds != null ? Integer.valueOf(durationSeconds.toString()) : null);
                            session.setVideoTitle(title != null ? title.toString() : null);
                            session.setVideoDescription(description != null ? description.toString() : null);
                            session.setVideoStatus(SessionVideoStatus.READY);
                            session.setVideoStatusUpdatedAt(java.time.Instant.now());
                            syllabusSessionRepository.save(session);
                            System.out.println("✅ SyllabusSession " + sessionId + " marked VIDEO READY");
                        },
                        () -> System.out.println("⚠️ VIDEO_READY: SyllabusSession not found id=" + sessionId)
                    );
                }

                case "VIDEO_FAILED" -> {
                    syllabusSessionRepository.findById(sessionId).ifPresentOrElse(
                        session -> {
                            session.setVideoStatus(SessionVideoStatus.FAILED);
                            session.setVideoStatusUpdatedAt(java.time.Instant.now());
                            syllabusSessionRepository.save(session);
                            Object reason = payload.get("reason");
                            System.out.println("❌ SyllabusSession " + sessionId + " marked VIDEO FAILED reason=" + reason);
                        },
                        () -> System.out.println("⚠️ VIDEO_FAILED: SyllabusSession not found id=" + sessionId)
                    );
                }

                // ── NEW: file equivalents ──
                case "FILE_READY" -> {
                    syllabusSessionRepository.findById(sessionId).ifPresentOrElse(
                        session -> {
                            Object fileId = payload.get("fileId");
                            Object url = payload.get("url");
                            Object fileName = payload.get("fileName");

                            session.setFileId(fileId != null ? fileId.toString() : null);
                            session.setFileUrl(url != null ? url.toString() : null);
                            session.setFileName(fileName != null ? fileName.toString() : null);
                            session.setFileStatus(SessionFileStatus.READY);
                            syllabusSessionRepository.save(session);
                            System.out.println("✅ SyllabusSession " + sessionId + " marked FILE READY");
                        },
                        () -> System.out.println("⚠️ FILE_READY: SyllabusSession not found id=" + sessionId)
                    );
                }

                case "FILE_FAILED" -> {
                    syllabusSessionRepository.findById(sessionId).ifPresentOrElse(
                        session -> {
                            session.setFileStatus(SessionFileStatus.FAILED);
                            syllabusSessionRepository.save(session);
                            Object reason = payload.get("reason");
                            System.out.println("❌ SyllabusSession " + sessionId + " marked FILE FAILED reason=" + reason);
                        },
                        () -> System.out.println("⚠️ FILE_FAILED: SyllabusSession not found id=" + sessionId)
                    );
                }

                default -> System.out.println("ℹ️ COURSE ignoring featured-status event type=" + type);
            }

        } catch (Exception e) {
            System.out.println("❌ COURSE FeaturedProgramKafkaConsumer error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}