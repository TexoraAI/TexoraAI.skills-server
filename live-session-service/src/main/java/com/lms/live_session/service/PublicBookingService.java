


package com.lms.live_session.service;

import com.lms.live_session.entity.LiveSession;
import com.lms.live_session.entity.PublicSessionBooking;
import com.lms.live_session.event.SessionNotificationEvent;
import com.lms.live_session.kafka.NotificationProducer;
import com.lms.live_session.repository.LiveSessionRepository;
import com.lms.live_session.repository.PublicBookingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PublicBookingService {

    private final PublicBookingRepository bookingRepository;
    private final LiveSessionRepository sessionRepository;
    private final NotificationProducer notificationProducer;
    private final UrlBuilderService urlBuilderService;

    public PublicBookingService(PublicBookingRepository bookingRepository,
                                LiveSessionRepository sessionRepository,
                                NotificationProducer notificationProducer,
                                UrlBuilderService urlBuilderService) {
        this.bookingRepository   = bookingRepository;
        this.sessionRepository   = sessionRepository;
        this.notificationProducer = notificationProducer;
        this.urlBuilderService   = urlBuilderService;
    }

    // ✅ Create public booking — now accepts 4 extra fields
    public PublicSessionBooking bookSession(Long sessionId,
                                            String fullName, String email,
                                            String phoneNumber, String country,
                                            Boolean gdprConsent,
                                            String topicsOfInterest, String jobRole,
                                            String howDidYouHear, String learningGoal) {

        LiveSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (bookingRepository.existsBySessionIdAndEmail(sessionId, email)) {
            throw new RuntimeException("Email already booked for this session");
        }

        // ✅ Use full constructor with new fields
        PublicSessionBooking booking = new PublicSessionBooking(
                sessionId, fullName, email, phoneNumber, country, gdprConsent,
                topicsOfInterest, jobRole, howDidYouHear, learningGoal
        );

        booking.setUniqueAccessToken(UUID.randomUUID().toString());
        PublicSessionBooking saved = bookingRepository.save(booking);

        sendBookingConfirmation(saved, session);

        System.out.println("✅ Public booking created: " + saved.getId() + " for " + email);
        return saved;
    }

    // ✅ Send booking confirmation email via Kafka
    private void sendBookingConfirmation(PublicSessionBooking booking, LiveSession session) {
        try {
            String joinLink = urlBuilderService.generatePublicJoinLink(
                    booking.getUniqueAccessToken());

            SessionNotificationEvent event = new SessionNotificationEvent(
                    session.getId(),
                    session.getTrainerEmail(),
                    session.getBatchId(),
                    session.getTitle(),
                    session.getScheduledDate().toString(),
                    session.getScheduledTime().toString(),
                    session.getDuration(),
                    "PUBLIC_BOOKING_CONFIRMATION",
                    booking.getEmail(),
                    booking.getFullName(),
                    "PUBLIC_USER",
                    joinLink
            );

            notificationProducer.sendPublicUserBooking(event);
            System.out.println("✅ Booking confirmation sent to: " + booking.getEmail());

        } catch (Exception e) {
            System.err.println("❌ Failed to send booking confirmation: " + e.getMessage());
        }
    }

    // ── Other methods unchanged ───────────────────────────────────────

    public Optional<PublicSessionBooking> getBookingByToken(String token) {
        return bookingRepository.findByUniqueAccessToken(token);
    }

    public List<PublicSessionBooking> getSessionBookings(Long sessionId) {
        return bookingRepository.findBySessionId(sessionId);
    }

    public PublicSessionBooking markAsJoined(Long bookingId) {
        PublicSessionBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setJoinedAt(java.time.LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    public PublicSessionBooking markAsLeft(Long bookingId) {
        PublicSessionBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setLeftAt(java.time.LocalDateTime.now());
        booking.setBookingStatus("ATTENDED");
        return bookingRepository.save(booking);
    }

    public PublicSessionBooking cancelBooking(Long bookingId) {
        PublicSessionBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setBookingStatus("CANCELLED");
        return bookingRepository.save(booking);
    }

    public Long getBookingDuration(Long bookingId) {
        PublicSessionBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getJoinedAt() == null || booking.getLeftAt() == null) {
            return 0L;
        }

        return java.time.temporal.ChronoUnit.MINUTES.between(
                booking.getJoinedAt(), booking.getLeftAt());
    }
}