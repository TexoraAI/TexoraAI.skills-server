package com.lms.file.repository;

import com.lms.file.model.FeaturedSessionFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeaturedSessionFileRepository extends JpaRepository<FeaturedSessionFile, Long> {

    Optional<FeaturedSessionFile> findBySessionId(Long sessionId);
    Optional<FeaturedSessionFile> findByUrl(String url);
    List<FeaturedSessionFile> findBySessionIdIn(List<Long> sessionIds);
}