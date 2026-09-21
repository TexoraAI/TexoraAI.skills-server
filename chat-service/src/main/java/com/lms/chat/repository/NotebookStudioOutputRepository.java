package com.lms.chat.repository;

import com.lms.chat.entity.NotebookStudioOutput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotebookStudioOutputRepository extends JpaRepository<NotebookStudioOutput, Long> {
    List<NotebookStudioOutput> findByNotebook_IdOrderByCreatedAtDesc(Long notebookId);
    Optional<NotebookStudioOutput> findByIdAndNotebook_StudentEmail(Long id, String studentEmail);
}