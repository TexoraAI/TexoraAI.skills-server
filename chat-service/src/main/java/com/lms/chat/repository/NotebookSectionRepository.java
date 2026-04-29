// NotebookSectionRepository.java
package com.lms.chat.repository;

import com.lms.chat.entity.NotebookSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface NotebookSectionRepository extends JpaRepository<NotebookSection, Long> {
    Optional<NotebookSection> findByIdAndNotebook_StudentEmail(Long id, String studentEmail);
    int countByNotebook_Id(Long notebookId);
}