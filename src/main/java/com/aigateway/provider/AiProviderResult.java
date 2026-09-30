package com.aigateway.provider;

public record AiProviderResult(
        String response,
        Long inputTokens,
        Long outputTokens
) {
}
