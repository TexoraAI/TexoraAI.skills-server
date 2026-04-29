// NotebookPageRepository.java
package com.lms.chat.repository;

import com.lms.chat.entity.NotebookPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface NotebookPageRepository extends JpaRepository<NotebookPage, Long> {
    Optional<NotebookPage> findByIdAndSection_Notebook_StudentEmail(Long id, String studentEmail);
    int countBySection_Id(Long sectionId);
}