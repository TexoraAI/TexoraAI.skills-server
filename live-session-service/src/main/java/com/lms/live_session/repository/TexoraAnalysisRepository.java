package com.lms.live_session.repository;

import com.lms.live_session.entity.TexoraAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TexoraAnalysisRepository extends JpaRepository<TexoraAnalysis, Long> {
    Optional<TexoraAnalysis> findByTexoraMeetingId(String texoraMeetingId);
}