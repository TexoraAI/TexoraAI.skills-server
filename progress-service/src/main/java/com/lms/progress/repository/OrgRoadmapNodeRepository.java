package com.lms.progress.repository;

import com.lms.progress.model.OrgRoadmapNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrgRoadmapNodeRepository extends JpaRepository<OrgRoadmapNode, Long> {

    List<OrgRoadmapNode> findByOrgRoadmapId(Long orgRoadmapId);
}
