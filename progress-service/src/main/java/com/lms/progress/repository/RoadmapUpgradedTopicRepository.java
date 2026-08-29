package com.lms.progress.repository;

import com.lms.progress.model.RoadmapUpgradedTopic;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Not used by any endpoint directly today (topics are always reached via
 * the syllabus aggregate root, same pattern as RoadmapUpgradedResource) -
 * added for symmetry/future use, not a new aggregate root.
 */
public interface RoadmapUpgradedTopicRepository extends JpaRepository<RoadmapUpgradedTopic, Long> {
}