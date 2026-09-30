package com.aigateway.service;

import java.math.BigDecimal;
import java.util.List;

import com.aigateway.config.AiPricingProperties;
import com.aigateway.config.AiPricingProperties.ModelPricing;
import com.aigateway.entity.Conversation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CostEstimationServiceTest {

    @Test
    void calculatesInputCost() {
        CostEstimationService.CostEstimate estimate = service("2.00", "0.00")
                .estimate(List.of(conversation(250_000L, 0L, "test-model")));

        assertTrue(estimate.available());
        assertDecimalEquals("0.500000", estimate.estimatedCostUsd());
    }

    @Test
    void calculatesOutputCost() {
        CostEstimationService.CostEstimate estimate = service("0.00", "4.00")
                .estimate(List.of(conversation(0L, 500_000L, "test-model")));

        assertTrue(estimate.available());
        assertDecimalEquals("2.000000", estimate.estimatedCostUsd());
    }

    @Test
    void calculatesTotalCostAcrossRecords() {
        CostEstimationService.CostEstimate estimate = service("2.00", "4.00")
                .estimate(List.of(
                        conversation(250_000L, 500_000L, "test-model"),
                        conversation(500_000L, 250_000L, "test-model")
                ));

        assertTrue(estimate.available());
        assertDecimalEquals("4.500000", estimate.estimatedCostUsd());
    }

    @Test
    void preservesBigDecimalPrecision() {
        CostEstimationService.CostEstimate estimate = service("0.123456789", "0")
                .estimate(List.of(conversation(1L, 0L, "test-model")));

        assertTrue(estimate.available());
        assertEquals(new BigDecimal("0.000000123456789"), estimate.estimatedCostUsd());
    }

    @Test
    void returnsUnavailableWhenTokenMetricsAreNull() {
        CostEstimationService.CostEstimate estimate = service("2.00", "4.00")
                .estimate(List.of(conversation(null, 10L, "test-model")));

        assertFalse(estimate.available());
        assertNull(estimate.estimatedCostUsd());
    }

    @Test
    void returnsUnavailableWhenModelHasNoPricing() {
        CostEstimationService.CostEstimate estimate = service("2.00", "4.00")
                .estimate(List.of(conversation(10L, 20L, "unknown-model")));

        assertFalse(estimate.available());
        assertNull(estimate.estimatedCostUsd());
    }

    private CostEstimationService service(String inputPrice, String outputPrice) {
        AiPricingProperties properties = new AiPricingProperties();
        ModelPricing pricing = new ModelPricing();
        pricing.setProvider("gemini");
        pricing.setModel("test-model");
        pricing.setInputPerMillion(new BigDecimal(inputPrice));
        pricing.setOutputPerMillion(new BigDecimal(outputPrice));
        properties.setModels(List.of(pricing));
        return new CostEstimationService(properties);
    }

    private Conversation conversation(Long inputTokens, Long outputTokens, String model) {
        Conversation conversation = new Conversation();
        conversation.setProvider("gemini");
        conversation.setModel(model);
        conversation.setInputTokens(inputTokens);
        conversation.setOutputTokens(outputTokens);
        return conversation;
    }

    private void assertDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
