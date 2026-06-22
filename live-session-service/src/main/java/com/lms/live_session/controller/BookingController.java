package com.lms.live_session.controller;

import com.lms.live_session.dto.BookedSlotRequest;
import com.lms.live_session.dto.BookedSlotResponse;
import com.lms.live_session.dto.PublicPageResponse;
import com.lms.live_session.entity.*;
import com.lms.live_session.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * BookingController
 * Base path: /api/live-sessions/v1/booking
 *
 * ─── Authenticated endpoints (JWT Bearer token required) ──────────────────
 *   POST   /availability
 *   GET    /availability
 *   POST   /event-types
 *   GET    /event-types
 *   PUT    /event-types/{id}
 *   DELETE /event-types/{id}
 *   POST   /page/setup
 *   GET    /page/settings
 *   GET    /page/check-slug?slug=...
 *   GET    /requests[?status=PENDING|CONFIRMED|CANCELLED]
 *   PATCH  /requests/{id}/confirm
 *   PATCH  /requests/{id}/cancel
 *   GET    /my-bookings[?userId=...]
 *   GET    /analytics/summary[?range=week|month|quarter]
 *   GET    /analytics/trends[?range=week|month|quarter]
 *
 * ─── Public endpoints (no auth – added to SecurityConfig permitAll) ────────
 *   GET    /public/{slug}
 *   GET    /public/{slug}/{eventTypeId}/slots?date=YYYY-MM-DD
 *   POST   /public/{slug}/{eventTypeId}/book
 *   GET    /public/verify/{token}
 *   GET    /public/trainers
 */
@RestController
@RequestMapping("/api/live-sessions/v1/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // =========================================================================
    // INNER CLASS – uniform error envelope
    // =========================================================================

    static class ErrorResponse {
        public String error;
        public String message;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public ErrorResponse(String error, String message) {
            this.error   = error;
            this.message = message;
        }
    }

    // =========================================================================
    // AVAILABILITY  (auth required)
    // =========================================================================

    /**
     * Replace the trainer's full weekly availability schedule.
     * Body: JSON array of TrainerAvailability objects (without id / trainerEmail –
     *       those are filled server-side).
     */
 // Add this private method at the top of the class
    private ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(401)
            .body(new ErrorResponse("Unauthorized", "Valid JWT token required"));
    }
