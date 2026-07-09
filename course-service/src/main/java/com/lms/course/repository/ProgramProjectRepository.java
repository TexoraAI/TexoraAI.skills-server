package com.lms.course.repository;

import com.lms.course.model.ProgramProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgramProjectRepository extends JpaRepository<ProgramProject, Long> {
    List<ProgramProject> findByProgramIdOrderByDisplayOrderAsc(Long programId);
    void deleteByProgramId(Long programId);
}