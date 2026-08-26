package com.lms.live_session.repository;

import com.lms.live_session.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByCreatorIdOrderByCreatedAtDesc(String creatorId);

    List<Schedule> findByCreatorIdAndDateBetween(String creatorId, LocalDate startDate, LocalDate endDate);

    List<Schedule> findByCreatorIdAndDate(String creatorId, LocalDate date);

    @Modifying
    @Transactional
    @Query("DELETE FROM Schedule s WHERE s.creatorId = :creatorId")
    void deleteByCreatorId(String creatorId);
}