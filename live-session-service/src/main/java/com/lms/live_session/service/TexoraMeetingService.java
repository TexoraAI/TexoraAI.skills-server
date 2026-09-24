package com.lms.live_session.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.live_session.exception.MeetingException;
import com.lms.live_session.service.EgressService;
import com.lms.live_session.service.MeetingTokenService;
import com.lms.live_session.dto.TexoraAnalysisResponseDTO;
import com.lms.live_session.dto.TexoraMeetingRequestDTO;
import com.lms.live_session.dto.TexoraMeetingResponseDTO;
import com.lms.live_session.entity.*;
import com.lms.live_session.repository.TexoraAnalysisRepository;
import com.lms.live_session.repository.TexoraMeetingRepository;
import com.lms.live_session.repository.TexoraParticipantRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class TexoraMeetingService {

    private final TexoraMeetingRepository repository;
    private final MeetingTokenService tokenService;
    private final EgressService egressService;
    private final TexoraParticipantRepository participantRepository;
    private final TexoraAnalysisRepository analysisRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String CODE_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.region}")
    private String awsRegion;

    public TexoraMeetingService(TexoraMeetingRepository repository,
                                 MeetingTokenService tokenService,
                                 EgressService egressService,
                                 TexoraParticipantRepository participantRepository,
                                 TexoraAnalysisRepository analysisRepository) {
        this.repository = repository;
        this.tokenService = tokenService;
        this.egressService = egressService;
        this.participantRepository = participantRepository;
        this.analysisRepository = analysisRepository;
    }

    // ─────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────
    public TexoraMeetingResponseDTO createMeeting(TexoraMeetingRequestDTO dto) {
        if (dto.getTopic() == null || dto.getTopic().isBlank()) {
            throw new MeetingException("Field 'topic' is required.");
        }
        if (dto.getStartTime() == null || dto.getStartTime().isBlank()) {
            throw new MeetingException("Field 'startTime' is required.");
        }
        if (dto.getDurationMinutes() == null || dto.getDurationMinutes() <= 0) {
            throw new MeetingException("Field 'durationMinutes' must be a positive integer.");
        }
        if (dto.getExternalRef() != null && dto.getExternalRef().length() > 64) {
            throw new MeetingException("Field 'externalRef' must be at most 64 characters.");
        }

        LocalDateTime startUtc;
        try {
            startUtc = ZonedDateTime.parse(dto.getStartTime())
                    .withZoneSameInstant(ZoneId.of("UTC"))
                    .toLocalDateTime();
        } catch (Exception e) {
            throw new MeetingException("Field 'startTime' must be a valid ISO 8601 UTC datetime string.");
        }

        TexoraMeeting meeting = new TexoraMeeting();
        meeting.setTopic(dto.getTopic());
        meeting.setStartTimeUtc(startUtc);
        meeting.setDurationMinutes(dto.getDurationMinutes());
        meeting.setExternalRef(dto.getExternalRef());
        meeting.setStatus(TexoraMeetingStatus.SCHEDULED);

        if (dto.getContext() != null) {
            try {
                meeting.setContextJson(objectMapper.writeValueAsString(dto.getContext()));
            } catch (Exception e) {
                System.err.println("[TexoraMeetingService] Failed to serialize context: " + e.getMessage());
            }
        }

        String code = generateUniqueJoinCode();
        meeting.setJoinCode(code);
        meeting.setRoomName("texora-meeting-" + code);
        meeting.setMeetingLink(baseUrl + "/ilmorameet/" + code);

        LocalDateTime expiresAtUtc = startUtc.plusMinutes(dto.getDurationMinutes()).plusHours(4);
        meeting.setExpiresAtUtc(expiresAtUtc);

        TexoraMeeting saved = repository.save(meeting);
        saved.setTexoraMeetingId("mtg_" + saved.getId());
        saved = repository.save(saved);

        String expiresAtIso = expiresAtUtc.atZone(ZoneId.of("UTC")).toInstant().toString();

        return new TexoraMeetingResponseDTO(saved.getMeetingLink(), saved.getTexoraMeetingId(), expiresAtIso);
    }

    // ─────────────────────────────────────────────────────────
    // CANCEL
    // ─────────────────────────────────────────────────────────
    public void cancelMeeting(String texoraMeetingId) {
        TexoraMeeting meeting = repository.findByTexoraMeetingId(texoraMeetingId)
                .orElseThrow(() -> new MeetingException("MEETING_NOT_FOUND"));

        if (meeting.getStatus() == TexoraMeetingStatus.CANCELLED) {
            return;
        }
        meeting.setStatus(TexoraMeetingStatus.CANCELLED);
        meeting.setCancelledAt(LocalDateTime.now(ZoneId.of("UTC")));
        repository.save(meeting);
        tokenService.closeRoom(meeting.getRoomName());
    }

    // ─────────────────────────────────────────────────────────
    // JOIN — public, no API key
    // ─────────────────────────────────────────────────────────
    public Map<String, Object> validateJoinCode(String joinCode) {
        TexoraMeeting meeting = repository.findByJoinCode(normalizeCode(joinCode)).orElse(null);
        if (meeting == null) {
            return Map.of("valid", false, "message", "No meeting found for this join code");
        }
        String reason = notJoinableReason(meeting);
        if (reason != null) {
            return Map.of("valid", false, "message", reason);
        }
        return Map.of(
                "valid", true,
                "meetingId", meeting.getTexoraMeetingId(),
                "topic", meeting.getTopic(),
                "status", meeting.getStatus().name()
        );
    }

    public Map<String, String> generateJoinToken(String joinCode, String identity, String displayName, String role) {
        TexoraMeeting meeting = repository.findByJoinCode(normalizeCode(joinCode))
                .orElseThrow(() -> new MeetingException("No meeting found for this join code"));

        String reason = notJoinableReason(meeting);
        if (reason != null) {
            throw new MeetingException(reason);
        }

        boolean firstJoin = meeting.getStatus() == TexoraMeetingStatus.SCHEDULED;
        if (firstJoin) {
            meeting.setStatus(TexoraMeetingStatus.ACTIVE);
            meeting.setStartedAt(LocalDateTime.now(ZoneId.of("UTC")));
            repository.save(meeting);
        }

        String resolvedIdentity = (identity != null && !identity.isBlank()) ? identity.trim().toLowerCase() : UUID.randomUUID().toString();
        String resolvedName = (displayName != null && !displayName.isBlank()) ? displayName : "Guest";

        TexoraParticipant participant = new TexoraParticipant();
        participant.setTexoraMeetingId(meeting.getTexoraMeetingId());
        participant.setRole(role != null ? role : "guest");
        participant.setIdentity(resolvedIdentity);
        participant.setDisplayName(resolvedName);
        participant.setJoinedAt(LocalDateTime.now(ZoneId.of("UTC")));
        participantRepository.save(participant);

        String token = tokenService.generateMeetingToken(
                meeting.getRoomName(), resolvedIdentity, resolvedName, false, resolvedIdentity, UUID.randomUUID().toString());

        if (firstJoin) {
            startRecording(meeting);
        }

        return Map.of("room", meeting.getRoomName(), "token", token);
    }

    public void recordLeave(String joinCode, String identity) {
        TexoraMeeting meeting = repository.findByJoinCode(normalizeCode(joinCode)).orElse(null);
        if (meeting == null || identity == null) return;

        String normalizedIdentity = identity.trim().toLowerCase();
        participantRepository.findByTexoraMeetingIdAndIdentityAndLeftAtIsNull(meeting.getTexoraMeetingId(), normalizedIdentity)
                .ifPresent(p -> {
                    p.setLeftAt(LocalDateTime.now(ZoneId.of("UTC")));
                    participantRepository.save(p);
                });
    }

    public List<Map<String, Object>> buildAttendanceData(TexoraMeeting meeting) {
        List<TexoraParticipant> participants = participantRepository.findByTexoraMeetingId(meeting.getTexoraMeetingId());
        List<Map<String, Object>> result = new ArrayList<>();

        for (TexoraParticipant p : participants) {
            LocalDateTime leftAt = p.getLeftAt() != null ? p.getLeftAt() : meeting.getEndedAt();

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("role", p.getRole());
            entry.put("name", p.getDisplayName());
            entry.put("joinedAt", p.getJoinedAt().atZone(ZoneId.of("UTC")).toInstant().toString());
            entry.put("leftAt", leftAt != null ? leftAt.atZone(ZoneId.of("UTC")).toInstant().toString() : null);
            result.add(entry);
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────
    // ANALYSIS lookup — GET /meetings/{meetingId}/analysis
    // ─────────────────────────────────────────────────────────
    public TexoraAnalysisResponseDTO getAnalysis(String texoraMeetingId) {
        TexoraMeeting meeting = repository.findByTexoraMeetingId(texoraMeetingId)
                .orElseThrow(() -> new MeetingException("MEETING_NOT_FOUND"));

        TexoraAnalysisResponseDTO dto = new TexoraAnalysisResponseDTO();
        dto.setMeetingId(meeting.getTexoraMeetingId());
        dto.setExternalRef(meeting.getExternalRef());

        if (meeting.getStartedAt() != null) {
            dto.setEnded(new TexoraAnalysisResponseDTO.EndedBlock(
                    toIso(meeting.getStartedAt()),
                    meeting.getEndedAt() != null ? toIso(meeting.getEndedAt()) : null
            ));
        }

        switch (meeting.getStatus()) {
            case SCHEDULED -> dto.setStatus("PENDING");
            case ACTIVE -> dto.setStatus("IN_PROGRESS");
            case NO_SHOW -> dto.setStatus("NO_SHOW");
            case CANCELLED -> dto.setStatus("CANCELLED");
            case ENDED -> {
                var analysisOpt = analysisRepository.findByTexoraMeetingId(texoraMeetingId);
                if (analysisOpt.isEmpty()) {
                    dto.setStatus("ENDED_AWAITING_ANALYSIS");
                } else {
                    TexoraAnalysis analysis = analysisOpt.get();
                    switch (analysis.getStatus()) {
                        case PROCESSING -> dto.setStatus("ENDED_AWAITING_ANALYSIS");
                        case FAILED -> {
                            dto.setStatus("ANALYSIS_FAILED");
                            dto.setFailure(new TexoraAnalysisResponseDTO.FailureBlock(
                                    analysis.getFailureReason(), analysis.getFailureMessage()));
                        }
                        case COMPLETED -> {
                            dto.setStatus("ANALYSIS_COMPLETED");
                            Object parsedSummary = parseSummaryJson(analysis.getSummaryJson());
                            dto.setAnalysis(new TexoraAnalysisResponseDTO.AnalysisBlock(
                                    analysis.getAnalysisVersion(), parsedSummary));
                        }
                    }
                }
            }
        }

        return dto;
    }

    // ─────────────────────────────────────────────────────────
    // RECORDING
    // ─────────────────────────────────────────────────────────
    private void startRecording(TexoraMeeting meeting) {
        try {
            EgressService.EgressStartResult result = egressService.startRecording(meeting.getId(), meeting.getRoomName());
            if (result != null) {
                meeting.setEgressId(result.egressId);
                repository.save(meeting);
            }
        } catch (Exception e) {
            System.err.println("[TexoraMeetingService] Failed to start recording for "
                    + meeting.getTexoraMeetingId() + ": " + e.getMessage());
        }
    }

    void stopRecordingIfRunning(TexoraMeeting meeting) {
        // Stop the mixed room-level recording (unchanged).
        if (meeting.getEgressId() != null) {
            try {
                livekit.LivekitEgress.EgressInfo info = egressService.stopRecordingAndGetInfo(meeting.getEgressId());
                if (info != null && info.getFileResultsCount() > 0) {
                    String filename = info.getFileResults(0).getFilename();
                    String s3Url = "https://" + bucket + ".s3." + awsRegion + ".amazonaws.com/" + filename;
                    meeting.setRecordingS3Url(s3Url);
                }
            } catch (Exception e) {
                System.err.println("[TexoraMeetingService] Failed to stop room recording for "
                        + meeting.getTexoraMeetingId() + ": " + e.getMessage());
            } finally {
                meeting.setEgressId(null);
            }
        }

        // NEW — also stop each participant's individual track egress, and
        // capture their audio file's S3 URL for per-speaker transcription.
        List<TexoraParticipant> participants = participantRepository.findByTexoraMeetingId(meeting.getTexoraMeetingId());
        for (TexoraParticipant p : participants) {
            if (p.getAudioEgressId() == null) continue;
            try {
                livekit.LivekitEgress.EgressInfo info = egressService.stopRecordingAndGetInfo(p.getAudioEgressId());
                if (info != null && info.getFileResultsCount() > 0) {
                    String filename = info.getFileResults(0).getFilename();
                    String s3Url = "https://" + bucket + ".s3." + awsRegion + ".amazonaws.com/" + filename;
                    p.setAudioS3Url(s3Url);
                    participantRepository.save(p);
                }
            } catch (Exception e) {
                System.err.println("[TexoraMeetingService] Failed to stop track egress for "
                        + p.getIdentity() + ": " + e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────
    private String notJoinableReason(TexoraMeeting meeting) {
        if (meeting.getStatus() == TexoraMeetingStatus.CANCELLED) return "This meeting has been cancelled";
        if (meeting.getStatus() == TexoraMeetingStatus.ENDED) return "This meeting has already ended";
        if (meeting.getExpiresAtUtc() != null && LocalDateTime.now(ZoneId.of("UTC")).isAfter(meeting.getExpiresAtUtc())) {
            return "This meeting link has expired";
        }
        return null;
    }

    private String normalizeCode(String joinCode) {
        return joinCode == null ? null : joinCode.trim().toLowerCase();
    }

    private String generateUniqueJoinCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder("tx");
            for (int i = 0; i < 6; i++) {
                sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
            }
            code = sb.toString();
        } while (repository.existsByJoinCode(code));
        return code;
    }

    private Object parseSummaryJson(String json) {
        if (json == null) return null;
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            System.err.println("[TexoraMeetingService] Failed to parse stored summary JSON: " + e.getMessage());
            return null;
        }
    }

    private String toIso(LocalDateTime utc) {
        return utc.atZone(ZoneId.of("UTC")).toInstant().toString();
    }
    
    public String getTranscriptText(String texoraMeetingId) {
        return analysisRepository.findByTexoraMeetingId(texoraMeetingId)
                .map(TexoraAnalysis::getTranscriptText)
                .orElse(null);
    }
}