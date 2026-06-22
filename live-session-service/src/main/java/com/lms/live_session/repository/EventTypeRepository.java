package com.lms.live_session.repository;

import com.lms.live_session.entity.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventTypeRepository extends JpaRepository<EventType, Long> {

    List<EventType> findByTrainerEmail(String trainerEmail);

    List<EventType> findByTrainerEmailAndIsActiveTrue(String trainerEmail);

    Optional<EventType> findByTrainerEmailAndId(String trainerEmail, Long id);

    Optional<EventType> findBySlug(String slug);

    boolean existsBySlugAndTrainerEmail(String slug, String trainerEmail);
}