package com.lms.progress.repository;

import com.lms.progress.model.RoadmapTemplateNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoadmapTemplateNodeRepository extends JpaRepository<RoadmapTemplateNode, Long> {

    List<RoadmapTemplateNode> findByTemplateId(Long templateId);
}
