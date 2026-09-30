package com.aigateway.service;

import java.math.BigDecimal;
import java.util.List;

import com.aigateway.config.AiPricingProperties;
import com.aigateway.config.AiPricingProperties.ModelPricing;
import com.aigateway.dto.UsageResponse;
import com.aigateway.entity.Conversation;
import com.aigateway.repository.ConversationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsageServiceTest {

    @Test
    void keepsExistingUsageFieldsAndAddsCostFields() throws Exception {
        Conversation success = conversation("success", 100L, 50L, 200L);
        Conversation error = conversation("error", 50L, 25L, 400L);
        ConversationRepository repository = mock(ConversationRepository.class);
        when(repository.findAll()).thenReturn(List.of(success, error));

        UsageService usageService = new UsageService(repository, costEstimationService());
        UsageResponse response = usageService.getUsage();

        assertEquals(2L, response.getRequests());
        assertEquals(225L, response.getTokens());
        assertEquals(300.0, response.getAverageLatencyMs());
        assertEquals(0.5, response.getErrorRate());
        assertTrue(response.isCostEstimationAvailable());
        assertEquals(0, new BigDecimal("0.000300").compareTo(response.getEstimatedCostUsd()));

        JsonNode json = new ObjectMapper().valueToTree(response);
        assertTrue(json.has("requests"));
        assertTrue(json.has("tokens"));
        assertTrue(json.has("averageLatencyMs"));
        assertTrue(json.has("errorRate"));
        assertTrue(json.has("estimatedCostUsd"));
        assertTrue(json.has("costEstimationAvailable"));
    }

    private CostEstimationService costEstimationService() {
        AiPricingProperties properties = new AiPricingProperties();
        ModelPricing pricing = new ModelPricing();
        pricing.setProvider("gemini");
        pricing.setModel("test-model");
        pricing.setInputPerMillion(new BigDecimal("1.00"));
        pricing.setOutputPerMillion(new BigDecimal("2.00"));
        properties.setModels(List.of(pricing));
        return new CostEstimationService(properties);
    }

    private Conversation conversation(String status, Long inputTokens, Long outputTokens, Long latencyMs) {
        Conversation conversation = new Conversation();
        conversation.setStatus(status);
        conversation.setProvider("gemini");
        conversation.setModel("test-model");
        conversation.setInputTokens(inputTokens);
        conversation.setOutputTokens(outputTokens);
        conversation.setLatencyMs(latencyMs);
        return conversation;
    }
}
