package com.aigateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class RateLimitService {

    private final ConcurrentHashMap<Long, RequestWindow> windows = new ConcurrentHashMap<>();
    private final int requestsPerWindow;
    private final Duration windowDuration;
    private final Clock clock;

    @Autowired
    public RateLimitService(
            @Value("${rate-limit.requests-per-minute:10}") int requestsPerWindow,
            @Value("${rate-limit.window-seconds:60}") long windowSeconds
    ) {
        this(requestsPerWindow, windowSeconds, Clock.systemUTC());
    }

    RateLimitService(int requestsPerWindow, long windowSeconds, Clock clock) {
        if (requestsPerWindow <= 0 || windowSeconds <= 0) {
            throw new IllegalStateException("Rate limit configuration must be greater than zero.");
        }
        this.requestsPerWindow = requestsPerWindow;
        this.windowDuration = Duration.ofSeconds(windowSeconds);
        this.clock = clock;
    }

    public RateLimitDecision tryAcquire(Long userId) {
        Objects.requireNonNull(userId, "Authenticated user id is required.");
        Instant now = clock.instant();
        AtomicReference<RateLimitDecision> decision = new AtomicReference<>();

        windows.compute(userId, (id, currentWindow) -> {
            if (currentWindow == null
                    || !now.isBefore(currentWindow.startedAt().plus(windowDuration))) {
                decision.set(new RateLimitDecision(true, 0));
                return new RequestWindow(now, 1);
            }

            if (currentWindow.requestCount() < requestsPerWindow) {
                decision.set(new RateLimitDecision(true, 0));
                return new RequestWindow(currentWindow.startedAt(), currentWindow.requestCount() + 1);
            }

            Instant resetsAt = currentWindow.startedAt().plus(windowDuration);
            long remainingMillis = Math.max(1, Duration.between(now, resetsAt).toMillis());
            long retryAfterSeconds = Math.max(1, (remainingMillis + 999) / 1_000);
            decision.set(new RateLimitDecision(false, retryAfterSeconds));
            return currentWindow;
        });

        return decision.get();
    }

    public record RateLimitDecision(boolean allowed, long retryAfterSeconds) {
    }

    private record RequestWindow(Instant startedAt, int requestCount) {
    }
}
