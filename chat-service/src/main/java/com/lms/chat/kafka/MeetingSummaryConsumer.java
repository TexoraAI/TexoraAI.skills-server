//package com.lms.chat.kafka;
//
//import com.lms.chat.entity.MeetingSummary;
//import com.lms.chat.entity.MeetingSummaryStatus;
//import com.lms.chat.repository.MeetingSummaryRepository;
//import com.lms.chat.service.AudioTranscriptionService;
//import com.lms.chat.service.OpenAiService;
//import jakarta.transaction.Transactional;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class MeetingSummaryConsumer {
//
//    private final MeetingSummaryRepository repository;
//    private final AudioTranscriptionService audioTranscriptionService;
//    private final OpenAiService openAiService;
//
//    private static final String SUMMARY_SYSTEM_PROMPT = """
//            You are an AI meeting assistant. You will be given a transcript
//            that may include a spoken-audio transcription section and a
//            chat-messages section from an online meeting. Produce:
//            1. A concise summary of what was discussed.
//            2. A short list of key points or action items, if any.
//            Be clear and well-organized. If either section is empty, base
//            your summary on whatever content is available.
//            """;
//
//    public MeetingSummaryConsumer(MeetingSummaryRepository repository,
//                                   AudioTranscriptionService audioTranscriptionService,
//                                   OpenAiService openAiService) {
//        this.repository = repository;
//        this.audioTranscriptionService = audioTranscriptionService;
//        this.openAiService = openAiService;
//    }
//
//    @SuppressWarnings("unchecked")
//    @Transactional
//    @KafkaListener(topics = "meeting-summary-requests", groupId = "chat-service-group")
//    public void consume(Map<String, Object> event) {
//        Long meetingId = ((Number) event.get("meetingId")).longValue();
//        String title = (String) event.get("title");
//        String requestedByEmail = (String) event.get("requestedByEmail");
//        String requestedByRole = (String) event.get("requestedByRole");
//
//        Object orgIdRaw = event.get("organizationId");
//        Long organizationId = orgIdRaw == null ? null : ((Number) orgIdRaw).longValue();
//
//        String recordingS3Url = (String) event.get("recordingS3Url");
//        List<Map<String, Object>> chatMessages =
//                (List<Map<String, Object>>) event.get("chatMessages");
//
//        System.out.println("📩 MEETING SUMMARY REQUESTED -> meetingId=" + meetingId
//                + " requestedBy=" + requestedByEmail + " hasRecording=" + (recordingS3Url != null));
//
//        MeetingSummary summary = repository.findByMeetingId(meetingId).orElseGet(MeetingSummary::new);
//        summary.setMeetingId(meetingId);
//        summary.setTitle(title);
//        summary.setRequestedByEmail(requestedByEmail);
//        summary.setRequestedByRole(requestedByRole);
//        summary.setOrganizationId(organizationId);
//        summary.setStatus(MeetingSummaryStatus.GENERATING);
//        repository.save(summary);
//
//        String chatTranscript = buildChatTranscript(chatMessages);
//
//        try {
//        	 System.out.println("========== MEETING SUMMARY START ==========");
//        	    System.out.println("Meeting ID: " + meetingId);
//        	    System.out.println("Recording URL: " + recordingS3Url);
//            String audioTranscript = "";
//            if (recordingS3Url != null && !recordingS3Url.isBlank()) {
//                audioTranscript = audioTranscriptionService.downloadAndTranscribe(recordingS3Url);
//            }
//
//            String fullTranscript = "[Spoken audio transcript]\n"
//                    + (audioTranscript.isBlank() ? "(no recording available)" : audioTranscript)
//                    + "\n\n[Chat messages]\n"
//                    + (chatTranscript.isBlank() ? "(no chat messages)" : chatTranscript);
//            System.out.println("▶ Calling OpenAI Chat...");
//            String summaryText = openAiService.chat(SUMMARY_SYSTEM_PROMPT, fullTranscript);
//            System.out.println("✅ OpenAI Chat completed.");
//
//            summary.setTranscriptText(fullTranscript);
//            summary.setSummaryText(summaryText);
//            summary.setStatus(MeetingSummaryStatus.READY);
//            summary.setGeneratedAt(java.time.LocalDateTime.now());
//            repository.save(summary);
//
//            System.out.println("✅ MEETING SUMMARY READY -> meetingId=" + meetingId);
//
//        }
////        catch (Exception e) {
////            System.err.println("❌ MEETING SUMMARY FAILED -> meetingId=" + meetingId + " : " + e.getMessage());
////            summary.setStatus(MeetingSummaryStatus.FAILED);
////            repository.save(summary);
////            // caught here, not rethrown — consumer offset still commits
////        }
//        catch (Exception e) {
//
//            System.err.println("❌❌❌ MEETING SUMMARY FAILED ❌❌❌");
//            System.err.println("Meeting ID = " + meetingId);
//
//            e.printStackTrace();
//
//            summary.setStatus(MeetingSummaryStatus.FAILED);
//            repository.save(summary);
//        }
//    }
//
//    private String buildChatTranscript(List<Map<String, Object>> chatMessages) {
//        if (chatMessages == null || chatMessages.isEmpty()) {
//            return "";
//        }
//        StringBuilder sb = new StringBuilder();
//        for (Map<String, Object> msg : chatMessages) {
//            Object sender = msg.get("senderName");
//            Object text = msg.get("text");
//            if (text == null) continue;
//            sb.append(sender != null ? sender : "Unknown").append(": ").append(text).append("\n");
//        }
//        return sb.toString();
//    }
//}

