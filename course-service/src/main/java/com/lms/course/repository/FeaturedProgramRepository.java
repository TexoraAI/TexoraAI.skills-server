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

    // ── lightweight query for homepage — no collections touched, zero N+1, single query.
    // Extended to include the badge flags + discount fields so the homepage can render
    // real "Featured"/"Trending"/"Bestseller" badges and strike-through pricing instead
    // of guessing isBestseller from rating on the client. ──
    @Query("SELECT new com.lms.course.dto.FeaturedProgramSummaryDTO(" +
           "f.id, f.title, f.category, f.instructorName, f.instructorRole, f.level, " +
           "f.durationWeeks, f.lessons, f.liveSessions, f.projects, f.studentsEnrolled, " +
           "f.rating, f.price, f.shortDescription, f.thumbnailUrl, f.bannerUrl, " +
           "f.instructorPhotoUrl, f.instructorLinkedIn, f.videoUrl, f.enrollmentUrl, " +
           "f.isFeatured, f.isTrending, f.isBestseller, f.isPopular, f.isRecommended, f.isComingSoon, " +
           "f.originalPrice, f.discountPercent) " +
           "FROM FeaturedProgram f " +
           "WHERE f.status = :status AND f.publishStatus = :publishStatus " +
           "ORDER BY f.displayOrder ASC")
    List<FeaturedProgramSummaryDTO> findSummaryByStatusAndPublishStatus(
            @Param("status") String status,
            @Param("publishStatus") String publishStatus);
}