package com.lms.live_session.service;

import com.lms.live_session.dto.BookedSlotRequest;
import com.lms.live_session.dto.BookedSlotResponse;
import com.lms.live_session.dto.PublicPageResponse;
import com.lms.live_session.entity.*;
import com.lms.live_session.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
@Service
public class BookingService {

    // ── Repositories ──────────────────────────────────────────────────────────

    @Autowired
    private TrainerAvailabilityRepository availabilityRepo;

    @Autowired
    private EventTypeRepository eventTypeRepo;

    @Autowired
    private BookingPageSettingsRepository pageRepo;

    @Autowired
    private BookedSlotRepository slotRepo;

    @Autowired
    private LiveSessionRepository liveSessionRepo;

    /** Base URL used to construct the personal join link returned to the booker */
    @Value("${app.base-url:http://localhost:5173}")
    private String appBaseUrl;

    // =========================================================================
    // AVAILABILITY
    // =========================================================================

    /**
     * Replaces ALL availability entries for a trainer in one atomic operation.
     * The frontend sends the full weekly schedule every time.
     */
//    public List<TrainerAvailability> setAvailability(String trainerEmail,
//                                                     List<TrainerAvailability> slots) {
//        availabilityRepo.deleteByTrainerEmail(trainerEmail);
//        slots.forEach(s -> {
//            s.setTrainerEmail(trainerEmail);
//            s.setId(null); // ensure inserts, not updates
//        });
//        return availabilityRepo.saveAll(slots);
//    }
//    @Transactional
//    public List<TrainerAvailability> setAvailability(String trainerEmail,
//                                                     List<TrainerAvailability> slots) {
//        availabilityRepo.deleteByTrainerEmail(trainerEmail);
//        availabilityRepo.flush();
//        slots.forEach(s -> {
//            s.setTrainerEmail(trainerEmail);
//            s.setId(null);
//            if (s.getIsActive() == null) s.setIsActive(true);
//            if (s.getTimezone() == null) s.setTimezone("Asia/Kolkata");
//        });
//        return availabilityRepo.saveAll(slots);
//    }
    
    @Transactional
    public List<TrainerAvailability> setAvailability(String trainerEmail,
                                                     List<TrainerAvailability> slots) {
        availabilityRepo.deleteByTrainerEmail(trainerEmail);
        availabilityRepo.flush();
        slots.forEach(s -> {
            s.setTrainerEmail(trainerEmail);
            s.setId(null);
            if (s.getIsActive() == null) s.setIsActive(true);
            if (s.getTimezone() == null) s.setTimezone("Asia/Kolkata");
        });
        return availabilityRepo.saveAll(slots);
    }
    public List<TrainerAvailability> getAvailability(String trainerEmail) {
        return availabilityRepo.findByTrainerEmail(trainerEmail);  // ← changed
    }

    // =========================================================================
    // AVAILABLE TIME SLOTS (public)
    // =========================================================================

    /**
     * Computes available start times for a given event type on a given date.
     * Steps:
     *   1. Look up the day's availability window
     *   2. Generate slots every [durationMinutes] minutes inside the window
     *   3. Remove already-booked slots
     *   4. Remove slots that clash with existing live sessions
     */
    public List<LocalTime> getAvailableTimeSlots(String trainerEmail,
                                                 Long eventTypeId,
                                                 LocalDate date) {
        EventType eventType = eventTypeRepo
                .findByTrainerEmailAndId(trainerEmail, eventTypeId)
                .orElseThrow(() -> new RuntimeException("Event type not found"));

        // dayOfWeek: 0=Sun … 6=Sat (matches frontend & entity convention)
        int dow = date.getDayOfWeek().getValue() % 7;

        List<TrainerAvailability> availList =
                availabilityRepo.findByTrainerEmailAndDayOfWeek(trainerEmail, dow);

        if (availList.isEmpty()) return Collections.emptyList();

        int duration = eventType.getDurationMinutes();
        List<LocalTime> slots = new ArrayList<>();

        for (TrainerAvailability avail : availList) {
            if (!Boolean.TRUE.equals(avail.getIsActive())) continue;
            LocalTime cursor = avail.getStartTime();
            while (!cursor.plusMinutes(duration).isAfter(avail.getEndTime())) {
                slots.add(cursor);
                cursor = cursor.plusMinutes(duration);
            }
        }

        // Remove already-booked slots for this event type on this date
        List<BookedSlot> booked = slotRepo
                .findByEventTypeIdAndBookedDateAndStatusNot(eventTypeId, date, "CANCELLED");
        booked.forEach(b -> slots.remove(b.getStartTime()));

        // Remove slots that clash with the trainer's other live sessions
        List<LiveSession> sessions = liveSessionRepo
                .findByTrainerEmailAndScheduledDateBetween(trainerEmail, date, date);
        sessions.forEach(s -> {
            if (s.getScheduledTime() != null) slots.remove(s.getScheduledTime());
        });

        return slots;
    }

