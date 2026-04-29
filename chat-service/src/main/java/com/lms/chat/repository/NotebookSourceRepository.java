// com.lms.chat.repository.NotebookSourceRepository.java
package com.lms.chat.repository;

import com.lms.chat.entity.NotebookSource;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NotebookSourceRepository extends JpaRepository<NotebookSource, Long> {
    Optional<NotebookSource> findByIdAndNotebook_StudentEmail(Long id, String email);
}