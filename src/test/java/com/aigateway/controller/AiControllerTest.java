package com.aigateway.controller;

import com.aigateway.dto.AiRequest;
import com.aigateway.exception.AiProviderException;
import com.aigateway.security.AuthenticatedUser;
import com.aigateway.service.ConversationService;
import com.aigateway.service.GeminiService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiControllerTest {

    @Test
    void storesOnlyOneConversationAfterProviderRetriesAreExhausted() {
        ConversationService conversationService = mock(ConversationService.class);
        GeminiService geminiService = mock(GeminiService.class);
        when(geminiService.generateResponse("hello"))
                .thenThrow(new AiProviderException("provider unavailable"));
        when(geminiService.getModel()).thenReturn("test-model");

        AiController controller = new AiController(conversationService, geminiService);
        AiRequest request = new AiRequest();
        request.setMessage("hello");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        new AuthenticatedUser(7L, "demo"),
                        null
                );

        assertThrows(
                AiProviderException.class,
                () -> controller.chat(request, authentication)
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
}