    // =========================================================================
    // EVENT TYPES
    // =========================================================================

    public EventType createEventType(String trainerEmail, EventType et) {
        et.setTrainerEmail(trainerEmail);
        et.setId(null);

        // Auto-generate unique URL slug
        String base = et.getName()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        String candidate = base;
        int counter = 2;
        while (eventTypeRepo.existsBySlugAndTrainerEmail(candidate, trainerEmail)) {
            candidate = base + "-" + counter++;
        }
        et.setSlug(candidate);

        return eventTypeRepo.save(et);
    }

    public List<EventType> getEventTypes(String trainerEmail) {
        return eventTypeRepo.findByTrainerEmailAndIsActiveTrue(trainerEmail);
    }

    public EventType updateEventType(String trainerEmail, Long id, EventType updated) {
        EventType existing = eventTypeRepo
                .findByTrainerEmailAndId(trainerEmail, id)
                .orElseThrow(() -> new RuntimeException("Event type not found"));

        existing.setName(updated.getName());
        existing.setDurationMinutes(updated.getDurationMinutes());
        existing.setSessionMode(updated.getSessionMode());
        existing.setExternalUrlTemplate(updated.getExternalUrlTemplate());
        existing.setPrice(updated.getPrice());
        existing.setCurrency(updated.getCurrency());
        existing.setDescription(updated.getDescription());
        existing.setMaxParticipants(updated.getMaxParticipants());

        return eventTypeRepo.save(existing);
    }

    /** Soft-delete: sets isActive = false */
    public void deleteEventType(String trainerEmail, Long id) {
        EventType existing = eventTypeRepo
                .findByTrainerEmailAndId(trainerEmail, id)
                .orElseThrow(() -> new RuntimeException("Event type not found"));
        existing.setIsActive(false);
        eventTypeRepo.save(existing);
    }

    // =========================================================================
    // BOOKING PAGE SETTINGS
    // =========================================================================

    public BookingPageSettings setupBookingPage(String trainerEmail,
                                                BookingPageSettings req) {
        // FIX 4: Require displayName
        if (req.getDisplayName() == null || req.getDisplayName().isBlank()) {
            throw new RuntimeException("Display name is required.");
        }

        // Slug uniqueness check (allow trainer to keep their own slug)
        if (req.getPublicSlug() != null && !req.getPublicSlug().isBlank()) {
            if (pageRepo.existsByPublicSlugAndTrainerEmailNot(req.getPublicSlug(), trainerEmail)) {
                throw new RuntimeException("Slug already taken. Please choose another.");
            }
        }

        BookingPageSettings page = pageRepo
                .findByTrainerEmail(trainerEmail)
                .orElse(new BookingPageSettings());

        page.setTrainerEmail(trainerEmail);
        page.setDisplayName(req.getDisplayName());
        if (req.getLogoUrl() != null) page.setLogoUrl(req.getLogoUrl());
        page.setPublicSlug(req.getPublicSlug());
        page.setBio(req.getBio());
        page.setIsPublished(req.getIsPublished() != null ? req.getIsPublished() : false);

        return pageRepo.save(page);
    }

