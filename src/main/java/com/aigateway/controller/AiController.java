package com.aigateway.controller;

import com.aigateway.dto.AiRequest;
import com.aigateway.dto.AiChatResponse;
import com.aigateway.dto.AnalyzeRequest;
import com.aigateway.dto.AnalyzeResponse;
import com.aigateway.service.ConversationService;
import com.aigateway.service.GeminiService;
import com.aigateway.service.GeminiService.AnalyzeResult;
import com.aigateway.service.GeminiService.GeminiResult;
import com.aigateway.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final ConversationService conversationService;
    private final GeminiService geminiService;

    public AiController(ConversationService conversationService, GeminiService geminiService) {
        this.conversationService = conversationService;
        this.geminiService = geminiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(
            @RequestBody AiRequest request,
            Authentication authentication
    ) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new IllegalArgumentException("Message must not be blank.");
        }

        long startTime = System.nanoTime();
        Long userId = ((AuthenticatedUser) authentication.getPrincipal()).userId();
        GeminiResult result;

        try {
            result = geminiService.generateResponse(request.getMessage());
        } catch (RuntimeException exception) {
            saveFailedConversation(request.getMessage(), userId, startTime);
            throw exception;
        }

        long latencyMs = elapsedMilliseconds(startTime);
        conversationService.saveConversation(
                request.getMessage(),
                result.response(),
                "gemini",
                geminiService.getModel(),
                result.inputTokens(),
                result.outputTokens(),
                latencyMs,
                userId,
                "success"
        );

        AiChatResponse response = new AiChatResponse(
                result.response(),
                "gemini",
                geminiService.getModel(),
                "success"
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeResponse> analyze(
            @RequestBody AnalyzeRequest request,
            Authentication authentication
    ) {
        if (request.getText() == null || request.getText().isBlank()) {
            throw new IllegalArgumentException("Text must not be blank.");
        }

        long startTime = System.nanoTime();
        Long userId = ((AuthenticatedUser) authentication.getPrincipal()).userId();
        AnalyzeResult result;

        try {
            result = geminiService.analyze(request.getText());
        } catch (RuntimeException exception) {
            saveFailedConversation(request.getText(), userId, startTime);
            throw exception;
        }

        long latencyMs = elapsedMilliseconds(startTime);
        conversationService.saveConversation(
                request.getText(),
                result.responseJson(),
                "gemini",
                geminiService.getModel(),
                result.inputTokens(),
                result.outputTokens(),
                latencyMs,
                userId,
                "success"
        );
        return ResponseEntity.ok(result.response());
    }

    private void saveFailedConversation(String message, Long userId, long startTime) {
        conversationService.saveConversation(
                message,
                null,
                "gemini",
                geminiService.getModel(),
                null,
                null,
                elapsedMilliseconds(startTime),
                userId,
                "error"
        );
    }

    private long elapsedMilliseconds(long startTime) {
        return (System.nanoTime() - startTime) / 1_000_000;
    }
}
