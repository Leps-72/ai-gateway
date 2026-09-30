package com.aigateway.web;

import com.aigateway.exception.RateLimitExceededException;
import com.aigateway.security.AuthenticatedUser;
import com.aigateway.service.RateLimitService;
import com.aigateway.service.RateLimitService.RateLimitDecision;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.security.Principal;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final RateLimitService rateLimitService;

    public RateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        Principal principal = request.getUserPrincipal();
        if (!(principal instanceof Authentication authentication)
                || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return true;
        }

        RateLimitDecision decision = rateLimitService.tryAcquire(user.userId());
        if (!decision.allowed()) {
            logger.warn("RATE_LIMIT userId={} endpoint={}", user.userId(), request.getRequestURI());
            throw new RateLimitExceededException(decision.retryAfterSeconds());
        }
        return true;
    }
}