    public BookingPageSettings getBookingPage(String trainerEmail) {
        return pageRepo.findByTrainerEmail(trainerEmail)
                .orElse(new BookingPageSettings());
    }

    public Map<String, Object> checkSlugAvailability(String slug, String trainerEmail) {
        boolean taken = pageRepo.existsByPublicSlugAndTrainerEmailNot(slug, trainerEmail);
        Map<String, Object> result = new HashMap<>();
        result.put("available", !taken);
        result.put("slug", slug);
        return result;
    }

    // =========================================================================
    // PUBLIC PAGE
    // =========================================================================

    public PublicPageResponse getPublicPage(String slug) {
        BookingPageSettings settings = pageRepo.findByPublicSlug(slug)
                .orElseThrow(() -> new RuntimeException("Booking page not found"));

        // FIX 3: throw a sentinel message so the controller can return HTTP 403
        if (!Boolean.TRUE.equals(settings.getIsPublished())) {
            throw new RuntimeException("NOT_PUBLISHED");
        }

        List<EventType> eventTypes =
                eventTypeRepo.findByTrainerEmailAndIsActiveTrue(settings.getTrainerEmail());

        return new PublicPageResponse(settings, eventTypes);
    }

    // =========================================================================
    // BOOK A SLOT (public)
    // =========================================================================

    public BookedSlotResponse bookSlot(BookedSlotRequest req) {
        // Double-check slot is still free
        boolean conflict = slotRepo
                .existsByTrainerEmailAndBookedDateAndStartTimeAndStatusNot(
                        req.getTrainerEmail(), req.getBookedDate(),
                        req.getStartTime(), "CANCELLED");
        if (conflict) {
            throw new RuntimeException(
                    "This slot is no longer available. Please choose another time.");
        }

        EventType eventType = eventTypeRepo
                .findByTrainerEmailAndId(req.getTrainerEmail(), req.getEventTypeId())
                .orElseThrow(() -> new RuntimeException("Event type not found"));

        BookedSlot slot = new BookedSlot();
        slot.setEventTypeId(req.getEventTypeId());
        slot.setTrainerEmail(req.getTrainerEmail());
        slot.setBookerName(req.getBookerName());
        slot.setBookerEmail(req.getBookerEmail());
        slot.setBookerUserId(req.getBookerUserId());
        slot.setBookedDate(req.getBookedDate());
        slot.setStartTime(req.getStartTime());
        slot.setEndTime(req.getStartTime().plusMinutes(eventType.getDurationMinutes()));
        slot.setTimezone(req.getTimezone() != null ? req.getTimezone() : "Asia/Kolkata");
        slot.setStatus("PENDING");
        slot.setNotes(req.getNotes());
        slot.setUniqueAccessToken(UUID.randomUUID().toString());

        BookedSlot saved = slotRepo.save(slot);

        return toResponse(saved, eventType,
                "Booking confirmed! You'll receive a confirmation email shortly.");
    }

    // =========================================================================
    // BOOKING MANAGEMENT (trainer auth required)
    // =========================================================================

    /**
     * Confirms a pending booking and auto-creates a LiveSession row so the
     * trainer can go live directly from the Booking Requests page.
     */
    public BookedSlotResponse confirmSlot(String trainerEmail, Long slotId) {
        BookedSlot slot = slotRepo.findById(slotId)
                .filter(s -> s.getTrainerEmail().equals(trainerEmail))
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        slot.setStatus("CONFIRMED");

        EventType eventType = eventTypeRepo
                .findByTrainerEmailAndId(trainerEmail, slot.getEventTypeId())
                .orElseThrow(() -> new RuntimeException("Event type not found"));

        // Auto-create a LiveSession so trainer can launch it from dashboard
        LiveSession session = new LiveSession();
        session.setTrainerEmail(trainerEmail);
        session.setTitle(eventType.getName() + " with " + slot.getBookerName());
        session.setScheduledDate(slot.getBookedDate());
        session.setScheduledTime(slot.getStartTime());
        session.setDuration(eventType.getDurationMinutes());
        session.setMeetingType(eventType.getSessionMode());
        session.setStatus("SCHEDULED");
        session.setNotifyStudents(false);

        if ("EXTERNAL".equals(eventType.getSessionMode())
                && eventType.getExternalUrlTemplate() != null) {
            session.setExternalMeetingUrl(eventType.getExternalUrlTemplate());
        }

        LiveSession savedSession = liveSessionRepo.save(session);
        slot.setLiveSessionId(savedSession.getId());

        BookedSlot savedSlot = slotRepo.save(slot);
        return toResponse(savedSlot, eventType, "Booking confirmed. Live session created.");
    }

