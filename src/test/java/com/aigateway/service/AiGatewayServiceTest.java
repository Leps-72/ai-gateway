package com.aigateway.service;

import com.aigateway.provider.AiProvider;
import com.aigateway.provider.AiProviderResult;
import com.aigateway.provider.AnalyzeProviderResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiGatewayServiceTest {

    @Test
    void delegatesChatAndAnalyzeThroughProviderAbstraction() {
        AiProvider provider = mock(AiProvider.class);
        AiProviderResult chatResult = new AiProviderResult("answer", 2L, 3L);
        AnalyzeProviderResult analyzeResult = new AnalyzeProviderResult(
                "summary",
                "negative",
                "performance",
                "high",
                "{\"summary\":\"summary\",\"sentiment\":\"negative\","
                        + "\"category\":\"performance\",\"priority\":\"high\"}",
                4L,
                5L
        );
        when(provider.chat("hello")).thenReturn(chatResult);
        when(provider.analyze("slow checkout")).thenReturn(analyzeResult);
        when(provider.getProviderName()).thenReturn("gemini");
        when(provider.getModelName()).thenReturn("test-model");

        AiGatewayService service = new AiGatewayService(provider);

        assertSame(chatResult, service.chat("hello"));
        assertSame(analyzeResult, service.analyze("slow checkout"));
        assertEquals("gemini", service.getProviderName());
        assertEquals("test-model", service.getModelName());
        verify(provider).chat("hello");
        verify(provider).analyze("slow checkout");
    }
}
