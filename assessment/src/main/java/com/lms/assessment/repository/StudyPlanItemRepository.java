package com.lms.assessment.repository;

import com.lms.assessment.model.StudyPlanItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudyPlanItemRepository extends JpaRepository<StudyPlanItem, Long> {
    List<StudyPlanItem> findBySectionIdOrderByOrderIndexAsc(Long sectionId);
    List<StudyPlanItem> findByProblemId(Long problemId);
    void deleteBySectionId(Long sectionId);
}