    public BookedSlotResponse cancelSlot(String trainerEmail, Long slotId) {
        BookedSlot slot = slotRepo.findById(slotId)
                .filter(s -> s.getTrainerEmail().equals(trainerEmail))
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        slot.setStatus("CANCELLED");
        BookedSlot saved = slotRepo.save(slot);

        EventType eventType = eventTypeRepo
                .findByTrainerEmailAndId(trainerEmail, saved.getEventTypeId())
                .orElse(null);

        return toResponse(saved, eventType, "Booking cancelled.");
    }

    // FIX 1: return List<BookedSlotResponse> instead of raw List<BookedSlot>
    public List<BookedSlotResponse> getTrainerBookings(String trainerEmail, String status) {
        List<BookedSlot> slots;
        if (status == null || status.isBlank()) {
            slots = slotRepo.findByTrainerEmailOrderByBookedDateDesc(trainerEmail);
        } else {
            slots = slotRepo.findByTrainerEmailAndStatus(trainerEmail, status.toUpperCase());
        }
        return slots.stream()
                .map(slot -> {
                    EventType et = eventTypeRepo
                            .findByTrainerEmailAndId(trainerEmail, slot.getEventTypeId())
                            .orElse(null);
                    return toResponse(slot, et, null);
                })
                .collect(Collectors.toList());
    }

    // =========================================================================
    // STUDENT / BOOKER VIEWS
    // =========================================================================

    // FIX 1: return List<BookedSlotResponse> instead of raw List<BookedSlot>
    public List<BookedSlotResponse> getBookingsByStudent(String bookerEmail, Long userId) {
        List<BookedSlot> slots;
        if (userId != null) {
            slots = slotRepo.findByBookerUserId(userId);
        } else {
            slots = slotRepo.findByBookerEmail(bookerEmail);
        }
        return slots.stream()
                .map(slot -> {
                    EventType et = eventTypeRepo
                            .findById(slot.getEventTypeId())
                            .orElse(null);
                    BookedSlotResponse resp = toResponse(slot, et, null);
                    // FIX 1: populate trainerName from BookingPageSettings
                    String trainerName = pageRepo.findByTrainerEmail(slot.getTrainerEmail())
                            .map(p -> p.getDisplayName())
                            .orElse(slot.getTrainerEmail());
                    resp.setTrainerName(trainerName);
                    return resp;
                })
                .collect(Collectors.toList());
    }

    public Optional<BookedSlot> getBookingByToken(String token) {
        return slotRepo.findByUniqueAccessToken(token);
    }

    // =========================================================================
    // ANALYTICS (trainer auth required)
    // =========================================================================

