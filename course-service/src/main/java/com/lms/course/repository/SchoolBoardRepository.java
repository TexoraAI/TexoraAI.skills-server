// ─────────────────────────────────────────────────────────────────────────────
// FILE 1: SchoolBoardRepository.java
// package com.lms.course.repository;
// ─────────────────────────────────────────────────────────────────────────────

package com.lms.course.repository;

import com.lms.course.model.SchoolBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolBoardRepository extends JpaRepository<SchoolBoard, Long> {

    List<SchoolBoard> findByActiveTrueOrderByDisplayOrderAsc();

    Optional<SchoolBoard> findByIdAndActiveTrue(Long id);

    Optional<SchoolBoard> findByBoardKeyAndActiveTrue(String boardKey);
}