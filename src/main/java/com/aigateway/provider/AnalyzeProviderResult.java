package com.aigateway.provider;

public record AnalyzeProviderResult(
        String summary,
        String sentiment,
        String category,
        String priority,
        String responseJson,
        Long inputTokens,
        Long outputTokens
) {
}