//    @PostMapping("/availability")
//    public ResponseEntity<?> setAvailability(
//            @RequestBody List<TrainerAvailability> slots,
//            Principal principal) {
//        try {
//            return ResponseEntity.ok(
//                    bookingService.setAvailability(principal.getName(), slots));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
//        }
//    }
//
//    /** Returns all active availability blocks for the authenticated trainer. */
//    @GetMapping("/availability")
//    public ResponseEntity<?> getAvailability(Principal principal) {
//        try {
//            return ResponseEntity.ok(bookingService.getAvailability(principal.getName()));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
//        }
//    }
    @PostMapping("/availability")
    public ResponseEntity<?> setAvailability(
            @RequestBody List<TrainerAvailability> slots,
            Principal principal) {
        if (principal == null) return unauthorized();  // ← add this
        try {
            return ResponseEntity.ok(
                    bookingService.setAvailability(principal.getName(), slots));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/availability")
    public ResponseEntity<?> getAvailability(Principal principal) {
        if (principal == null) return unauthorized();  // ← add this
        try {
            return ResponseEntity.ok(bookingService.getAvailability(principal.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // repeat for ALL methods that use principal:
    // createEventType, getEventTypes, updateEventType, deleteEventType,
    // setupBookingPage, getBookingPageSettings, checkSlugAvailability,
    // getBookingRequests, confirmBooking, cancelBooking,
    // getMyBookings, getAnalyticsSummary, getAnalyticsTrends
    // =========================================================================
    // EVENT TYPES  (auth required)
    // =========================================================================

    @PostMapping("/event-types")
    public ResponseEntity<?> createEventType(
            @RequestBody EventType eventType,
            Principal principal) {
        try {
            return ResponseEntity.ok(
                    bookingService.createEventType(principal.getName(), eventType));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/event-types")
    public ResponseEntity<?> getEventTypes(Principal principal) {
        try {
            return ResponseEntity.ok(bookingService.getEventTypes(principal.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/event-types/{id}")
    public ResponseEntity<?> updateEventType(
            @PathVariable Long id,
            @RequestBody EventType eventType,
            Principal principal) {
        try {
            return ResponseEntity.ok(
                    bookingService.updateEventType(principal.getName(), id, eventType));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/event-types/{id}")
    public ResponseEntity<?> deleteEventType(
            @PathVariable Long id,
            Principal principal) {
        try {
            bookingService.deleteEventType(principal.getName(), id);
            return ResponseEntity.ok(Map.of("message", "Event type deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // =========================================================================
    // BOOKING PAGE SETTINGS  (auth required)
    // =========================================================================

    @PostMapping("/page/setup")
    public ResponseEntity<?> setupBookingPage(
            @RequestBody BookingPageSettings settings,
            Principal principal) {
        try {
            return ResponseEntity.ok(
                    bookingService.setupBookingPage(principal.getName(), settings));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/page/settings")
    public ResponseEntity<?> getBookingPageSettings(Principal principal) {
        try {
            return ResponseEntity.ok(bookingService.getBookingPage(principal.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /** Check whether a desired public slug is available. */
    @GetMapping("/page/check-slug")
    public ResponseEntity<?> checkSlugAvailability(
            @RequestParam String slug,
            Principal principal) {
        try {
            return ResponseEntity.ok(
                    bookingService.checkSlugAvailability(slug, principal.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // =========================================================================
    // BOOKING MANAGEMENT  (auth required – trainer actions)
    // =========================================================================

    /**
     * Returns all bookings for the authenticated trainer.
     * @param status optional filter: PENDING | CONFIRMED | CANCELLED | COMPLETED
     */
    @GetMapping("/requests")
    public ResponseEntity<?> getBookingRequests(
            @RequestParam(required = false) String status,
            Principal principal) {
        try {
            return ResponseEntity.ok(
                    bookingService.getTrainerBookings(principal.getName(), status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /** Confirm a pending booking and auto-create the LiveSession. */
    @PatchMapping("/requests/{id}/confirm")
    public ResponseEntity<?> confirmBooking(
            @PathVariable Long id,
            Principal principal) {
        try {
            BookedSlotResponse response =
                    bookingService.confirmSlot(principal.getName(), id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /** Cancel a booking. */
    @PatchMapping("/requests/{id}/cancel")
    public ResponseEntity<?> cancelBooking(
            @PathVariable Long id,
            Principal principal) {
        try {
            BookedSlotResponse response =
                    bookingService.cancelSlot(principal.getName(), id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // =========================================================================
    // STUDENT  –  view own bookings  (auth required)
    // =========================================================================

    /**
     * Returns bookings for the authenticated student.
     * @param userId optional ILM ORA user ID; falls back to email lookup.
     */
    @GetMapping("/my-bookings")
    public ResponseEntity<?> getMyBookings(
            @RequestParam(required = false) Long userId,
            Principal principal) {
        try {
            return ResponseEntity.ok(
                    bookingService.getBookingsByStudent(principal.getName(), userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // =========================================================================
    // ANALYTICS  (auth required – trainer)
    // =========================================================================

    /**
     * Returns aggregated booking statistics for the authenticated trainer.
     * @param range  week | month (default) | quarter
     */
    @GetMapping("/analytics/summary")
    public ResponseEntity<?> getAnalyticsSummary(
            @RequestParam(defaultValue = "month") String range,
            Principal principal) {
        try {
            return ResponseEntity.ok(
                    bookingService.getAnalyticsSummary(principal.getName(), range));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Returns month-by-month booking + session counts for the last 6 months.
     * @param range  week | month (default) | quarter  (kept for API consistency)
     */
    @GetMapping("/analytics/trends")
    public ResponseEntity<?> getAnalyticsTrends(
            @RequestParam(defaultValue = "month") String range,
            Principal principal) {
        try {
            return ResponseEntity.ok(
                    bookingService.getAnalyticsTrends(principal.getName(), range));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // =========================================================================
    // PUBLIC ENDPOINTS  (no auth – must be added to SecurityConfig permitAll)
    // Pattern: /api/live-sessions/v1/booking/public/**
    // =========================================================================

    /**
     * Returns the published booking page for a given slug including all
     * active event types. Used by PublicBookingLandingPage.jsx on mount.
     *
     * FIX 3: returns HTTP 403 with a structured body when the page is not yet published.
     */
    @GetMapping("/public/{slug}")
    public ResponseEntity<?> getPublicPage(@PathVariable String slug) {
        try {
            PublicPageResponse page = bookingService.getPublicPage(slug);
            return ResponseEntity.ok(page);
        } catch (Exception e) {
            // FIX 3: distinguish NOT_PUBLISHED from true 404
            if ("NOT_PUBLISHED".equals(e.getMessage())) {
                return ResponseEntity.status(403).body(
                        new ErrorResponse("NOT_PUBLISHED",
                                "This booking page is not published yet"));
            }
            return ResponseEntity.status(404).body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Returns a list of available LocalTime slots for a specific event type
     * on a specific date.
     * @param date ISO date string "YYYY-MM-DD"
     */
    @GetMapping("/public/{slug}/{eventTypeId}/slots")
    public ResponseEntity<?> getAvailableSlots(
            @PathVariable String slug,
            @PathVariable Long eventTypeId,
            @RequestParam String date) {
        try {
            // Resolve trainerEmail from slug
            PublicPageResponse page = bookingService.getPublicPage(slug);
            String trainerEmail = page.getSettings().getTrainerEmail();

            LocalDate localDate = LocalDate.parse(date);
            return ResponseEntity.ok(
                    bookingService.getAvailableTimeSlots(trainerEmail, eventTypeId, localDate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Public booking submission.
     * trainerEmail and eventTypeId are resolved from the URL slug – the client
     * does not need to send them in the body.
     */
    @PostMapping("/public/{slug}/{eventTypeId}/book")
    public ResponseEntity<?> bookSlot(
            @PathVariable String slug,
            @PathVariable Long eventTypeId,
            @RequestBody BookedSlotRequest request) {
        try {
            // Resolve trainerEmail from slug server-side (security: never trust client)
            PublicPageResponse page = bookingService.getPublicPage(slug);
            String trainerEmail = page.getSettings().getTrainerEmail();

            request.setTrainerEmail(trainerEmail);
            request.setEventTypeId(eventTypeId);

            BookedSlotResponse response = bookingService.bookSlot(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Verifies a booking by its unique access token.
     * Used by the join page to look up session details before showing the
     * "Enter Session" button.
     */
    @GetMapping("/public/verify/{token}")
    public ResponseEntity<?> verifyToken(@PathVariable String token) {
        try {
            return bookingService.getBookingByToken(token)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(404)
                            .body(new ErrorResponse("Booking not found")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Lists all trainers with a published booking page, including their
     * active event types and specializations.
     * Public – no auth required.
     */
    @GetMapping("/public/trainers")
    public ResponseEntity<?> listPublishedTrainers() {
        try {
            return ResponseEntity.ok(bookingService.getPublishedTrainers());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}