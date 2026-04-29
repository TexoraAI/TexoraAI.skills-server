// NotebookRepository.java
package com.lms.chat.repository;

import com.lms.chat.entity.Notebook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotebookRepository extends JpaRepository<Notebook, Long> {
    List<Notebook> findByStudentEmailOrderByCreatedAtAsc(String studentEmail);
    Optional<Notebook> findByIdAndStudentEmail(Long id, String studentEmail);
}