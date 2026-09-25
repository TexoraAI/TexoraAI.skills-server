package com.lms.live_session.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.live_session.entity.TexoraMeeting;
import com.lms.live_session.entity.TexoraWebhookOutbox;
import com.lms.live_session.repository.TexoraWebhookOutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class TexoraWebhookService {

    private final TexoraWebhookOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TexoraWebhookService(TexoraWebhookOutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

//    @Transactional
//    public void emitMeetingEnded(TexoraMeeting meeting, List<Map<String, Object>> participants) {
//        String status = switch (meeting.getStatus()) {
//            case NO_SHOW -> "NO_SHOW";
//            default -> "COMPLETED";
//        };
//
//        Long durationSeconds = null;
//        if (meeting.getStartedAt() != null && meeting.getEndedAt() != null) {
//            durationSeconds = java.time.Duration.between(meeting.getStartedAt(), meeting.getEndedAt()).getSeconds();
//        }
//
//        Map<String, Object> data = new LinkedHashMap<>();
//        data.put("status", status);
//        data.put("startedAt", meeting.getStartedAt() != null ? toIso(meeting.getStartedAt()) : null);
//        data.put("endedAt", meeting.getEndedAt() != null ? toIso(meeting.getEndedAt()) : null);
//        data.put("durationSeconds", durationSeconds);
//        data.put("attendance", Map.of("participants", participants != null ? participants : List.of()));
//
//        enqueue(meeting, "meeting.ended", data);
//    }
    @Transactional
    public void emitMeetingEnded(TexoraMeeting meeting, List<Map<String, Object>> participants) {
        String status = determineEndedStatus(meeting, participants);

        Long durationSeconds = null;
        if (meeting.getStartedAt() != null && meeting.getEndedAt() != null) {
            durationSeconds = java.time.Duration.between(meeting.getStartedAt(), meeting.getEndedAt()).getSeconds();
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", status);
        data.put("startedAt", meeting.getStartedAt() != null ? toIso(meeting.getStartedAt()) : null);
        data.put("endedAt", meeting.getEndedAt() != null ? toIso(meeting.getEndedAt()) : null);
        data.put("durationSeconds", durationSeconds);
        data.put("recordingUrl", meeting.getRecordingS3Url());
        data.put("recordingUrl", meeting.getRecordingS3Url()); // ← only this line added
        data.put("attendance", Map.of("participants", participants != null ? participants : List.of()));

        enqueue(meeting, "meeting.ended", data);
    }

    // Documented threshold for PARTIAL (spec Section 7.2 explicitly asks
    // ILMOra to define and document this):
    //   - NO_SHOW: the meeting's own lifecycle status is NO_SHOW (nobody
//         ever joined — set by the scheduler before this is called).
    //   - PARTIAL: fewer than 2 distinct participants ever joined, OR actual
//         call duration was under 20% of the scheduled durationMinutes
//         (minimum 3-minute floor, so a short scheduled call doesn't get an
//         unreasonably tiny cutoff).
    //   - COMPLETED: everything else.
    private String determineEndedStatus(TexoraMeeting meeting, List<Map<String, Object>> participants) {
        if (meeting.getStatus() == com.lms.live_session.entity.TexoraMeetingStatus.NO_SHOW) {
            return "NO_SHOW";
        }

        int distinctParticipants = participants != null
                ? (int) participants.stream().map(p -> (String) p.get("identity"))
                		.filter(java.util.Objects::nonNull)
                		.distinct().count()
                : 0;
        if (distinctParticipants < 2) {
            return "PARTIAL";
        }

        if (meeting.getStartedAt() != null && meeting.getEndedAt() != null && meeting.getDurationMinutes() != null) {
            long actualSeconds = java.time.Duration.between(meeting.getStartedAt(), meeting.getEndedAt()).getSeconds();
            long scheduledSeconds = meeting.getDurationMinutes() * 60L;
            long threshold = Math.max(180, (long) (scheduledSeconds * 0.2));
            if (actualSeconds < threshold) {
                return "PARTIAL";
            }
        }

        return "COMPLETED";
    }

    @Transactional
    public void emitAnalysisCompleted(TexoraMeeting meeting, int analysisVersion, Object summaryObj,
                                       String transcriptUrl, String transcriptUrlExpiresAt) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("analysisVersion", analysisVersion);
        data.put("generatedAt", toIso(LocalDateTime.now(ZoneId.of("UTC"))));
        data.put("language", "en");
        data.put("analysis", summaryObj);
        data.put("transcriptUrl", transcriptUrl);
        data.put("transcriptUrlExpiresAt", transcriptUrlExpiresAt);

        enqueue(meeting, "analysis.completed", data);
    }

    @Transactional
    public void emitAnalysisFailed(TexoraMeeting meeting, String reason, String message, boolean retryable) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reason", reason);
        data.put("message", message);
        data.put("retryable", retryable);

        enqueue(meeting, "analysis.failed", data);
    }

    private void enqueue(TexoraMeeting meeting, String eventType, Map<String, Object> data) {
        try {
            String eventId = UUID.randomUUID().toString();

            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("eventId", eventId);
            envelope.put("eventType", eventType);
            envelope.put("schemaVersion", 1);
            envelope.put("occurredAt", toIso(LocalDateTime.now(ZoneId.of("UTC"))));
            envelope.put("meetingId", meeting.getTexoraMeetingId());
            envelope.put("externalRef", meeting.getExternalRef());
            envelope.put("data", data);

            String payloadJson = objectMapper.writeValueAsString(envelope);

            TexoraWebhookOutbox row = new TexoraWebhookOutbox();
            row.setEventId(eventId);
            row.setEventType(eventType);
            row.setMeetingId(meeting.getTexoraMeetingId());
            row.setPayloadJson(payloadJson);
            outboxRepository.save(row);

        } catch (Exception e) {
            System.err.println("[TexoraWebhookService] Failed to enqueue " + eventType
                    + " for " + meeting.getTexoraMeetingId() + ": " + e.getMessage());
        }
    }

    private String toIso(LocalDateTime utc) {
        return utc.atZone(ZoneId.of("UTC")).toInstant().toString();
    }
}