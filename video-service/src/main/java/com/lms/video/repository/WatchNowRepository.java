package com.lms.video.repository;

import com.lms.video.model.WatchNow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WatchNowRepository extends JpaRepository<WatchNow, Long> {

    /**
     * Find all WatchNow entries linked to a given courseId.
     * Used by the delete-by-course endpoint.
     */
    List<WatchNow> findByCourseId(Long courseId);
    long countByStatus(String status);
}