package com.lms.progress.repository;

import com.lms.progress.model.RoadmapUpgradedVideoCache;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoadmapUpgradedVideoCacheRepository extends JpaRepository<RoadmapUpgradedVideoCache, Long> {
    Optional<RoadmapUpgradedVideoCache> findBySearchKey(String searchKey);
}