package com.lms.live_session.repository;

import com.lms.live_session.entity.BookedSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookedSlotRepository extends JpaRepository<BookedSlot, Long> {

    List<BookedSlot> findByTrainerEmailOrderByBookedDateDesc(String trainerEmail);

    List<BookedSlot> findByTrainerEmailAndStatus(String trainerEmail, String status);

    List<BookedSlot> findByTrainerEmailAndBookedDate(String trainerEmail, LocalDate bookedDate);

    List<BookedSlot> findByEventTypeIdAndBookedDateAndStatusNot(
            Long eventTypeId, LocalDate bookedDate, String status);

    Optional<BookedSlot> findByUniqueAccessToken(String uniqueAccessToken);

    List<BookedSlot> findByBookerEmail(String bookerEmail);

    List<BookedSlot> findByBookerUserId(Long bookerUserId);

    boolean existsByTrainerEmailAndBookedDateAndStartTimeAndStatusNot(
            String trainerEmail, LocalDate bookedDate, LocalTime startTime, String status);

    // NEW – used by analytics trends endpoint
    List<BookedSlot> findByTrainerEmailAndBookedDateBetween(
            String trainerEmail, LocalDate start, LocalDate end);
}