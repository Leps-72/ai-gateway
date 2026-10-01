package com.aigateway.service;

import java.time.LocalDateTime;
import java.util.List;

import com.aigateway.entity.Conversation;
import com.aigateway.repository.ConversationRepository;
import org.springframework.stereotype.Service;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;

    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    public Conversation saveConversation(
            String message,
            String response,
            String provider,
            String model,
            Long inputTokens,
            Long outputTokens,
            Long latencyMs,
            Long userId,
            String status
    ) {
        Conversation conversation = new Conversation(
                message,
                response,
                provider,
                model,
                inputTokens,
                outputTokens,
                latencyMs,
                userId,
                status,
                LocalDateTime.now()
        );
        return conversationRepository.save(conversation);
    }

    public List<Conversation> getConversationsByUserId(Long userId) {
        return conversationRepository.findByUserId(userId);
    }
}
