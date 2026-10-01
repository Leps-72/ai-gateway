package com.aigateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.HttpRetryOptions;
import com.google.genai.types.GenerateContentResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GeminiServiceConfigurationTest {

    @Test
    void springUsesTheDependencyInjectionConstructor() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            TestPropertyValues.of(
                    "ai.gemini.model=gemini-spring-test-model",
                    "ai.request-timeout-seconds=10",
                    "ai.max-attempts=3",
                    "ai.retry-backoff-ms=500"
            ).applyTo(context);
            context.getBeanFactory().registerSingleton("objectMapper", new ObjectMapper());
            context.register(GeminiService.class);
            context.refresh();

            GeminiService service = context.getBean(GeminiService.class);

            assertEquals("gemini-spring-test-model", service.getModelName());
            assertEquals("gemini", service.getProviderName());
        }
    }

    @Test
    void usesConfiguredModelForChatAndAnalyze() {
        GenerateContentResponse chatResponse = response("Chat response");
        GenerateContentResponse analyzeResponse = response(
                "{\"summary\":\"Slow checkout\",\"sentiment\":\"negative\","
                        + "\"category\":\"performance\",\"priority\":\"high\"}"
        );
        List<String> usedModels = new ArrayList<>();
        GeminiService service = new GeminiService(
                new ObjectMapper(),
                "gemini-configured-model",
                (model, content, config) -> {
                    usedModels.add(model);
                    return config == null ? chatResponse : analyzeResponse;
                }
        );

        service.chat("Hello");
        service.analyze("Checkout is slow");

        assertEquals("gemini-configured-model", service.getModelName());
        assertEquals("gemini", service.getProviderName());
        assertEquals(
                List.of("gemini-configured-model", "gemini-configured-model"),
                usedModels
        );
    }

    @Test
    void configuresTimeoutAndThreeTotalAttempts() {
        HttpOptions options = GeminiService.buildHttpOptions(10, 3, 500);
        HttpRetryOptions retry = options.retryOptions().orElseThrow();

        assertEquals(10_000, options.timeout().orElseThrow());
        assertEquals(3, retry.attempts().orElseThrow());
        assertEquals(List.of(408, 429, 500, 502, 503, 504), retry.httpStatusCodes().orElseThrow());
        assertEquals(0.5, retry.initialDelay().orElseThrow());
        assertEquals(1.0, retry.maxDelay().orElseThrow());
        assertEquals(2.0, retry.expBase().orElseThrow());
        assertEquals(0.0, retry.jitter().orElseThrow());
    }

    private GenerateContentResponse response(String text) {
        GenerateContentResponse response = mock(GenerateContentResponse.class);
        when(response.text()).thenReturn(text);
        when(response.usageMetadata()).thenReturn(Optional.empty());
        return response;
    }
}
