package com.aigateway.controller;

import com.aigateway.dto.AiRequest;
import com.aigateway.dto.AiChatResponse;
import com.aigateway.dto.AnalyzeRequest;
import com.aigateway.dto.AnalyzeResponse;
import com.aigateway.provider.AiProviderResult;
import com.aigateway.provider.AnalyzeProviderResult;
import com.aigateway.service.AiGatewayService;
import com.aigateway.service.ConversationService;
import com.aigateway.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@Tag(name = "AI", description = "Gemini chat and structured analysis")
@SecurityRequirement(name = "bearerAuth")
public class AiController {

    private static final Logger logger = LoggerFactory.getLogger(AiController.class);

    private final ConversationService conversationService;
    private final AiGatewayService aiGatewayService;

    public AiController(ConversationService conversationService, AiGatewayService aiGatewayService) {
        this.conversationService = conversationService;
        this.aiGatewayService = aiGatewayService;
    }

    @PostMapping("/chat")
    @Operation(summary = "Send a chat message to the AI provider")
    public ResponseEntity<AiChatResponse> chat(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AiRequest.class),
                            examples = @ExampleObject(
                                    value = "{\"message\":\"Explain REST API in one sentence.\"}"
                            )
                    )
            )
            @RequestBody AiRequest request,
            Authentication authentication
    ) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new IllegalArgumentException("Message must not be blank.");
        }

        long startTime = System.nanoTime();
        Long userId = ((AuthenticatedUser) authentication.getPrincipal()).userId();
        AiProviderResult result;

        try {
            result = aiGatewayService.chat(request.getMessage());
        } catch (RuntimeException exception) {
            long latencyMs = elapsedMilliseconds(startTime);
            saveFailedConversation(request.getMessage(), userId, latencyMs);
            logAiRequest(userId, "chat", "error", latencyMs);
            throw exception;
        }

        long latencyMs = elapsedMilliseconds(startTime);
        conversationService.saveConversation(
                request.getMessage(),
                result.response(),
                aiGatewayService.getProviderName(),
                aiGatewayService.getModelName(),
                result.inputTokens(),
                result.outputTokens(),
                latencyMs,
                userId,
                "success"
        );
        logAiRequest(userId, "chat", "success", latencyMs);

        AiChatResponse response = new AiChatResponse(
                result.response(),
                aiGatewayService.getProviderName(),
                aiGatewayService.getModelName(),
                "success"
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/analyze")
    @Operation(summary = "Analyze text and return structured output")
    public ResponseEntity<AnalyzeResponse> analyze(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AnalyzeRequest.class),
                            examples = @ExampleObject(
                                    value = "{\"text\":\"Users report that checkout is extremely slow.\"}"
                            )
                    )
            )
            @RequestBody AnalyzeRequest request,
            Authentication authentication
    ) {
        if (request.getText() == null || request.getText().isBlank()) {
            throw new IllegalArgumentException("Text must not be blank.");
        }

        long startTime = System.nanoTime();
        Long userId = ((AuthenticatedUser) authentication.getPrincipal()).userId();
        AnalyzeProviderResult result;

        try {
            result = aiGatewayService.analyze(request.getText());
        } catch (RuntimeException exception) {
            long latencyMs = elapsedMilliseconds(startTime);
            saveFailedConversation(request.getText(), userId, latencyMs);
            logAiRequest(userId, "analyze", "error", latencyMs);
            throw exception;
        }

        long latencyMs = elapsedMilliseconds(startTime);
        conversationService.saveConversation(
                request.getText(),
                result.responseJson(),
                aiGatewayService.getProviderName(),
                aiGatewayService.getModelName(),
                result.inputTokens(),
                result.outputTokens(),
                latencyMs,
                userId,
                "success"
        );
        logAiRequest(userId, "analyze", "success", latencyMs);
        AnalyzeResponse response = new AnalyzeResponse();
        response.setSummary(result.summary());
        response.setSentiment(result.sentiment());
        response.setCategory(result.category());
        response.setPriority(result.priority());
        return ResponseEntity.ok(response);
    }

    private void saveFailedConversation(String message, Long userId, long latencyMs) {
        conversationService.saveConversation(
                message,
                null,
                aiGatewayService.getProviderName(),
                aiGatewayService.getModelName(),
                null,
                null,
                latencyMs,
                userId,
                "error"
        );
    }

    private void logAiRequest(Long userId, String type, String status, long latencyMs) {
        logger.info(
                "AI_REQUEST userId={} type={} provider={} model={} status={} latencyMs={}",
                userId,
                type,
                aiGatewayService.getProviderName(),
                aiGatewayService.getModelName(),
                status,
                latencyMs
        );
    }

    private long elapsedMilliseconds(long startTime) {
        return (System.nanoTime() - startTime) / 1_000_000;
    }
}
