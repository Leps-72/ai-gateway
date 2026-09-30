package com.aigateway.exception;

import com.aigateway.dto.ErrorResponse;
import com.aigateway.service.AuthService.UsernameAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> handleValidation(
            Exception exception,
            HttpServletRequest request
    ) {
        String message = exception instanceof IllegalArgumentException
                ? exception.getMessage()
                : "Request body is missing or invalid.";
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException exception,
            HttpServletRequest request
    ) {
        return error(
                HttpStatus.UNAUTHORIZED,
                "AUTHENTICATION_ERROR",
                "Invalid username or password.",
                request
        );
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUsername(
            UsernameAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.CONFLICT, "USERNAME_CONFLICT", exception.getMessage(), request);
    }

    @ExceptionHandler(AiTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleAiTimeout(
            AiTimeoutException exception,
            HttpServletRequest request
    ) {
        return error(
                HttpStatus.GATEWAY_TIMEOUT,
                "AI_TIMEOUT",
                "AI provider did not respond in time.",
                request
        );
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimit(
            RateLimitExceededException exception,
            HttpServletRequest request
    ) {
        ErrorResponse response = errorResponse(
                HttpStatus.TOO_MANY_REQUESTS,
                "RATE_LIMIT_EXCEEDED",
                "Too many AI requests. Please try again later.",
                request
        );
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(exception.getRetryAfterSeconds()))
                .body(response);
    }

    @ExceptionHandler(InvalidAiResponseException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAiResponse(
            InvalidAiResponseException exception,
            HttpServletRequest request
    ) {
        return error(
                HttpStatus.BAD_GATEWAY,
                "INVALID_AI_RESPONSE",
                "AI provider returned an invalid response.",
                request
        );
    }

    @ExceptionHandler(AiProviderException.class)
    public ResponseEntity<ErrorResponse> handleAiProvider(
            AiProviderException exception,
            HttpServletRequest request
    ) {
        return error(
                HttpStatus.SERVICE_UNAVAILABLE,
                "AI_PROVIDER_ERROR",
                "AI provider is temporarily unavailable.",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "An unexpected error occurred.",
                request
        );
    }

    private ResponseEntity<ErrorResponse> error(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request
    ) {
        ErrorResponse response = errorResponse(status, error, message, request);
        return ResponseEntity.status(status).body(response);
    }

    private ErrorResponse errorResponse(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request
    ) {
        return new ErrorResponse(
                Instant.now(),
                status.value(),
                error,
                message,
                request.getRequestURI()
        );
    }
}
