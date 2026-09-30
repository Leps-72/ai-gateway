package com.aigateway.exception;

import com.aigateway.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsTimeoutToGatewayTimeout() {
        ResponseEntity<ErrorResponse> response = handler.handleAiTimeout(
                new AiTimeoutException("timeout", new RuntimeException()),
                request("/ai/chat")
        );

        assertError(response, HttpStatus.GATEWAY_TIMEOUT, "AI_TIMEOUT", "/ai/chat");
    }

    @Test
    void mapsProviderFailureToServiceUnavailable() {
        ResponseEntity<ErrorResponse> response = handler.handleAiProvider(
                new AiProviderException("provider failure"),
                request("/ai/chat")
        );

        assertError(response, HttpStatus.SERVICE_UNAVAILABLE, "AI_PROVIDER_ERROR", "/ai/chat");
    }

    @Test
    void mapsMalformedResponseToBadGateway() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidAiResponse(
                new InvalidAiResponseException("invalid JSON"),
                request("/ai/analyze")
        );

        assertError(response, HttpStatus.BAD_GATEWAY, "INVALID_AI_RESPONSE", "/ai/analyze");
    }

    private MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(path);
        return request;
    }

    private void assertError(
            ResponseEntity<ErrorResponse> response,
            HttpStatus status,
            String error,
            String path
    ) {
        assertEquals(status, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(status.value(), response.getBody().status());
        assertEquals(error, response.getBody().error());
        assertEquals(path, response.getBody().path());
    }
}
