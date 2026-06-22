package com.lms.batch.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lms.batch.entity.OrgLimits;
public interface OrgLimitsRepository 
    extends JpaRepository<OrgLimits, String> {}