package com.aigateway.web;

import com.aigateway.controller.AiController;
import com.aigateway.exception.GlobalExceptionHandler;
import com.aigateway.security.AuthenticatedUser;
import com.aigateway.service.ConversationService;
import com.aigateway.service.GeminiService;
import com.aigateway.service.RateLimitService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RateLimitInterceptorTest {

    @Test
    void rejectedRequestReturns429WithoutCallingGeminiOrSavingConversation() throws Exception {
        ConversationService conversationService = mock(ConversationService.class);
        GeminiService geminiService = mock(GeminiService.class);
        RateLimitService rateLimitService = new RateLimitService(1, 60);
        RateLimitInterceptor interceptor = new RateLimitInterceptor(rateLimitService);
        AiController controller = new AiController(conversationService, geminiService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(interceptor)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        AuthenticatedUser user = new AuthenticatedUser(7L, "demo");
        rateLimitService.tryAcquire(user.userId());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null);

        mockMvc.perform(post("/ai/chat")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"This must not reach Gemini\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("RATE_LIMIT_EXCEEDED"))
                .andExpect(jsonPath("$.path").value("/ai/chat"));

        verifyNoInteractions(geminiService);
        verifyNoInteractions(conversationService);
    }
}
