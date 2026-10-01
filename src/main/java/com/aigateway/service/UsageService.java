package com.aigateway.service;

import java.util.List;

import com.aigateway.dto.UsageResponse;
import com.aigateway.entity.Conversation;
import com.aigateway.repository.ConversationRepository;
import org.springframework.stereotype.Service;

@Service
public class UsageService {

    private final ConversationRepository conversationRepository;
    private final CostEstimationService costEstimationService;

    public UsageService(
            ConversationRepository conversationRepository,
            CostEstimationService costEstimationService
    ) {
        this.conversationRepository = conversationRepository;
        this.costEstimationService = costEstimationService;
    }

    public UsageResponse getUsage(Long userId) {
        List<Conversation> conversations = conversationRepository.findByUserId(userId);
        long requests = conversations.size();

        long tokens = conversations.stream()
                .mapToLong(conversation -> valueOrZero(conversation.getInputTokens())
                        + valueOrZero(conversation.getOutputTokens()))
                .sum();

        double averageLatencyMs = conversations.stream()
                .map(Conversation::getLatencyMs)
                .filter(latency -> latency != null)
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        long errors = conversations.stream()
                .filter(conversation -> "error".equals(conversation.getStatus()))
                .count();
        double errorRate = requests == 0 ? 0.0 : (double) errors / requests;

        CostEstimationService.CostEstimate costEstimate = costEstimationService.estimate(conversations);

        return new UsageResponse(
                requests,
                tokens,
                averageLatencyMs,
                errorRate,
                costEstimate.estimatedCostUsd(),
                costEstimate.available()
        );
    }

    private long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }
}
