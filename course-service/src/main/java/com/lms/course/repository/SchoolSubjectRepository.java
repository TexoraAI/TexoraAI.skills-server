package com.lms.course.repository;

import com.lms.course.model.SchoolSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolSubjectRepository extends JpaRepository<SchoolSubject, Long> {

    // get all subjects for a class (no stream filter)
    List<SchoolSubject> findBySchoolClassIdAndActiveTrueOrderByDisplayOrderAsc(Long schoolClassId);

    // get subjects for a class filtered by specific stream
    List<SchoolSubject> findBySchoolClassIdAndStreamAndActiveTrueOrderByDisplayOrderAsc(Long schoolClassId, String stream);

    Optional<SchoolSubject> findByIdAndActiveTrue(Long id);
}