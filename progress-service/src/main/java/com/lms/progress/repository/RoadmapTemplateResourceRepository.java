package com.lms.progress.repository;

import com.lms.progress.model.RoadmapTemplateResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoadmapTemplateResourceRepository extends JpaRepository<RoadmapTemplateResource, Long> {

    List<RoadmapTemplateResource> findByNodeId(Long nodeId);
    List<RoadmapTemplateResource> findByNodeIdIn(List<Long> nodeIds);

}
