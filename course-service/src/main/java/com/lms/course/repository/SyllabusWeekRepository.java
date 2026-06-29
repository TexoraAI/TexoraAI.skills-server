package com.lms.course.repository;

import com.lms.course.model.SyllabusWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SyllabusWeekRepository extends JpaRepository<SyllabusWeek, Long> {

    List<SyllabusWeek> findByProgramIdOrderByWeekNumberAsc(Long programId);

    void deleteByProgramId(Long programId);
}