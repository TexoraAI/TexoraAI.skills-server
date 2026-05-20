package com.lms.course.repository;

import com.lms.course.model.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {

    List<SchoolClass> findByBoardIdAndActiveTrueOrderByClassNumberAsc(Long boardId);

    Optional<SchoolClass> findByIdAndActiveTrue(Long id);
}