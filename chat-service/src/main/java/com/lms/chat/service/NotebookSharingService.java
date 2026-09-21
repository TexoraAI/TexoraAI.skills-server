package com.lms.chat.service;

import com.lms.chat.entity.Notebook;
import com.lms.chat.entity.NotebookCollaborator;
import com.lms.chat.repository.NotebookCollaboratorRepository;
import com.lms.chat.repository.NotebookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotebookSharingService {

    private final NotebookRepository notebookRepository;
    private final NotebookCollaboratorRepository collaboratorRepository;

    public NotebookSharingService(NotebookRepository notebookRepository,
                                   NotebookCollaboratorRepository collaboratorRepository) {
        this.notebookRepository = notebookRepository;
        this.collaboratorRepository = collaboratorRepository;
    }

    @Transactional
    public void share(Long notebookId, String email, String ownerEmail) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("An email address is required to share this notebook");
        }
        String normalizedEmail = email.trim().toLowerCase();

        // Ownership check — only the owner can share their notebook
        Notebook nb = notebookRepository
                .findByIdAndStudentEmail(notebookId, ownerEmail)
                .orElseThrow(() -> new RuntimeException("Notebook not found"));

        if (normalizedEmail.equalsIgnoreCase(ownerEmail)) {
            throw new IllegalArgumentException("You can't share a notebook with your own email");
        }

        if (collaboratorRepository.existsByNotebook_IdAndEmail(nb.getId(), normalizedEmail)) {
            throw new IllegalStateException("This notebook is already shared with " + normalizedEmail);
        }

        NotebookCollaborator collaborator = new NotebookCollaborator();
        collaborator.setNotebook(nb);
        collaborator.setEmail(normalizedEmail);
        collaboratorRepository.save(collaborator);
    }
}