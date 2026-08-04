package com.lms.live_session.controller;

import com.lms.live_session.dto.ChatMessageDTO;
import com.lms.live_session.entity.ChatMessage;
import com.lms.live_session.repository.ChatMessageRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;

    public ChatController(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @MessageMapping("/chat.send")
    @SendTo("/topic/messages")
    public ChatMessageDTO sendMessage(ChatMessageDTO message) {
        // Persist for AI context/history — but never let a DB hiccup block
        // the live broadcast, so this is best-effort and swallows errors.
        try {
            ChatMessage entity = new ChatMessage();
            entity.setSessionId(message.getSessionId());
            entity.setSenderId(message.getSenderId());
            entity.setSenderRole(message.getRole());
            entity.setMessage(message.getMessage());
            entity.setTimestamp(LocalDateTime.now());
            chatMessageRepository.save(entity);
        } catch (Exception e) {
            System.err.println("❌ Failed to persist chat message for session "
                + message.getSessionId() + ": " + e.getMessage());
        }
        return message;
    }
}