package com.lms.live_session.service;

import com.lms.live_session.entity.AiTranscriptSegment;
import com.lms.live_session.entity.AiTranscriptSession;
import com.lms.live_session.entity.AiTranscriptSession.TranscriptStatus;
import com.lms.live_session.entity.Recording;
import com.lms.live_session.repository.AiTranscriptSegmentRepository;
import com.lms.live_session.repository.AiTranscriptSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AiTranscriptLinkService {

    private final AiTranscriptSessionRepository sessionRepo;
    private final AiTranscriptSegmentRepository segmentRepo;

    public AiTranscriptLinkService(
        AiTranscriptSessionRepository sessionRepo,
        AiTranscriptSegmentRepository segmentRepo
    ) {
        this.sessionRepo = sessionRepo;
        this.segmentRepo = segmentRepo;
    }

    // Called once a Recording's Whisper transcript finishes (transcriptStatus == DONE).
    // Only applies to recordings tied to a live (virtual) session — standalone
    // uploads with no sessionId have nothing to link against.
    public void linkRecordingTranscript(Recording recording) {
        if (recording.getSessionId() == null) return;
        if (recording.getTranscriptText() == null || recording.getTranscriptText().isBlank()) return;

        AiTranscriptSession ts = sessionRepo
            .findFirstByLiveSessionIdOrderByStartedAtDesc(recording.getSessionId())
            .orElseGet(() -> {
                AiTranscriptSession fresh = new AiTranscriptSession();
                fresh.setTrainerEmail(recording.getTrainerEmail());
                fresh.setLiveSessionId(recording.getSessionId());
                fresh.setTitle(recording.getTitle() != null ? recording.getTitle() : "Meeting Transcript");
                fresh.setStartedAt(LocalDateTime.now());
                return fresh;
            });

        ts.setStatus(TranscriptStatus.COMPLETED);
        ts.setStoppedAt(LocalDateTime.now());
        AiTranscriptSession savedTs = sessionRepo.save(ts);

        // Naive first pass: whole Whisper transcript as a single segment.
        // No per-word timestamps available from OpenAiClientService today.
        AiTranscriptSegment seg = new AiTranscriptSegment();
        seg.setTranscriptSessionId(savedTs.getId());
        seg.setText(recording.getTranscriptText());
        seg.setSpeakerName("Call Recording");
        seg.setStartedAtSecond(0);
        segmentRepo.save(seg);
    }
}