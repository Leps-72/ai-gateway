package com.aigateway.controller;

import java.util.List;

import com.aigateway.entity.Conversation;
import com.aigateway.security.AuthenticatedUser;
import com.aigateway.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Conversations", description = "Stored AI conversations")
@SecurityRequirement(name = "bearerAuth")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping("/conversations")
    @Operation(summary = "List stored conversations")
    public List<Conversation> getConversations(Authentication authentication) {
        Long userId = ((AuthenticatedUser) authentication.getPrincipal()).userId();
        return conversationService.getConversationsByUserId(userId);
    }
}
