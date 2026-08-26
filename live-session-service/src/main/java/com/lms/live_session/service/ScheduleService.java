package com.lms.live_session.service;

import com.lms.live_session.dto.ScheduleRequestDTO;
import com.lms.live_session.dto.ScheduleResponseDTO;
import com.lms.live_session.entity.Schedule;
import com.lms.live_session.entity.ScheduleReminder;
import com.lms.live_session.entity.ScheduleType;
import com.lms.live_session.exception.MeetingException;
import com.lms.live_session.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ScheduleService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    
    private final ScheduleRepository scheduleRepository;
    private final ReminderService reminderService;
    private final MeetingService meetingService;

    @Autowired
    public ScheduleService(ScheduleRepository scheduleRepository, ReminderService reminderService,
                            MeetingService meetingService) {
        this.scheduleRepository = scheduleRepository;
        this.reminderService = reminderService;
        this.meetingService = meetingService;
    }
//    public ScheduleResponseDTO createSchedule(ScheduleRequestDTO dto, String creatorId) {
//        validateRequired(dto);
//
//        Schedule schedule = new Schedule();
//        applyDtoToSchedule(dto, schedule);
////        schedule.setCreatorId(creatorId);
////        schedule.setCreatorName(dto.getCreatorName());
////        schedule.setOrganizationId(dto.getOrganizationId());
//        schedule.setCreatorId(creatorId);
//        schedule.setCreatorName(
//            StringUtils.hasText(dto.getCreatorName()) ? dto.getCreatorName() : creatorId
//        );
//       
//        schedule.setOrganizationId(dto.getOrganizationId());
//        schedule = scheduleRepository.save(schedule);
//        createReminderIfNeeded(schedule.getId(), schedule.getReminder(), creatorId);
//        return mapToDTO(schedule);
//    }

    public ScheduleResponseDTO createSchedule(ScheduleRequestDTO dto, String creatorId, String creatorRole) {
        validateRequired(dto);

        Schedule schedule = new Schedule();
        applyDtoToSchedule(dto, schedule);
        schedule.setCreatorId(creatorId);
        schedule.setCreatorName(
            StringUtils.hasText(dto.getCreatorName()) ? dto.getCreatorName() : creatorId
        );

        schedule.setOrganizationId(dto.getOrganizationId());

        // NEW — create a real joinable meeting for video-type schedules
        // NEW — create a real joinable meeting for video-type schedules
        if (requiresMeetingLink(schedule.getType())) {
            try {
                var meetingDto = meetingService.createMeetingForSchedule(
                        schedule.getTitle(),
                        dto.getDate(),
                        dto.getStartTime(),
                        dto.getEndTime(),
                        java.time.ZoneId.systemDefault().getId(),
                        creatorId,
                        creatorRole,
                        schedule.getCreatorName(),
                        schedule.getOrganizationId()
                );
                schedule.setMeetingId(meetingDto.getId());
                schedule.setMeetingUrl(meetingDto.getMeetingUrl());
                schedule.setJoinCode(meetingDto.getJoinCode());
            } catch (Exception e) {
                System.err.println("Failed to create meeting for schedule: " + e.getMessage());
            }
        }

        schedule = scheduleRepository.save(schedule);
        createReminderIfNeeded(schedule.getId(), schedule.getReminder(), creatorId);
        return mapToDTO(schedule);
    }
    
    public List<ScheduleResponseDTO> getMySchedules(String creatorId) {
        List<Schedule> schedules = scheduleRepository.findByCreatorIdOrderByCreatedAtDesc(creatorId);
        return schedules.stream()
                .sorted(Comparator.comparing(Schedule::getDate).thenComparing(Schedule::getStartTime))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ScheduleResponseDTO getScheduleById(Long scheduleId, String creatorId) {
        Schedule schedule = findOrThrow(scheduleId);
        verifyOwnership(schedule, creatorId);
        return mapToDTO(schedule);
    }

    public ScheduleResponseDTO updateSchedule(Long scheduleId, ScheduleRequestDTO dto, String creatorId) {
        Schedule schedule = findOrThrow(scheduleId);
        verifyOwnership(schedule, creatorId);

        applyDtoToSchedule(dto, schedule);

        schedule = scheduleRepository.save(schedule);
        reminderService.deleteRemindersByScheduleId(scheduleId);
        createReminderIfNeeded(scheduleId, schedule.getReminder(), creatorId);
        return mapToDTO(schedule);
    }

    public void deleteSchedule(Long scheduleId, String creatorId) {
        Schedule schedule = findOrThrow(scheduleId);
        verifyOwnership(schedule, creatorId);
        reminderService.deleteRemindersByScheduleId(scheduleId);
        scheduleRepository.delete(schedule);
    }

    public List<ScheduleResponseDTO> getSchedulesByTab(String creatorId, String tab) {
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;

        String normalizedTab = tab == null ? "" : tab.trim().toLowerCase();

        switch (normalizedTab) {
            case "today":
                startDate = today;
                endDate = today;
                break;
            case "this week":
                startDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                endDate = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                break;
            case "this month":
                YearMonth currentMonth = YearMonth.from(today);
                startDate = currentMonth.atDay(1);
                endDate = currentMonth.atEndOfMonth();
                break;
            case "upcoming":
                startDate = today;
                endDate = today.plusDays(90);
                break;
            default:
                throw new MeetingException("Invalid tab: " + tab
                        + " (expected Upcoming, Today, This Week, or This Month)");
        }

        List<Schedule> schedules = scheduleRepository.findByCreatorIdAndDateBetween(creatorId, startDate, endDate);
        return schedules.stream()
                .sorted(Comparator.comparing(Schedule::getDate).thenComparing(Schedule::getStartTime))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Map<LocalDate, List<ScheduleResponseDTO>> getSchedulesGroupedByDate(String creatorId, String month) {
        YearMonth yearMonth = StringUtils.hasText(month)
                ? YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"))
                : YearMonth.now();

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Schedule> schedules = scheduleRepository.findByCreatorIdAndDateBetween(creatorId, startDate, endDate);

        Map<LocalDate, List<ScheduleResponseDTO>> grouped = new LinkedHashMap<>();
        schedules.stream()
                .sorted(Comparator.comparing(Schedule::getDate).thenComparing(Schedule::getStartTime))
                .forEach(s -> grouped.computeIfAbsent(s.getDate(), d -> new ArrayList<>()).add(mapToDTO(s)));

        return grouped;
    }

    // ==========================================================
    // Helpers
    // ==========================================================

    private Schedule findOrThrow(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new MeetingException("Schedule not found: " + scheduleId));
    }

    private void verifyOwnership(Schedule schedule, String creatorId) {
        if (creatorId == null || !creatorId.equals(schedule.getCreatorId())) {
            throw new MeetingException("Not authorized to access this schedule");
        }
    }

    private void validateRequired(ScheduleRequestDTO dto) {
        if (!StringUtils.hasText(dto.getTitle())) throw new MeetingException("title is required");
        if (!StringUtils.hasText(dto.getDate())) throw new MeetingException("date is required");
        if (!StringUtils.hasText(dto.getStartTime())) throw new MeetingException("startTime is required");
        if (!StringUtils.hasText(dto.getEndTime())) throw new MeetingException("endTime is required");

        if (StringUtils.hasText(dto.getType())) {
            ScheduleType.fromValue(dto.getType());
        }
        if (StringUtils.hasText(dto.getReminder())) {
            ScheduleReminder.fromValue(dto.getReminder());
        }
    }

    private void applyDtoToSchedule(ScheduleRequestDTO dto, Schedule schedule) {
        schedule.setTitle(dto.getTitle());
        schedule.setDate(LocalDate.parse(dto.getDate(), DATE_FMT));
        schedule.setStartTime(LocalTime.parse(dto.getStartTime(), TIME_FMT));
        schedule.setEndTime(LocalTime.parse(dto.getEndTime(), TIME_FMT));
        schedule.setType(StringUtils.hasText(dto.getType())
                ? ScheduleType.fromValue(dto.getType()).name() : null);
        schedule.setLocation(dto.getLocation());
        schedule.setDescription(dto.getDescription());
        schedule.setReminder(StringUtils.hasText(dto.getReminder())
                ? ScheduleReminder.fromValue(dto.getReminder()).getValue() : null);
        if (dto.getCreatorName() != null) {
            schedule.setCreatorName(dto.getCreatorName());
        }
        if (dto.getOrganizationId() != null) {
            schedule.setOrganizationId(dto.getOrganizationId());
        }
    }

    private ScheduleResponseDTO mapToDTO(Schedule schedule) {
        ScheduleResponseDTO dto = new ScheduleResponseDTO();
        dto.setId(schedule.getId());
        dto.setTitle(schedule.getTitle());
        dto.setDate(schedule.getDate());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setType(schedule.getType());
        dto.setLocation(schedule.getLocation());
        dto.setDescription(schedule.getDescription());
        dto.setReminder(schedule.getReminder());
        dto.setCreatorId(schedule.getCreatorId());
        dto.setCreatorName(schedule.getCreatorName());
        dto.setOrganizationId(schedule.getOrganizationId());
        dto.setMeetingId(schedule.getMeetingId());
        dto.setMeetingUrl(schedule.getMeetingUrl());
        dto.setJoinCode(schedule.getJoinCode());
        dto.setCreatedAt(schedule.getCreatedAt());
        dto.setUpdatedAt(schedule.getUpdatedAt());
        
        return dto;
    }
    private void createReminderIfNeeded(Long scheduleId, String reminderValue, String creatorId) {
        if (reminderValue == null || "NO_REMINDER".equalsIgnoreCase(reminderValue)) {
            return;
        }
        try {
            com.lms.live_session.dto.ReminderRequestDTO reminderDto = new com.lms.live_session.dto.ReminderRequestDTO();
            reminderDto.setScheduleId(scheduleId);
            reminderDto.setReminderTime(reminderValue);
            reminderService.createReminder(reminderDto, creatorId);
        } catch (Exception e) {
            System.err.println("Failed to create reminder for scheduleId=" + scheduleId + ": " + e.getMessage());
        }
    }
    private boolean requiresMeetingLink(String type) {
        return true; // every schedule type now gets a real meeting link
    }
}