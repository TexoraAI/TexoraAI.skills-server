package com.lms.assessment.repository;

import com.lms.assessment.model.StudyPlanSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudyPlanSectionRepository extends JpaRepository<StudyPlanSection, Long> {
    List<StudyPlanSection> findByStudyPlanIdOrderByOrderIndexAsc(Long studyPlanId);
    void deleteByStudyPlanId(Long studyPlanId);
}