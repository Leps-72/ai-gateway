package com.aigateway.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.aigateway.config.AiPricingProperties;
import com.aigateway.config.AiPricingProperties.ModelPricing;
import com.aigateway.entity.Conversation;
import org.springframework.stereotype.Service;

@Service
public class CostEstimationService {

    private static final BigDecimal ONE_MILLION = BigDecimal.valueOf(1_000_000L);

    private final AiPricingProperties pricingProperties;

    public CostEstimationService(AiPricingProperties pricingProperties) {
        this.pricingProperties = pricingProperties;
    }

    public CostEstimate estimate(List<Conversation> conversations) {
        BigDecimal total = BigDecimal.ZERO;

        for (Conversation conversation : conversations) {
            if (!hasValidTokenMetrics(conversation)) {
                return CostEstimate.unavailable();
            }

            Optional<ModelPricing> pricing = pricingProperties.find(
                    conversation.getProvider(),
                    conversation.getModel()
            );
            if (pricing.isEmpty()) {
                return CostEstimate.unavailable();
            }

            BigDecimal inputCost = calculate(
                    conversation.getInputTokens(),
                    pricing.get().getInputPerMillion()
            );
            BigDecimal outputCost = calculate(
                    conversation.getOutputTokens(),
                    pricing.get().getOutputPerMillion()
            );
            total = total.add(inputCost).add(outputCost);
        }

        return CostEstimate.available(total);
    }

    private BigDecimal calculate(long tokens, BigDecimal pricePerMillion) {
        return BigDecimal.valueOf(tokens)
                .multiply(pricePerMillion)
                .divide(ONE_MILLION);
    }

    private boolean hasValidTokenMetrics(Conversation conversation) {
        return conversation.getInputTokens() != null
                && conversation.getOutputTokens() != null
                && conversation.getInputTokens() >= 0
                && conversation.getOutputTokens() >= 0;
    }

    public record CostEstimate(BigDecimal estimatedCostUsd, boolean available) {

        private static CostEstimate available(BigDecimal cost) {
            return new CostEstimate(cost, true);
        }

        private static CostEstimate unavailable() {
            return new CostEstimate(null, false);
        }
    }
}
