package com.lms.progress.repository;

import com.lms.progress.model.OrgRoadmapResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrgRoadmapResourceRepository extends JpaRepository<OrgRoadmapResource, Long> {

    List<OrgRoadmapResource> findByNodeId(Long nodeId);
    List<OrgRoadmapResource> findByNodeIdIn(List<Long> nodeIds);
}
