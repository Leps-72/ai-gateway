package com.aigateway.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitServiceTest {

    @Test
    void allowsRequestsBelowLimitAndRejectsEleventhRequest() {
        MutableClock clock = new MutableClock(Instant.parse("2026-09-30T00:00:00Z"));
        RateLimitService service = new RateLimitService(10, 60, clock);

        for (int request = 1; request <= 10; request++) {
            assertTrue(service.tryAcquire(1L).allowed());
        }

        assertFalse(service.tryAcquire(1L).allowed());
    }

    @Test
    void keepsIndependentCountersForDifferentUsers() {
        MutableClock clock = new MutableClock(Instant.parse("2026-09-30T00:00:00Z"));
        RateLimitService service = new RateLimitService(1, 60, clock);

        assertTrue(service.tryAcquire(1L).allowed());
        assertFalse(service.tryAcquire(1L).allowed());
        assertTrue(service.tryAcquire(2L).allowed());
    }

    @Test
    void allowsRequestsAgainAfterWindowExpires() {
        MutableClock clock = new MutableClock(Instant.parse("2026-09-30T00:00:00Z"));
        RateLimitService service = new RateLimitService(1, 60, clock);

        assertTrue(service.tryAcquire(1L).allowed());
        assertFalse(service.tryAcquire(1L).allowed());

        clock.advance(Duration.ofSeconds(60));

        assertTrue(service.tryAcquire(1L).allowed());
    }

    private static class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
