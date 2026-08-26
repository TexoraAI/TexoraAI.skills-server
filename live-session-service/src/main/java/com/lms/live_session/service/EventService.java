package com.lms.live_session.service;

import com.lms.live_session.dto.EventAttendeeDTO;
import com.lms.live_session.dto.EventRequestDTO;
import com.lms.live_session.dto.EventResponseDTO;
import com.lms.live_session.dto.MeetingResponseDTO;
import com.lms.live_session.entity.Event;
import com.lms.live_session.entity.EventAttendee;
import com.lms.live_session.entity.EventAttendeeStatus;
import com.lms.live_session.entity.EventAttendeeType;
import com.lms.live_session.entity.EventAvailability;
import com.lms.live_session.entity.EventReminder;
import com.lms.live_session.entity.EventRepeat;
import com.lms.live_session.entity.Meeting;
import com.lms.live_session.exception.MeetingException;
import com.lms.live_session.repository.EventAttendeeRepository;
import com.lms.live_session.repository.EventRepository;
import com.lms.live_session.repository.MeetingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.lms.live_session.event.SessionNotificationEvent;
@Service
@Transactional
public class EventService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final EventRepository eventRepository;
    private final EventAttendeeRepository attendeeRepository;
    private final MeetingService meetingService;
    private final MeetingRepository meetingRepository;
    private final ReminderService reminderService;
    private final com.lms.live_session.kafka.NotificationProducer notificationProducer;
    @Autowired
    public EventService(EventRepository eventRepository,
                         EventAttendeeRepository attendeeRepository,
                         MeetingService meetingService,
                         MeetingRepository meetingRepository,
                         ReminderService reminderService,
                         com.lms.live_session.kafka.NotificationProducer notificationProducer) {
        this.eventRepository = eventRepository;
        this.attendeeRepository = attendeeRepository;
        this.meetingService = meetingService;
        this.meetingRepository = meetingRepository;
        this.reminderService = reminderService;
        this.notificationProducer = notificationProducer;
    }

    public EventResponseDTO createEvent(EventRequestDTO dto, String creatorId, String creatorRole) {
        validateRequired(dto);

        Event event = new Event();
        applyDtoToEvent(dto, event);
        event.setCreatorId(creatorId);
        event.setCreatorRole(creatorRole);
        event.setCreatorName(dto.getCreatorName());
        event.setOrganizationId(dto.getOrganizationId());

        event = eventRepository.save(event);

        saveAttendees(event.getId(), dto.getRequiredAttendees(), EventAttendeeType.REQUIRED.name());
        saveAttendees(event.getId(), dto.getOptionalAttendees(), EventAttendeeType.OPTIONAL.name());

        createReminderIfNeeded(event.getId(), event.getReminder(), creatorId);

        if ("ONLINE".equalsIgnoreCase(event.getMode())) {
            MeetingResponseDTO meeting = meetingService.createMeetingForEvent(event);
            if (meeting != null && meeting.getId() != null) {
                event.setMeetingId(meeting.getId());
                event = eventRepository.save(event);
            }
        }
        notifyAttendeesOfEventCreation(event, dto);
        return mapToDTO(event);
    }


    public List<EventResponseDTO> getMyEvents(String creatorId) {
        List<Event> events = eventRepository.findByCreatorIdOrderByCreatedAtDesc(creatorId);
        Map<Long, List<EventAttendeeDTO>> attendeesByEventId = batchLoadAttendees(events);
        return events.stream().map(e -> mapToDTO(e, attendeesByEventId)).collect(Collectors.toList());
    }

    public EventResponseDTO getEventById(Long eventId, String requesterId) {
        Event event = findOrThrow(eventId);
        if (requesterId == null || !requesterId.equals(event.getCreatorId())) {
            throw new MeetingException("Not authorized to view this event");
        }
        return mapToDTO(event);
    }

    public EventResponseDTO updateEvent(Long eventId, EventRequestDTO dto, String creatorId) {
        Event event = findOrThrow(eventId);
        if (creatorId == null || !creatorId.equals(event.getCreatorId())) {
            throw new MeetingException("Not authorized to update this event");
        }

        String oldMode = event.getMode();
        applyDtoToEvent(dto, event);
        String newMode = event.getMode();

        attendeeRepository.deleteByEventId(eventId);
        saveAttendees(eventId, dto.getRequiredAttendees(), EventAttendeeType.REQUIRED.name());
        saveAttendees(eventId, dto.getOptionalAttendees(), EventAttendeeType.OPTIONAL.name());

        reminderService.deleteRemindersByEventId(eventId);
        createReminderIfNeeded(eventId, event.getReminder(), creatorId);
        if ("ONLINE".equalsIgnoreCase(oldMode) && "IN_PERSON".equalsIgnoreCase(newMode)) {
            if (event.getMeetingId() != null) {
                meetingService.deleteMeeting(event.getMeetingId(), event.getCreatorId());
                event.setMeetingId(null);
            }
        } else if ("IN_PERSON".equalsIgnoreCase(oldMode) && "ONLINE".equalsIgnoreCase(newMode)) {
            MeetingResponseDTO meeting = meetingService.createMeetingForEvent(event);
            if (meeting != null && meeting.getId() != null) {
                event.setMeetingId(meeting.getId());
            }
        }

        event = eventRepository.save(event);
        return mapToDTO(event);
    }


    public void deleteEvent(Long eventId, String creatorId) {
        Event event = findOrThrow(eventId);
        if (creatorId == null || !creatorId.equals(event.getCreatorId())) {
            throw new MeetingException("Not authorized to cancel this event");
        }

        event.setStatus("CANCELLED");
        eventRepository.save(event);
    }
    
    public EventResponseDTO restoreEvent(Long eventId, String creatorId) {
        Event event = findOrThrow(eventId);
        if (creatorId == null || !creatorId.equals(event.getCreatorId())) {
            throw new MeetingException("Not authorized to restore this event");
        }

        event.setStatus("ACTIVE");
        event = eventRepository.save(event);
        return mapToDTO(event);
    }

    public Map<LocalDate, List<EventResponseDTO>> getMyEventsGroupedByDate(String creatorId, String month) {
        YearMonth yearMonth = StringUtils.hasText(month)
                ? YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"))
                : YearMonth.now();

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();


        List<Event> events = eventRepository.findByCreatorIdAndDateBetween(creatorId, startDate, endDate);
        Map<Long, List<EventAttendeeDTO>> attendeesByEventId = batchLoadAttendees(events);

        Map<LocalDate, List<EventResponseDTO>> grouped = new LinkedHashMap<>();
        events.stream()
                .sorted(Comparator.comparing(Event::getDate).thenComparing(Event::getStartTime))
                .forEach(e -> grouped.computeIfAbsent(e.getDate(), d -> new ArrayList<>()).add(mapToDTO(e, attendeesByEventId)));

        return grouped;
    }
    


    public List<EventResponseDTO> getEventsByDateRange(String creatorId, LocalDate startDate, LocalDate endDate) {
        List<Event> events = eventRepository.findByCreatorIdAndDateBetween(creatorId, startDate, endDate);
        Map<Long, List<EventAttendeeDTO>> attendeesByEventId = batchLoadAttendees(events);
        return events.stream()
                .sorted(Comparator.comparing(Event::getDate).thenComparing(Event::getStartTime))
                .map(e -> mapToDTO(e, attendeesByEventId))
                .collect(Collectors.toList());
    }

    private Event findOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new MeetingException("Event not found: " + eventId));
    }

    private void validateRequired(EventRequestDTO dto) {
        if (!StringUtils.hasText(dto.getTitle())) throw new MeetingException("title is required");
        if (!StringUtils.hasText(dto.getDate())) throw new MeetingException("date is required");
        if (!StringUtils.hasText(dto.getStartTime())) throw new MeetingException("startTime is required");
        if (!StringUtils.hasText(dto.getEndTime())) throw new MeetingException("endTime is required");
        if (!StringUtils.hasText(dto.getMode())) throw new MeetingException("mode is required");
        if (!"ONLINE".equalsIgnoreCase(dto.getMode()) && !"IN_PERSON".equalsIgnoreCase(dto.getMode())) {
            throw new MeetingException("mode must be ONLINE or IN_PERSON");
        }
        if (StringUtils.hasText(dto.getReminder())) EventReminder.fromValue(dto.getReminder());
        if (StringUtils.hasText(dto.getRepeat())) EventRepeat.fromValue(dto.getRepeat());
        if (StringUtils.hasText(dto.getAvailability())) EventAvailability.fromValue(dto.getAvailability());
    }

    private void applyDtoToEvent(EventRequestDTO dto, Event event) {
        event.setTitle(dto.getTitle());
        event.setDate(LocalDate.parse(dto.getDate(), DATE_FMT));
        event.setStartTime(LocalTime.parse(dto.getStartTime(), TIME_FMT));
        event.setEndTime(LocalTime.parse(dto.getEndTime(), TIME_FMT));
        event.setMode(dto.getMode() != null ? dto.getMode().toUpperCase() : null);
        event.setLocation(dto.getLocation());
        event.setDescription(dto.getDescription());
        event.setPurpose(dto.getPurpose());
        event.setReminder(StringUtils.hasText(dto.getReminder()) ? EventReminder.fromValue(dto.getReminder()).getValue() : null);
        event.setRepeat(StringUtils.hasText(dto.getRepeat()) ? EventRepeat.fromValue(dto.getRepeat()).getValue() : null);
        event.setAvailability(StringUtils.hasText(dto.getAvailability()) ? EventAvailability.fromValue(dto.getAvailability()).getValue() : null);
        if (dto.getWaitingRoom() != null) event.setWaitingRoom(dto.getWaitingRoom());
        if (dto.getMuteOnEntry() != null) event.setMuteOnEntry(dto.getMuteOnEntry());
        if (dto.getRecordMeeting() != null) event.setRecordMeeting(dto.getRecordMeeting());
        if (dto.getAllowScreenShare() != null) event.setAllowScreenShare(dto.getAllowScreenShare());
        if (dto.getCreatorName() != null) event.setCreatorName(dto.getCreatorName());
        if (dto.getOrganizationId() != null) event.setOrganizationId(dto.getOrganizationId());
    }

    private void saveAttendees(Long eventId, List<String> emails, String type) {
        if (emails == null) return;
        for (String email : emails) {
            if (!StringUtils.hasText(email)) continue;
            if (attendeeRepository.existsByEventIdAndAttendeeEmail(eventId, email)) continue;
            EventAttendee attendee = new EventAttendee();
            attendee.setEventId(eventId);
            attendee.setAttendeeEmail(email);
            attendee.setType(type);
            attendee.setStatus(EventAttendeeStatus.PENDING.name());
            attendeeRepository.save(attendee);
        }
    }


    private EventResponseDTO mapToDTO(Event event) {
        return mapToDTO(event, null);
    }

    private EventResponseDTO mapToDTO(Event event, Map<Long, List<EventAttendeeDTO>> attendeesByEventId) {
        List<EventAttendeeDTO> attendeeDTOs;
        if (attendeesByEventId != null) {
            attendeeDTOs = attendeesByEventId.getOrDefault(event.getId(), List.of());
        } else {
            attendeeDTOs = attendeeRepository.findByEventId(event.getId()).stream()
                    .map(a -> new EventAttendeeDTO(a.getAttendeeEmail(), a.getType(), a.getStatus(), a.getCreatedAt()))
                    .collect(Collectors.toList());
        }

        String meetingUrl = null;
        String joinCode = null;

        if (event.getMeetingId() != null) {
            Optional<Meeting> meetingOpt = meetingRepository.findById(event.getMeetingId());
            if (meetingOpt.isPresent()) {
                Meeting meeting = meetingOpt.get();
                meetingUrl = meeting.getMeetingUrl();
                joinCode = meeting.getJoinCode();
            }
        }

        EventResponseDTO responseDTO = new EventResponseDTO();
        responseDTO.setId(event.getId());
        responseDTO.setTitle(event.getTitle());
        responseDTO.setDate(event.getDate());
        responseDTO.setStartTime(event.getStartTime());
        responseDTO.setEndTime(event.getEndTime());
        responseDTO.setMode(event.getMode());
        responseDTO.setLocation(event.getLocation());
        responseDTO.setDescription(event.getDescription());
        responseDTO.setPurpose(event.getPurpose());
        responseDTO.setReminder(event.getReminder());
        responseDTO.setRepeat(event.getRepeat());
        responseDTO.setAvailability(event.getAvailability());
        responseDTO.setStatus(event.getStatus());
        responseDTO.setWaitingRoom(event.getWaitingRoom());
        responseDTO.setMuteOnEntry(event.getMuteOnEntry());
        responseDTO.setRecordMeeting(event.getRecordMeeting());
        responseDTO.setAllowScreenShare(event.getAllowScreenShare());
        responseDTO.setCreatorId(event.getCreatorId());
        responseDTO.setCreatorRole(event.getCreatorRole());
        responseDTO.setCreatorName(event.getCreatorName());
        responseDTO.setOrganizationId(event.getOrganizationId());
        responseDTO.setMeetingId(event.getMeetingId());
        responseDTO.setMeetingUrl(meetingUrl);
        responseDTO.setJoinCode(joinCode);
        responseDTO.setAttendees(attendeeDTOs);
        responseDTO.setCreatedAt(event.getCreatedAt());
        responseDTO.setUpdatedAt(event.getUpdatedAt());

        return responseDTO;
    }
    private Map<Long, List<EventAttendeeDTO>> batchLoadAttendees(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        if (eventIds.isEmpty()) return Map.of();

        return attendeeRepository.findByEventIdIn(eventIds).stream()
                .map(a -> new java.util.AbstractMap.SimpleEntry<>(
                        a.getEventId(),
                        new EventAttendeeDTO(a.getAttendeeEmail(), a.getType(), a.getStatus(), a.getCreatedAt())))
                .collect(Collectors.groupingBy(
                        java.util.Map.Entry::getKey,
                        Collectors.mapping(java.util.Map.Entry::getValue, Collectors.toList())));
    }
    private void createReminderIfNeeded(Long eventId, String reminderValue, String creatorId) {
        if (reminderValue == null || "NO_REMINDER".equalsIgnoreCase(reminderValue)) {
            return;
        }
        try {
            com.lms.live_session.dto.ReminderRequestDTO reminderDto = new com.lms.live_session.dto.ReminderRequestDTO();
            reminderDto.setEventId(eventId);
            reminderDto.setReminderTime(reminderValue);
            reminderService.createReminder(reminderDto, creatorId);
        } catch (Exception e) {
            System.err.println("Failed to create reminder for eventId=" + eventId + ": " + e.getMessage());
        }
    }
    private void notifyAttendeesOfEventCreation(Event event, EventRequestDTO dto) {
        java.util.List<String> allAttendees = new java.util.ArrayList<>();
        if (dto.getRequiredAttendees() != null) allAttendees.addAll(dto.getRequiredAttendees());
        if (dto.getOptionalAttendees() != null) allAttendees.addAll(dto.getOptionalAttendees());

        if (allAttendees.isEmpty()) return;

        String sessionLink = null;
        if (event.getMeetingId() != null) {
            sessionLink = meetingRepository.findById(event.getMeetingId())
                    .map(Meeting::getMeetingUrl)
                    .orElse(null);
        }

        for (String attendeeEmail : allAttendees) {
            if (!StringUtils.hasText(attendeeEmail)) continue;
            try {
                SessionNotificationEvent notification = new SessionNotificationEvent(
                        event.getId(),
                        null,
                        null,
                        event.getTitle(),
                        event.getDate() == null ? null : event.getDate().toString(),
                        event.getStartTime() == null ? null : event.getStartTime().toString(),
                        null,
                        "MEETING_INVITE",
                        attendeeEmail,
                        null,
                        null,
                        sessionLink
                );
                notificationProducer.sendMeetingInvite(notification);
            } catch (Exception e) {
                System.err.println("Failed to publish meeting-invite event for eventId=" + event.getId()
                        + ", attendee=" + attendeeEmail + ": " + e.getMessage());
            }
        }
    }
}