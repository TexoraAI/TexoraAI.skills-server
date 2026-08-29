//
//package com.lms.course.repository;
//import com.lms.course.model.FeaturedProgram;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//import java.util.List;
//import java.util.Optional;
//@Repository
//public interface FeaturedProgramRepository extends JpaRepository<FeaturedProgram, Long> {
//    List<FeaturedProgram> findAllByStatusOrderByDisplayOrderAsc(String status);
//    List<FeaturedProgram> findAllByCategoryIgnoreCaseAndStatus(String category, String status);
//    Optional<FeaturedProgram> findBySlug(String slug);
//    List<FeaturedProgram> findAllByOrderByDisplayOrderAsc();
//    long countByStatus(String status);
//    @Query("SELECT DISTINCT f.category FROM FeaturedProgram f")
//    List<String> findDistinctCategories();
//
//    // NEW: public/homepage endpoints must only surface Active + Published programs
//    List<FeaturedProgram> findAllByStatusAndPublishStatusOrderByDisplayOrderAsc(String status, String publishStatus);
//    List<FeaturedProgram> findAllByCategoryIgnoreCaseAndStatusAndPublishStatus(String category, String status, String publishStatus);
//}
package com.lms.course.repository;

import com.lms.course.dto.FeaturedProgramSummaryDTO;
import com.lms.course.model.FeaturedProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeaturedProgramRepository extends JpaRepository<FeaturedProgram, Long> {

    List<FeaturedProgram> findAllByStatusOrderByDisplayOrderAsc(String status);
    List<FeaturedProgram> findAllByCategoryIgnoreCaseAndStatus(String category, String status);
    Optional<FeaturedProgram> findBySlug(String slug);
    List<FeaturedProgram> findAllByOrderByDisplayOrderAsc();
    long countByStatus(String status);

    @Query("SELECT DISTINCT f.category FROM FeaturedProgram f")
    List<String> findDistinctCategories();

    List<FeaturedProgram> findAllByStatusAndPublishStatusOrderByDisplayOrderAsc(String status, String publishStatus);
    List<FeaturedProgram> findAllByCategoryIgnoreCaseAndStatusAndPublishStatus(String category, String status, String publishStatus);

    // ── NEW: lightweight query for homepage — no collections touched, zero N+1, single query ──
    @Query("SELECT new com.lms.course.dto.FeaturedProgramSummaryDTO(" +
           "f.id, f.title, f.category, f.instructorName, f.instructorRole, f.level, " +
           "f.durationWeeks, f.lessons, f.liveSessions, f.projects, f.studentsEnrolled, " +
           "f.rating, f.price, f.shortDescription, f.thumbnailUrl, f.bannerUrl, " +
           "f.instructorPhotoUrl, f.instructorLinkedIn, f.videoUrl, f.enrollmentUrl) " +
           "FROM FeaturedProgram f " +
           "WHERE f.status = :status AND f.publishStatus = :publishStatus " +
           "ORDER BY f.displayOrder ASC")
    List<FeaturedProgramSummaryDTO> findSummaryByStatusAndPublishStatus(
            @Param("status") String status,
            @Param("publishStatus") String publishStatus);
}