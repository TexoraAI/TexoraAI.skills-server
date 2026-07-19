//package com.lms.video.repository;
//
//import com.lms.video.model.WatchNow;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//
//public interface WatchNowRepository extends JpaRepository<WatchNow, Long> {
//
//    /**
//     * Find all WatchNow entries linked to a given courseId.
//     * Used by the delete-by-course endpoint.
//     */
//    List<WatchNow> findByCourseId(Long courseId);
//    long countByStatus(String status);
//}

package com.lms.video.repository;

import com.lms.video.model.WatchNow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WatchNowRepository extends JpaRepository<WatchNow, Long> {

    // public – published only, in display order
    List<WatchNow> findByStatusOrderBySortOrderAsc(String status);

    // admin – all statuses, in display order
    List<WatchNow> findAllByOrderBySortOrderAsc();
    
    long countByStatus(String status);
    long countByVideoFileNameIsNotNull();
    long countByExternalVideoUrlIsNotNull();
}