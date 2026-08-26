package com.lms.live_session.repository;

import com.lms.live_session.entity.AvailabilitySlot;
import com.lms.live_session.entity.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

    List<AvailabilitySlot> findByCreatorIdOrderByDayOfWeekAsc(String creatorId);

    List<AvailabilitySlot> findByCreatorIdAndDayOfWeek(String creatorId, DayOfWeek dayOfWeek);

    List<AvailabilitySlot> findByCreatorIdAndIsRecurringTrue(String creatorId);

    void deleteByCreatorId(String creatorId);
}