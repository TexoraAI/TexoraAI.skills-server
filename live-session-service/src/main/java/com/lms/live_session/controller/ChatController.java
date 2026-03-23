package com.lms.live_session.controller;

import com.lms.live_session.dto.ChatMessageDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @MessageMapping("/chat.send")
    @SendTo("/topic/messages")
    public ChatMessageDTO sendMessage(ChatMessageDTO message) {

        return message;
    }
}