    public Map<String, Object> getAnalyticsSummary(String trainerEmail, String range) {
        LocalDate cutoff = getCutoffDate(range);
        List<BookedSlot> slots = slotRepo.findByTrainerEmailOrderByBookedDateDesc(trainerEmail)
                .stream()
                .filter(s -> !s.getBookedDate().isBefore(cutoff))
                .collect(Collectors.toList());

        long total     = slots.size();
        long confirmed = slots.stream().filter(s -> "CONFIRMED".equals(s.getStatus())).count();
        long cancelled = slots.stream().filter(s -> "CANCELLED".equals(s.getStatus())).count();
        long pending   = slots.stream().filter(s -> "PENDING".equals(s.getStatus())).count();

        long sessionMins = slots.stream()
                .filter(s -> "CONFIRMED".equals(s.getStatus()))
                .mapToLong(s -> {
                    EventType et = eventTypeRepo.findById(s.getEventTypeId()).orElse(null);
                    return et != null ? et.getDurationMinutes() : 30;
                })
                .sum();

        Map<String, Object> r = new HashMap<>();
        r.put("total", total);
        r.put("confirmed", confirmed);
        r.put("cancelled", cancelled);
        r.put("pending", pending);
        r.put("sessionHours", Math.round(sessionMins / 60.0 * 10) / 10.0);
        r.put("conversionRate",   total > 0 ? Math.round((double) confirmed / total * 100) + "%" : "0%");
        r.put("cancellationRate", total > 0 ? Math.round((double) cancelled / total * 100) + "%" : "0%");
        return r;
    }

    public List<Map<String, Object>> getAnalyticsTrends(String trainerEmail, String range) {
        List<Map<String, Object>> trends = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate start = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            LocalDate end   = start.plusMonths(1).minusDays(1);

            List<BookedSlot> monthSlots = slotRepo
                    .findByTrainerEmailOrderByBookedDateDesc(trainerEmail)
                    .stream()
                    .filter(s -> !s.getBookedDate().isBefore(start) && !s.getBookedDate().isAfter(end))
                    .collect(Collectors.toList());

            Map<String, Object> entry = new HashMap<>();
            entry.put("month",    start.getMonth().toString().substring(0, 3));
            entry.put("bookings", monthSlots.size());
            entry.put("sessions", monthSlots.stream()
                    .filter(s -> "CONFIRMED".equals(s.getStatus())).count());
            trends.add(entry);
        }
        return trends;
    }

    private LocalDate getCutoffDate(String range) {
        return switch (range) {
            case "week"    -> LocalDate.now().minusWeeks(1);
            case "quarter" -> LocalDate.now().minusMonths(3);
            default        -> LocalDate.now().minusMonths(1);
        };
    }

    // =========================================================================
    // PUBLIC TRAINER LISTING
    // =========================================================================

    public List<Map<String, Object>> getPublishedTrainers() {
        List<BookingPageSettings> pages = pageRepo.findByIsPublishedTrue();
        return pages.stream().map(page -> {
            Map<String, Object> t = new HashMap<>();
            t.put("slug",        page.getPublicSlug());
            t.put("displayName", page.getDisplayName());
            t.put("bio",         page.getBio());
            t.put("logoUrl",     page.getLogoUrl());
            List<EventType> ets =
                    eventTypeRepo.findByTrainerEmailAndIsActiveTrue(page.getTrainerEmail());
            t.put("eventTypes", ets);
            t.put("specializations", ets.stream()
                    .map(EventType::getName)
                    .collect(Collectors.toList()));
            return t;
        }).collect(Collectors.toList());
    }

    // =========================================================================
    // HELPER – entity → response DTO
    // =========================================================================

    private BookedSlotResponse toResponse(BookedSlot slot,
                                          EventType eventType,
                                          String message) {
        String joinLink  = appBaseUrl + "/join/" + slot.getUniqueAccessToken();
        String etName    = eventType != null ? eventType.getName() : "Session";
        Integer duration = eventType != null ? eventType.getDurationMinutes() : null;

        BookedSlotResponse resp = new BookedSlotResponse(
                slot.getId(),
                slot.getBookerName(),
                slot.getBookerEmail(),
                slot.getTrainerEmail(),
                etName,
                duration,
                slot.getBookedDate(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getStatus(),
                joinLink,
                slot.getUniqueAccessToken(),
                message,
                slot.getLiveSessionId()
        );

        // FIX 2: populate createdAt
        resp.setCreatedAt(slot.getCreatedAt());

        return resp;
    }
}