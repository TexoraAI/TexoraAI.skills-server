package com.lms.live_session.repository;

import com.lms.live_session.entity.PublicSessionBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PublicBookingRepository extends JpaRepository<PublicSessionBooking, Long> {

    // ✅ Find booking by session and email
    Optional<PublicSessionBooking> findBySessionIdAndEmail(Long sessionId, String email);

    // ✅ Find all bookings for a session
    List<PublicSessionBooking> findBySessionId(Long sessionId);

    // ✅ Find by unique access token
    Optional<PublicSessionBooking> findByUniqueAccessToken(String token);

    // ✅ Find all active bookings for a session
    List<PublicSessionBooking> findBySessionIdAndBookingStatus(Long sessionId, String status);

    // ✅ Check if email already booked this session
    boolean existsBySessionIdAndEmail(Long sessionId, String email);
}