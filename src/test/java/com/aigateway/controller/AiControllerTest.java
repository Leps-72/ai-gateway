package com.aigateway.controller;

import com.aigateway.dto.AiRequest;
import com.aigateway.dto.AiChatResponse;
import com.aigateway.dto.AnalyzeRequest;
import com.aigateway.dto.AnalyzeResponse;
import com.aigateway.exception.AiProviderException;
import com.aigateway.provider.AiProviderResult;
import com.aigateway.provider.AnalyzeProviderResult;
import com.aigateway.security.AuthenticatedUser;
import com.aigateway.service.AiGatewayService;
import com.aigateway.service.ConversationService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiControllerTest {

    @Test
    void keepsChatResponseAndConversationMetricsContract() {
        ConversationService conversationService = mock(ConversationService.class);
        AiGatewayService aiGatewayService = mock(AiGatewayService.class);
        when(aiGatewayService.chat("hello"))
                .thenReturn(new AiProviderResult("answer", 2L, 3L));
        when(aiGatewayService.getProviderName()).thenReturn("gemini");
        when(aiGatewayService.getModelName()).thenReturn("test-model");
        AiController controller = new AiController(conversationService, aiGatewayService);

        AiRequest request = new AiRequest();
        request.setMessage("hello");
        AiChatResponse response = controller.chat(request, authentication()).getBody();

        assertNotNull(response);
        assertEquals("answer", response.getResponse());
        assertEquals("gemini", response.getProvider());
        assertEquals("test-model", response.getModel());
        assertEquals("success", response.getStatus());
        verify(conversationService).saveConversation(
                eq("hello"), eq("answer"), eq("gemini"), eq("test-model"),
                eq(2L), eq(3L), anyLong(), eq(7L), eq("success")
        );
    }

    @Test
    void keepsAnalyzeResponseAndConversationMetricsContract() {
        ConversationService conversationService = mock(ConversationService.class);
        AiGatewayService aiGatewayService = mock(AiGatewayService.class);
        String responseJson = "{\"summary\":\"slow\",\"sentiment\":\"negative\","
                + "\"category\":\"performance\",\"priority\":\"high\"}";
        when(aiGatewayService.analyze("slow checkout")).thenReturn(new AnalyzeProviderResult(
                "slow", "negative", "performance", "high", responseJson, 4L, 5L
        ));
        when(aiGatewayService.getProviderName()).thenReturn("gemini");
        when(aiGatewayService.getModelName()).thenReturn("test-model");
        AiController controller = new AiController(conversationService, aiGatewayService);

        AnalyzeRequest request = new AnalyzeRequest();
        request.setText("slow checkout");
        AnalyzeResponse response = controller.analyze(request, authentication()).getBody();

        assertNotNull(response);
        assertEquals("slow", response.getSummary());
        assertEquals("negative", response.getSentiment());
        assertEquals("performance", response.getCategory());
        assertEquals("high", response.getPriority());
        verify(conversationService).saveConversation(
                eq("slow checkout"), eq(responseJson), eq("gemini"), eq("test-model"),
                eq(4L), eq(5L), anyLong(), eq(7L), eq("success")
        );
    }

    @Test
    void storesOnlyOneConversationAfterProviderRetriesAreExhausted() {
        ConversationService conversationService = mock(ConversationService.class);
        AiGatewayService aiGatewayService = mock(AiGatewayService.class);
        when(aiGatewayService.chat("hello"))
                .thenThrow(new AiProviderException("provider unavailable"));
        when(aiGatewayService.getProviderName()).thenReturn("gemini");
        when(aiGatewayService.getModelName()).thenReturn("test-model");

        AiController controller = new AiController(conversationService, aiGatewayService);
        AiRequest request = new AiRequest();
        request.setMessage("hello");
        assertThrows(
                AiProviderException.class,
                () -> controller.chat(request, authentication())
        );

        verify(conversationService, times(1)).saveConversation(
                eq("hello"),
                eq(null),
                eq("gemini"),
                eq("test-model"),
                eq(null),
                eq(null),
                anyLong(),
                eq(7L),
                eq("error")
        );
    }

    private UsernamePasswordAuthenticationToken authentication() {
        return new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(7L, "demo"),
                null
        );
    }
}
