package com.lms.progress.repository;

import com.lms.progress.model.NodeStatus;
import com.lms.progress.model.UserRoadmapProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRoadmapProgressRepository extends JpaRepository<UserRoadmapProgress, Long> {

    List<UserRoadmapProgress> findByUserIdAndOrgRoadmapId(Long userId, Long orgRoadmapId);

    Optional<UserRoadmapProgress> findByUserIdAndNodeId(Long userId, Long nodeId);

    List<UserRoadmapProgress> findByOrgRoadmapIdAndUserIdIn(Long orgRoadmapId, List<Long> userIds);

    
    
    List<UserRoadmapProgress> findByOrgRoadmapId(Long orgRoadmapId);
}
