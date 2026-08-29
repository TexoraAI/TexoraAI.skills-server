package com.lms.progress.repository;

import com.lms.progress.model.RoadmapUpgradedMentorMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Chat history for a syllabus is a fundamentally different aggregate root
 * than the syllabus/module/resource tree, and JPA repositories are always
 * one-per-entity-root - this is the one deliberate exception to the
 * single-repository rule, per spec section 4.
 */
public interface RoadmapUpgradedMentorRepository extends JpaRepository<RoadmapUpgradedMentorMessage, Long> {

    List<RoadmapUpgradedMentorMessage> findBySyllabusIdOrderBySentAtAsc(Long syllabusId);
}
