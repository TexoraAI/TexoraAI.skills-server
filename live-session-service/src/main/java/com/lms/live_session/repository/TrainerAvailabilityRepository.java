package com.lms.live_session.repository;

import com.lms.live_session.entity.TrainerAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TrainerAvailabilityRepository extends JpaRepository<TrainerAvailability, Long> {

    List<TrainerAvailability> findByTrainerEmail(String trainerEmail);

    List<TrainerAvailability> findByTrainerEmailAndIsActiveTrue(String trainerEmail);

    List<TrainerAvailability> findByTrainerEmailAndDayOfWeek(String trainerEmail, Integer dayOfWeek);

    @Transactional
    void deleteByTrainerEmail(String trainerEmail);
}