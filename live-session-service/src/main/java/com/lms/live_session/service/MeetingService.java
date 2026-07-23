package com.lms.live_session.service;

import com.lms.live_session.dto.MeetingJoinRequestDTO;
import com.lms.live_session.dto.MeetingRequestDTO;
import com.lms.live_session.dto.MeetingResponseDTO;
import com.lms.live_session.entity.JoinRequestStatus;
import com.lms.live_session.entity.Meeting;
import com.lms.live_session.entity.MeetingJoinRequest;
import com.lms.live_session.entity.MeetingStatus;
import com.lms.live_session.entity.MeetingType;
import com.lms.live_session.exception.MeetingException;
import com.lms.live_session.repository.MeetingJoinRequestRepository;
import com.lms.live_session.repository.MeetingRepository;
import com.lms.live_session.util.JoinCodeGenerator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.YearMonth;
import java.util.TreeMap;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MeetingService {

    private final MeetingRepository repository;
    private final MeetingTokenService tokenService;
    private final MeetingJoinRequestRepository joinRequestRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    public MeetingService(MeetingRepository repository,
                           MeetingTokenService tokenService,
                           MeetingJoinRequestRepository joinRequestRepository) {
        this.repository = repository;
        this.tokenService = tokenService;
        this.joinRequestRepository = joinRequestRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // CREATE — INSTANT
    // ─────────────────────────────────────────────────────────────

    public MeetingResponseDTO createInstantMeeting(MeetingRequestDTO dto, String creatorId, String creatorRole) {
        Meeting meeting = new Meeting();
//        meeting.setTitle(blankToDefault(dto.getTitle(), "Instant meeting"));
//        meeting.setTitle(blankToDefault(dto.getTitle(), "Ilmorameet"));
        meeting.setTitle(blankToDefault(dto.getTitle(),
                "Ilmorameet · " + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMM d, h:mm a"))));
        meeting.setCreatorId(creatorId);
        meeting.setCreatorRole(creatorRole);
        meeting.setCreatorName(dto.getCreatorName());
        meeting.setOrganizationId(dto.getOrganizationId());
        meeting.setMeetingType(MeetingType.INSTANT);
        meeting.setMeetingStatus(MeetingStatus.ACTIVE);
        meeting.setReusable(true);

        assignJoinCodeAndUrl(meeting);

        Meeting saved = repository.save(meeting);
        return toResponseDTO(saved, creatorId);
    }

    // ─────────────────────────────────────────────────────────────
    // CREATE — SCHEDULED
    // ─────────────────────────────────────────────────────────────

//    public MeetingResponseDTO createScheduledMeeting(MeetingRequestDTO dto, String creatorId, String creatorRole) {
//        if (dto.getDate() == null || dto.getTime() == null || dto.getTimezone() == null) {
//            throw new MeetingException("date, time and timezone are required to schedule a meeting");
//        }
//
//        LocalDateTime scheduledTimeUtc = toUtc(dto.getDate(), dto.getTime(), dto.getTimezone());
//
//        Meeting meeting = new Meeting();
//        meeting.setTitle(blankToDefault(dto.getTitle(), "Scheduled meeting"));
//        meeting.setCreatorId(creatorId);
//        meeting.setCreatorRole(creatorRole);
//        meeting.setCreatorName(dto.getCreatorName());
//        meeting.setOrganizationId(dto.getOrganizationId());
//        meeting.setMeetingType(MeetingType.SCHEDULED);
//        meeting.setMeetingStatus(MeetingStatus.SCHEDULED);
//        meeting.setTimezone(dto.getTimezone());
//        meeting.setScheduledTimeUtc(scheduledTimeUtc);
//        meeting.setReusable(true);
//
//        assignJoinCodeAndUrl(meeting);
//
//        Meeting saved = repository.save(meeting);
//        return toResponseDTO(saved, creatorId);
//    }
    public MeetingResponseDTO createScheduledMeeting(MeetingRequestDTO dto, String creatorId, String creatorRole) {
        if (dto.getDate() == null || dto.getTime() == null || dto.getTimezone() == null) {
            throw new MeetingException("date, time and timezone are required to schedule a meeting");
        }

        LocalDateTime scheduledTimeUtc = toUtc(dto.getDate(), dto.getTime(), dto.getTimezone());

        if (!scheduledTimeUtc.isAfter(LocalDateTime.now(ZoneId.of("UTC")))) {
            throw new MeetingException("Scheduled time must be in the future");
        }

        Meeting meeting = new Meeting();
        meeting.setTitle(blankToDefault(dto.getTitle(), "Scheduled meeting"));
        meeting.setCreatorId(creatorId);
        meeting.setCreatorRole(creatorRole);
        meeting.setCreatorName(dto.getCreatorName());
        meeting.setOrganizationId(dto.getOrganizationId());
        meeting.setMeetingType(MeetingType.SCHEDULED);
        meeting.setMeetingStatus(MeetingStatus.SCHEDULED);
        meeting.setTimezone(dto.getTimezone());
        meeting.setScheduledTimeUtc(scheduledTimeUtc);
        meeting.setReusable(true);

        assignJoinCodeAndUrl(meeting);

        Meeting saved = repository.save(meeting);
        return toResponseDTO(saved, creatorId);
    }

    // ─────────────────────────────────────────────────────────────
    // JOIN CODE LOOKUP
    // ─────────────────────────────────────────────────────────────

    public Map<String, Object> validateJoinCode(String joinCode) {
        return repository.findByJoinCode(normalizeCode(joinCode))
                .map(m -> Map.<String, Object>of(
                        "valid", true,
                        "meeting", toResponseDTO(m, null)
                ))
                .orElseGet(() -> Map.of(
                        "valid", false,
                        "message", "No meeting found for this join code"
                ));
    }

    // requesterId is the caller's identity if authenticated, null for an
    // anonymous guest — either way this never throws for "not the host",
    // it just resolves isHost to false.
    public MeetingResponseDTO getMeetingByJoinCode(String joinCode, String requesterId) {
        Meeting meeting = repository.findByJoinCode(normalizeCode(joinCode))
                .orElseThrow(() -> new MeetingException("No meeting found for join code: " + joinCode));
        return toResponseDTO(meeting, requesterId);
    }

    // ─────────────────────────────────────────────────────────────
    // QUERIES
    // ─────────────────────────────────────────────────────────────

    public MeetingResponseDTO getMeetingById(Long id, String requesterId) {
        return toResponseDTO(findOrThrow(id), requesterId);
    }

    public List<MeetingResponseDTO> getMyMeetings(String creatorId) {
        return repository.findByCreatorIdOrderByCreatedAtDesc(creatorId)
                .stream()
                .map(m -> toResponseDTO(m, creatorId))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    // START / END
    // ─────────────────────────────────────────────────────────────

    public MeetingResponseDTO startScheduledMeeting(Long id) {
        Meeting meeting = findOrThrow(id);

        if (meeting.getMeetingStatus() == MeetingStatus.ACTIVE) {
            return toResponseDTO(meeting, meeting.getCreatorId());
        }
        if (meeting.getMeetingStatus() != MeetingStatus.SCHEDULED
                && meeting.getMeetingStatus() != MeetingStatus.CREATED) {
            throw new MeetingException("Meeting cannot be started from status " + meeting.getMeetingStatus());
        }

        meeting.setMeetingStatus(MeetingStatus.ACTIVE);
        Meeting saved = repository.save(meeting);
        return toResponseDTO(saved, saved.getCreatorId());
    }

    public MeetingResponseDTO endMeeting(Long id) {
        Meeting meeting = findOrThrow(id);

        meeting.setMeetingStatus(MeetingStatus.ENDED);
        meeting.setEndedAt(LocalDateTime.now());
        Meeting saved = repository.save(meeting);

        // A meeting that just ended can't still have guests waiting in the
        // lobby — clear anything left PENDING so a stale poll doesn't hang.
        joinRequestRepository.findByMeetingIdAndStatusOrderByRequestedAtAsc(id, JoinRequestStatus.PENDING)
                .forEach(r -> {
                    r.setStatus(JoinRequestStatus.DENIED);
                    r.setRespondedAt(LocalDateTime.now());
                    joinRequestRepository.save(r);
                });

        return toResponseDTO(saved, saved.getCreatorId());
    }

    // ─────────────────────────────────────────────────────────────
    // LIVEKIT TOKEN — HOST ONLY (guests go through the lobby below)
    // ─────────────────────────────────────────────────────────────

    public Map<String, String> generateJoinToken(Long id, String identity, String displayName) {
        Meeting meeting = findOrThrow(id);

        if (meeting.getMeetingStatus() != MeetingStatus.ACTIVE) {
            throw new MeetingException("Meeting is not active — cannot issue a join token");
        }
        if (identity == null || !identity.equals(meeting.getCreatorId())) {
            throw new MeetingException("Only the host can join directly — guests must request to join");
        }

        String token = tokenService.generateMeetingToken(meeting.getRoomName(), identity, displayName, true);

        return Map.of(
                "room", meeting.getRoomName(),
                "token", token
        );
    }

    // ─────────────────────────────────────────────────────────────
    // LOBBY — GUEST JOIN REQUESTS
    // ─────────────────────────────────────────────────────────────

//    public MeetingJoinRequestDTO requestToJoin(Long meetingId, String guestName) {
//        Meeting meeting = findOrThrow(meetingId);
//
//        if (meeting.getMeetingStatus() != MeetingStatus.ACTIVE) {
//            throw new MeetingException("Meeting is not active — nothing to join yet");
//        }
//
//        MeetingJoinRequest request = new MeetingJoinRequest();
//        request.setMeetingId(meetingId);
//        request.setGuestIdentity(UUID.randomUUID().toString());
//        request.setGuestName(blankToDefault(guestName, "Guest"));
//        request.setStatus(JoinRequestStatus.PENDING);
//
//        MeetingJoinRequest saved = joinRequestRepository.save(request);
//        return toJoinRequestDTO(saved);
//    }
    public MeetingJoinRequestDTO requestToJoin(Long meetingId, String guestName, String guestEmail) {
        Meeting meeting = findOrThrow(meetingId);

        if (meeting.getMeetingStatus() != MeetingStatus.ACTIVE) {
            throw new MeetingException("Meeting is not active — nothing to join yet");
        }

        if (guestEmail == null || guestEmail.isBlank() || !guestEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new MeetingException("A valid email is required to join");
        }

        MeetingJoinRequest request = new MeetingJoinRequest();
        request.setMeetingId(meetingId);
        request.setGuestIdentity(UUID.randomUUID().toString());
        request.setGuestName(blankToDefault(guestName, "Guest"));
        request.setGuestEmail(guestEmail.trim().toLowerCase());
        request.setStatus(JoinRequestStatus.PENDING);

        MeetingJoinRequest saved = joinRequestRepository.save(request);
        return toJoinRequestDTO(saved);
    }

    public MeetingJoinRequestDTO getJoinRequestStatus(Long meetingId, Long requestId, String guestIdentity) {
        MeetingJoinRequest request = joinRequestRepository
                .findByIdAndMeetingIdAndGuestIdentity(requestId, meetingId, guestIdentity)
                .orElseThrow(() -> new MeetingException("Join request not found"));
        return toJoinRequestDTO(request);
    }

    public List<MeetingJoinRequestDTO> listPendingJoinRequests(Long meetingId, String requesterId) {
        Meeting meeting = findOrThrow(meetingId);
        verifyHost(meeting, requesterId);

        return joinRequestRepository
                .findByMeetingIdAndStatusOrderByRequestedAtAsc(meetingId, JoinRequestStatus.PENDING)
                .stream()
                .map(this::toJoinRequestDTO)
                .collect(Collectors.toList());
    }

    public MeetingJoinRequestDTO admitJoinRequest(Long meetingId, Long requestId, String requesterId) {
        Meeting meeting = findOrThrow(meetingId);
        verifyHost(meeting, requesterId);
        return resolveJoinRequest(meetingId, requestId, JoinRequestStatus.ADMITTED);
    }

    public MeetingJoinRequestDTO denyJoinRequest(Long meetingId, Long requestId, String requesterId) {
        Meeting meeting = findOrThrow(meetingId);
        verifyHost(meeting, requesterId);
        return resolveJoinRequest(meetingId, requestId, JoinRequestStatus.DENIED);
    }

    public List<MeetingJoinRequestDTO> admitAll(Long meetingId, String requesterId) {
        Meeting meeting = findOrThrow(meetingId);
        verifyHost(meeting, requesterId);

        List<MeetingJoinRequest> pending = joinRequestRepository
                .findByMeetingIdAndStatusOrderByRequestedAtAsc(meetingId, JoinRequestStatus.PENDING);

        pending.forEach(r -> {
            r.setStatus(JoinRequestStatus.ADMITTED);
            r.setRespondedAt(LocalDateTime.now());
        });
        joinRequestRepository.saveAll(pending);

        return pending.stream().map(this::toJoinRequestDTO).collect(Collectors.toList());
    }

    // Guest calls this only after polling shows ADMITTED. guestIdentity is
    // the same opaque id issued at requestToJoin() time — it's the guest's
    // only credential, so it must match both the request row and be reused
    // as the LiveKit participant identity.
    public Map<String, String> generateGuestToken(Long meetingId, Long requestId, String guestIdentity, String displayName) {
        Meeting meeting = findOrThrow(meetingId);

        if (meeting.getMeetingStatus() != MeetingStatus.ACTIVE) {
            throw new MeetingException("Meeting is not active — cannot issue a join token");
        }

        MeetingJoinRequest request = joinRequestRepository
                .findByIdAndMeetingIdAndGuestIdentity(requestId, meetingId, guestIdentity)
                .orElseThrow(() -> new MeetingException("Join request not found"));

        if (request.getStatus() != JoinRequestStatus.ADMITTED) {
            throw new MeetingException("Not admitted yet — current status: " + request.getStatus());
        }

        String name = displayName != null ? displayName : request.getGuestName();
        String token = tokenService.generateMeetingToken(meeting.getRoomName(), guestIdentity, name, false);

        return Map.of(
                "room", meeting.getRoomName(),
                "token", token
        );
    }

    // ─────────────────────────────────────────────────────────────
    // SCHEDULER HOOK — called by MeetingScheduler
    // ─────────────────────────────────────────────────────────────

    public void activateDueMeetings() {
        List<Meeting> due = repository.findByMeetingStatusAndScheduledTimeUtcLessThanEqual(
                MeetingStatus.SCHEDULED, LocalDateTime.now());

        for (Meeting meeting : due) {
            meeting.setMeetingStatus(MeetingStatus.ACTIVE);
            repository.save(meeting);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────

    private Meeting findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new MeetingException("Meeting not found: " + id));
    }

    private void verifyHost(Meeting meeting, String requesterId) {
        if (requesterId == null || !requesterId.equals(meeting.getCreatorId())) {
            throw new MeetingException("Only the host can perform this action");
        }
    }

    private MeetingJoinRequestDTO resolveJoinRequest(Long meetingId, Long requestId, JoinRequestStatus newStatus) {
        MeetingJoinRequest request = joinRequestRepository.findByIdAndMeetingId(requestId, meetingId)
                .orElseThrow(() -> new MeetingException("Join request not found"));

        if (request.getStatus() != JoinRequestStatus.PENDING) {
            return toJoinRequestDTO(request); // already resolved — idempotent
        }

        request.setStatus(newStatus);
        request.setRespondedAt(LocalDateTime.now());
        MeetingJoinRequest saved = joinRequestRepository.save(request);
        return toJoinRequestDTO(saved);
    }

    private void assignJoinCodeAndUrl(Meeting meeting) {
        String code;
        do {
            code = JoinCodeGenerator.generate();
        } while (repository.existsByJoinCode(code));

        meeting.setJoinCode(code);
        meeting.setRoomName("meeting-" + code);
        meeting.setMeetingUrl(baseUrl + "/ilmorameet/" + code);
    }

    private LocalDateTime toUtc(String date, String time, String timezone) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            LocalTime localTime = LocalTime.parse(time);
            ZoneId zone = ZoneId.of(timezone);

            ZonedDateTime zonedDateTime = ZonedDateTime.of(localDate, localTime, zone);
            return zonedDateTime.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
        } catch (Exception e) {
            throw new MeetingException("Invalid date/time/timezone: " + e.getMessage());
        }
    }

    private String normalizeCode(String joinCode) {
        return joinCode == null ? null : joinCode.trim().toLowerCase();
    }

    private String blankToDefault(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private MeetingResponseDTO toResponseDTO(Meeting m, String requesterId) {
        MeetingResponseDTO dto = new MeetingResponseDTO();
        dto.setId(m.getId());
        dto.setTitle(m.getTitle());
        dto.setJoinCode(m.getJoinCode());
        dto.setMeetingUrl(m.getMeetingUrl());
        dto.setRoomName(m.getRoomName());
        dto.setCreatorId(m.getCreatorId());
        dto.setCreatorRole(m.getCreatorRole());
        dto.setCreatorName(m.getCreatorName());
        dto.setOrganizationId(m.getOrganizationId());
        dto.setMeetingType(m.getMeetingType() != null ? m.getMeetingType().name() : null);
        dto.setMeetingStatus(m.getMeetingStatus() != null ? m.getMeetingStatus().name() : null);
        dto.setTimezone(m.getTimezone());
        dto.setScheduledTimeUtc(m.getScheduledTimeUtc());
        dto.setCreatedAt(m.getCreatedAt());
        dto.setUpdatedAt(m.getUpdatedAt());
        dto.setEndedAt(m.getEndedAt());
        dto.setReusable(m.getReusable());
        dto.setHost(requesterId != null && requesterId.equals(m.getCreatorId()));
        return dto;
    }

    private MeetingJoinRequestDTO toJoinRequestDTO(MeetingJoinRequest r) {
        return new MeetingJoinRequestDTO(r.getId(), r.getGuestIdentity(), r.getGuestName(), r.getGuestEmail(), r.getStatus().name(), r.getRequestedAt());
    }
    
    public List<MeetingJoinRequestDTO> listAllJoinRequests(Long meetingId, String requesterId) {
        Meeting meeting = findOrThrow(meetingId);
        verifyHost(meeting, requesterId);

        return joinRequestRepository
                .findByMeetingIdOrderByRequestedAtDesc(meetingId)
                .stream()
                .map(this::toJoinRequestDTO)
                .collect(Collectors.toList());
    }
    @Transactional
    public void deleteMeeting(Long id, String requesterId) {
        Meeting meeting = findOrThrow(id);
        verifyHost(meeting, requesterId);

        // Clean up any join-request history first (no DB-level FK, but keep it tidy)
        joinRequestRepository.deleteByMeetingId(id);
        repository.delete(meeting);
    }
    
 // ─────────────────────────────────────────────────────────────
 // CALENDAR — meetings for a given month, grouped by date
 // ─────────────────────────────────────────────────────────────

 public Map<String, List<MeetingResponseDTO>> getMyMeetingsGroupedByDate(String creatorId, String month) {
     List<Meeting> meetings = repository.findByCreatorIdOrderByCreatedAtDesc(creatorId);

     YearMonth targetMonth = (month != null && !month.isBlank())
             ? YearMonth.parse(month)
             : YearMonth.now();

     Map<String, List<MeetingResponseDTO>> grouped = new TreeMap<>();

     for (Meeting m : meetings) {
         // Scheduled meetings are keyed by their scheduled time; instant/
         // already-ended meetings fall back to when they were created.
         LocalDateTime eventTime = m.getScheduledTimeUtc() != null ? m.getScheduledTimeUtc() : m.getCreatedAt();
         if (eventTime == null) continue;

         YearMonth eventMonth = YearMonth.from(eventTime);
         if (!eventMonth.equals(targetMonth)) continue;

         String dateKey = eventTime.toLocalDate().toString(); // "yyyy-MM-dd"
         grouped.computeIfAbsent(dateKey, k -> new java.util.ArrayList<>())
                .add(toResponseDTO(m, creatorId));
     }

     return grouped;
 }
}