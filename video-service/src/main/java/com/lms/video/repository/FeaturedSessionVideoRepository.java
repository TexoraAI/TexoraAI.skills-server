package com.lms.video.repository;

import com.lms.video.model.FeaturedSessionVideo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeaturedSessionVideoRepository extends JpaRepository<FeaturedSessionVideo, Long> {

    Optional<FeaturedSessionVideo> findBySessionId(Long sessionId);

    Optional<FeaturedSessionVideo> findByUrl(String url);

    List<FeaturedSessionVideo> findBySessionIdIn(List<Long> sessionIds);
}