package com.lms.chat.kafka;

import com.lms.chat.entity.MeetingSummary;
import com.lms.chat.entity.MeetingSummaryStatus;
import com.lms.chat.repository.MeetingSummaryRepository;
import com.lms.chat.service.AudioTranscriptionService;
import com.lms.chat.service.OpenAiService;
import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MeetingSummaryConsumer {

    private final MeetingSummaryRepository repository;
    private final AudioTranscriptionService audioTranscriptionService;
    private final OpenAiService openAiService;

    private static final String SUMMARY_SYSTEM_PROMPT = """
            You are an AI meeting assistant. You will be given a transcript
            that may include a spoken-audio transcription section and a
            chat-messages section from an online meeting. Produce:
            1. A concise summary of what was discussed.
            2. A short list of key points or action items, if any.
            Be clear and well-organized. If either section is empty, base
            your summary on whatever content is available.
            """;

    public MeetingSummaryConsumer(MeetingSummaryRepository repository,
                                   AudioTranscriptionService audioTranscriptionService,
                                   OpenAiService openAiService) {
        this.repository = repository;
        this.audioTranscriptionService = audioTranscriptionService;
        this.openAiService = openAiService;
    }

    @SuppressWarnings("unchecked")
    @Transactional
    @KafkaListener(
        topics = "meeting-summary-requests",
        groupId = "chat-service-meeting-summary-group",
        containerFactory = "meetingSummaryListenerContainerFactory"
    )
    public void consume(Map<String, Object> event) {
        Long meetingId = ((Number) event.get("meetingId")).longValue();
        String title = (String) event.get("title");
        String requestedByEmail = (String) event.get("requestedByEmail");
        String requestedByRole = (String) event.get("requestedByRole");

        Object orgIdRaw = event.get("organizationId");
        Long organizationId = orgIdRaw == null ? null : ((Number) orgIdRaw).longValue();

        String recordingS3Url = (String) event.get("recordingS3Url");
        List<Map<String, Object>> chatMessages =
                (List<Map<String, Object>>) event.get("chatMessages");

        System.out.println("📩 MEETING SUMMARY REQUESTED -> meetingId=" + meetingId
                + " requestedBy=" + requestedByEmail + " hasRecording=" + (recordingS3Url != null));

        MeetingSummary summary = repository.findByMeetingId(meetingId).orElseGet(MeetingSummary::new);
        summary.setMeetingId(meetingId);
        summary.setTitle(title);
        summary.setRequestedByEmail(requestedByEmail);
        summary.setRequestedByRole(requestedByRole);
        summary.setOrganizationId(organizationId);
        summary.setStatus(MeetingSummaryStatus.GENERATING);
        repository.save(summary);

        String chatTranscript = buildChatTranscript(chatMessages);

        try {
        	 System.out.println("========== MEETING SUMMARY START ==========");
        	    System.out.println("Meeting ID: " + meetingId);
        	    System.out.println("Recording URL: " + recordingS3Url);
            String audioTranscript = "";
            if (recordingS3Url != null && !recordingS3Url.isBlank()) {
                audioTranscript = audioTranscriptionService.downloadAndTranscribe(recordingS3Url);
            }

            String fullTranscript = "[Spoken audio transcript]\n"
                    + (audioTranscript.isBlank() ? "(no recording available)" : audioTranscript)
                    + "\n\n[Chat messages]\n"
                    + (chatTranscript.isBlank() ? "(no chat messages)" : chatTranscript);
            System.out.println("▶ Calling OpenAI Chat...");
            String summaryText = openAiService.chat(SUMMARY_SYSTEM_PROMPT, fullTranscript);
            System.out.println("✅ OpenAI Chat completed.");

            summary.setTranscriptText(fullTranscript);
            summary.setSummaryText(summaryText);
            summary.setStatus(MeetingSummaryStatus.READY);
            summary.setGeneratedAt(java.time.LocalDateTime.now());
            repository.save(summary);

            System.out.println("✅ MEETING SUMMARY READY -> meetingId=" + meetingId);

        }
//        catch (Exception e) {
//            System.err.println("❌ MEETING SUMMARY FAILED -> meetingId=" + meetingId + " : " + e.getMessage());
//            summary.setStatus(MeetingSummaryStatus.FAILED);
//            repository.save(summary);
//            // caught here, not rethrown — consumer offset still commits
//        }
        catch (Exception e) {

            System.err.println("❌❌❌ MEETING SUMMARY FAILED ❌❌❌");
            System.err.println("Meeting ID = " + meetingId);

            e.printStackTrace();

            summary.setStatus(MeetingSummaryStatus.FAILED);
            repository.save(summary);
        }
    }

    private String buildChatTranscript(List<Map<String, Object>> chatMessages) {
        if (chatMessages == null || chatMessages.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> msg : chatMessages) {
            Object sender = msg.get("senderName");
            Object text = msg.get("text");
            if (text == null) continue;
            sb.append(sender != null ? sender : "Unknown").append(": ").append(text).append("\n");
        }
        return sb.toString();
    }
}