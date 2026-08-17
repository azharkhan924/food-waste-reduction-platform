package com.foodwaste.security;

import java.time.Duration;

public enum RateLimitTier {
    AUTH(5, Duration.ofMinutes(1)),
    OTP(3, Duration.ofMinutes(3)),
    API(60, Duration.ofMinutes(1)),
    GLOBAL(120, Duration.ofMinutes(1));

    private final long capacity;
    private final Duration refillDuration;

    RateLimitTier(long capacity, Duration refillDuration) {
        this.capacity = capacity;
        this.refillDuration = refillDuration;
    }

    public long getCapacity() {
        return capacity;
    }

    public Duration getRefillDuration() {
        return refillDuration;
    }
}
