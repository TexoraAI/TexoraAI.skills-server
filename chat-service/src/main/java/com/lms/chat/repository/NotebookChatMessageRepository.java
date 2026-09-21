package com.lms.chat.repository;

import com.lms.chat.entity.NotebookChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotebookChatMessageRepository extends JpaRepository<NotebookChatMessage, Long> {

    List<NotebookChatMessage> findByNotebook_IdOrderByCreatedAtAsc(Long notebookId);
}