//package com.lms.assessment.repository;
//
//import com.lms.assessment.model.CodingProblem;
//import com.lms.assessment.model.CodingProblem.Difficulty;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface CodingProblemRepository extends JpaRepository<CodingProblem, Long> {
//
//    // Used by CodingProblemService.getMyProblems()
//    List<CodingProblem> findByTrainerEmailOrderByCreatedAtDesc(String trainerEmail);
//
//    // Used for filtering by difficulty
//    List<CodingProblem> findByDifficultyAndActiveTrue(Difficulty difficulty);
//
//    // Used for trainer dashboard — all active problems
//    List<CodingProblem> findByActiveTrue();
//
//    // Search by title keyword (future search feature)
//    @Query("SELECT p FROM CodingProblem p WHERE " +
//           "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
//           "AND p.active = true")
//    List<CodingProblem> searchByTitleKeyword(@Param("keyword") String keyword);
//}

package com.lms.assessment.repository;

import com.lms.assessment.model.CodingProblem;
import com.lms.assessment.model.CodingProblem.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodingProblemRepository extends JpaRepository<CodingProblem, Long> {

    List<CodingProblem> findByTrainerEmailOrderByCreatedAtDesc(String trainerEmail);

    // ✅ isActive — matches your Boolean field name exactly
    List<CodingProblem> findByDifficultyAndIsActiveTrue(Difficulty difficulty);

    List<CodingProblem> findByIsActiveTrue();

    // ✅ JPQL uses Java field name 'isActive' not column name 'is_active'
    @Query("SELECT p FROM CodingProblem p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND p.isActive = true")
    List<CodingProblem> searchByTitleKeyword(@Param("keyword") String keyword);
}