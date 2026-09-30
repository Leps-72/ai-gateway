package com.aigateway.controller;

import java.util.List;

import com.aigateway.entity.Conversation;
import com.aigateway.service.ConversationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping("/conversations")
    public List<Conversation> getConversations() {
        return conversationService.getAllConversations();
    }
}
