package com.foodwaste.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitingServiceTest {

    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        rateLimitingService = new RateLimitingService();
    }

    @Test
    void testAuthTier_AllowsCapacityAndBlocksExcess() {
        String ip = "10.0.0.1";
        // AUTH capacity is 5
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimitingService.tryConsume(ip, RateLimitTier.AUTH), "Request " + (i + 1) + " should succeed");
        }
        assertFalse(rateLimitingService.tryConsume(ip, RateLimitTier.AUTH), "6th request should be blocked");
    }

    @Test
    void testDifferentIps_HaveIndependentBuckets() {
        String ip1 = "10.0.0.1";
        String ip2 = "10.0.0.2";

        for (int i = 0; i < 5; i++) {
            rateLimitingService.tryConsume(ip1, RateLimitTier.AUTH);
        }
        assertFalse(rateLimitingService.tryConsume(ip1, RateLimitTier.AUTH));
        assertTrue(rateLimitingService.tryConsume(ip2, RateLimitTier.AUTH));
    }

    @Test
    void testGetAvailableTokens() {
        String ip = "10.0.0.3";
        assertEquals(5, rateLimitingService.getAvailableTokens(ip, RateLimitTier.AUTH));
        rateLimitingService.tryConsume(ip, RateLimitTier.AUTH);
        assertEquals(4, rateLimitingService.getAvailableTokens(ip, RateLimitTier.AUTH));
    }

    @Test
    void testRateLimitTierEnum() {
        assertEquals(5, RateLimitTier.AUTH.getCapacity());
        assertNotNull(RateLimitTier.AUTH.getRefillDuration());
        assertEquals(3, RateLimitTier.OTP.getCapacity());
        assertEquals(60, RateLimitTier.API.getCapacity());
        assertEquals(120, RateLimitTier.GLOBAL.getCapacity());
    }
}
