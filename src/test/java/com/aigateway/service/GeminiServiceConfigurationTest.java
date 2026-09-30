package com.aigateway.service;

import com.google.genai.types.HttpOptions;
import com.google.genai.types.HttpRetryOptions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GeminiServiceConfigurationTest {

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
}
