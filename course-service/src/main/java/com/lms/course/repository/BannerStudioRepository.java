//package com.lms.course.repository;
//
//import com.lms.course.model.BannerStudio;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.time.LocalDate;
//import java.util.List;
//
//public interface BannerStudioRepository extends JpaRepository<BannerStudio, Long> {
//
//    List<BannerStudio> findByStatusOrderByUpdatedAtDesc(BannerStudio.BannerStatus status);
//
//    List<BannerStudio> findAllByOrderByUpdatedAtDesc();
//
//    @Query("SELECT b FROM BannerStudio b WHERE " +
//            "(:status IS NULL OR b.status = :status) AND " +
//            "(:search IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
//            "ORDER BY b.updatedAt DESC")
//    List<BannerStudio> search(@Param("status") BannerStudio.BannerStatus status,
//                               @Param("search") String search);
//
//    @Query("SELECT b FROM BannerStudio b WHERE b.status = 'SCHEDULED' AND b.startDate <= :today")
//    List<BannerStudio> findScheduledBannersDueToGoLive(@Param("today") LocalDate today);
//
//    long countByStatus(BannerStudio.BannerStatus status);
//}

package com.lms.course.repository;

import com.lms.course.model.BannerStudio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BannerStudioRepository extends JpaRepository<BannerStudio, Long> {

    List<BannerStudio> findByStatusOrderByUpdatedAtDesc(BannerStudio.BannerStatus status);

    List<BannerStudio> findAllByOrderByUpdatedAtDesc();

    @Query("SELECT b FROM BannerStudio b WHERE " +
            "(:status IS NULL OR b.status = :status) AND " +
            "(:search IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
            "ORDER BY b.updatedAt DESC")
    List<BannerStudio> search(@Param("status") BannerStudio.BannerStatus status,
                               @Param("search") String search);

    @Query("SELECT b FROM BannerStudio b WHERE b.status = 'SCHEDULED' AND b.startDate <= :today")
    List<BannerStudio> findScheduledBannersDueToGoLive(@Param("today") LocalDate today);

    long countByStatus(BannerStudio.BannerStatus status);
}