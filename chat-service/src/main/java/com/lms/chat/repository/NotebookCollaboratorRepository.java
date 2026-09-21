package com.lms.chat.repository;

import com.lms.chat.entity.NotebookCollaborator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotebookCollaboratorRepository extends JpaRepository<NotebookCollaborator, Long> {
    boolean existsByNotebook_IdAndEmail(Long notebookId, String email);
    List<NotebookCollaborator> findByNotebook_Id(Long notebookId);
    List<NotebookCollaborator> findByEmail(String email);
}