package com.lms.live_session.service;

import com.lms.live_session.dto.AvailabilitySlotRequestDTO;
import com.lms.live_session.dto.AvailabilitySlotResponseDTO;
import com.lms.live_session.entity.AvailabilitySlot;
import com.lms.live_session.entity.DayOfWeek;
import com.lms.live_session.repository.AvailabilitySlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class AvailabilityService {

    @Autowired
    private AvailabilitySlotRepository availabilitySlotRepository;

    public AvailabilitySlotResponseDTO createAvailability(AvailabilitySlotRequestDTO dto, String creatorId) {
        if (dto.getDayOfWeek() == null || dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new IllegalArgumentException("dayOfWeek, startTime and endTime are required");
        }

        DayOfWeek day = DayOfWeek.fromString(dto.getDayOfWeek());

        if (!dto.getStartTime().isBefore(dto.getEndTime())) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }

        if (hasOverlap(creatorId, day, dto.getStartTime(), dto.getEndTime(), null)) {
            throw new IllegalArgumentException("This slot overlaps with an existing availability slot on " + day);
        }

        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setDayOfWeek(day);
        slot.setStartTime(dto.getStartTime());
        slot.setEndTime(dto.getEndTime());
        slot.setTimezone(dto.getTimezone());
        slot.setIsRecurring(dto.getIsRecurring() != null ? dto.getIsRecurring() : true);
        slot.setCreatorId(creatorId);

        slot = availabilitySlotRepository.save(slot);

        return mapToDTO(slot);
    }

    public List<AvailabilitySlotResponseDTO> getMyAvailability(String creatorId) {
        List<AvailabilitySlot> slots = availabilitySlotRepository.findByCreatorIdOrderByDayOfWeekAsc(creatorId);

        return slots.stream()
                .sorted(Comparator.comparingInt(s -> s.getDayOfWeek().sortOrder()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public AvailabilitySlotResponseDTO getSlotById(Long slotId, String creatorId) {
        AvailabilitySlot slot = availabilitySlotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Availability slot not found: " + slotId));

        verifyOwnership(slot, creatorId);

        return mapToDTO(slot);
    }

    public AvailabilitySlotResponseDTO updateAvailability(Long slotId, AvailabilitySlotRequestDTO dto, String creatorId) {
        AvailabilitySlot slot = availabilitySlotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Availability slot not found: " + slotId));

        verifyOwnership(slot, creatorId);

        DayOfWeek day = dto.getDayOfWeek() != null ? DayOfWeek.fromString(dto.getDayOfWeek()) : slot.getDayOfWeek();
        LocalTime newStart = dto.getStartTime() != null ? dto.getStartTime() : slot.getStartTime();
        LocalTime newEnd = dto.getEndTime() != null ? dto.getEndTime() : slot.getEndTime();

        if (!newStart.isBefore(newEnd)) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }

        if (hasOverlap(creatorId, day, newStart, newEnd, slotId)) {
            throw new IllegalArgumentException("This slot overlaps with an existing availability slot on " + day);
        }

        slot.setDayOfWeek(day);
        slot.setStartTime(newStart);
        slot.setEndTime(newEnd);
        if (dto.getTimezone() != null) {
            slot.setTimezone(dto.getTimezone());
        }
        if (dto.getIsRecurring() != null) {
            slot.setIsRecurring(dto.getIsRecurring());
        }

        slot = availabilitySlotRepository.save(slot);

        return mapToDTO(slot);
    }

    public void deleteAvailability(Long slotId, String creatorId) {
        AvailabilitySlot slot = availabilitySlotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Availability slot not found: " + slotId));

        verifyOwnership(slot, creatorId);

        availabilitySlotRepository.delete(slot);
    }

    public boolean isAvailableOnDate(String creatorId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        DayOfWeek day = DayOfWeek.fromJavaTime(date.getDayOfWeek());

        List<AvailabilitySlot> slots = availabilitySlotRepository.findByCreatorIdAndDayOfWeek(creatorId, day);

        return slots.stream().anyMatch(slot ->
                !startTime.isBefore(slot.getStartTime()) && !endTime.isAfter(slot.getEndTime())
        );
    }

    public List<AvailabilitySlotResponseDTO> getAvailabilityForDay(String creatorId, LocalDate date) {
        DayOfWeek day = DayOfWeek.fromJavaTime(date.getDayOfWeek());

        List<AvailabilitySlot> slots = availabilitySlotRepository.findByCreatorIdAndDayOfWeek(creatorId, day);

        return slots.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Map<String, List<AvailabilitySlotResponseDTO>> getWeeklyAvailability(String creatorId) {
        List<AvailabilitySlot> slots = availabilitySlotRepository.findByCreatorIdOrderByDayOfWeekAsc(creatorId);

        Map<String, List<AvailabilitySlotResponseDTO>> result = new LinkedHashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            result.put(day.name(), null);
        }

        Map<String, List<AvailabilitySlotResponseDTO>> grouped = slots.stream()
                .map(this::mapToDTO)
                .collect(Collectors.groupingBy(AvailabilitySlotResponseDTO::getDayOfWeek));

        for (Map.Entry<String, List<AvailabilitySlotResponseDTO>> entry : grouped.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }

        result.values().removeIf(v -> v == null);

        return result;
    }

    private void verifyOwnership(AvailabilitySlot slot, String creatorId) {
        if (!slot.getCreatorId().equals(creatorId)) {
            throw new SecurityException("You do not have permission to access this availability slot");
        }
    }

    private AvailabilitySlotResponseDTO mapToDTO(AvailabilitySlot slot) {
        return new AvailabilitySlotResponseDTO(
                slot.getId(),
                slot.getDayOfWeek().name(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getTimezone(),
                slot.getIsRecurring(),
                slot.getCreatedAt()
        );
    }

    private boolean hasOverlap(String creatorId, DayOfWeek dayOfWeek, LocalTime newStart, LocalTime newEnd, Long excludeSlotId) {
        List<AvailabilitySlot> existing = availabilitySlotRepository.findByCreatorIdAndDayOfWeek(creatorId, dayOfWeek);

        return existing.stream()
                .filter(s -> excludeSlotId == null || !s.getId().equals(excludeSlotId))
                .anyMatch(s -> newStart.isBefore(s.getEndTime()) && s.getStartTime().isBefore(newEnd));
    }
}