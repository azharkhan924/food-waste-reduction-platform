package com.foodwaste.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IpAbuseGuardTest {

    private IpAbuseGuard ipAbuseGuard;

    @BeforeEach
    void setUp() {
        ipAbuseGuard = new IpAbuseGuard();
    }

    @Test
    void testInitialState_NotBlocked() {
        // A new IP should not be blocked
        assertFalse(ipAbuseGuard.isBlocked("192.168.1.1"));
        assertFalse(ipAbuseGuard.isBlocked(null));
        assertFalse(ipAbuseGuard.isBlocked(""));
    }

    @Test
    void testUnderThreshold_NotBlocked() {
        // Sending 49 requests (under the limit of 50) should NOT block the IP
        String ip = "192.168.1.2";
        for (int i = 0; i < 49; i++) {
            ipAbuseGuard.recordRequest(ip);
        }
        assertFalse(ipAbuseGuard.isBlocked(ip));
    }

    @Test
    void testReachingThreshold_TriggersBlock() {
        // Sending 50 requests should block the IP
        String ip = "192.168.1.3";
        for (int i = 0; i < 50; i++) {
            ipAbuseGuard.recordRequest(ip);
        }
        assertTrue(ipAbuseGuard.isBlocked(ip));

        // The blocked IPs map should contain this IP
        Map<String, Instant> blockedIps = ipAbuseGuard.getBlockedIps();
        assertTrue(blockedIps.containsKey(ip));
        assertTrue(blockedIps.get(ip).isAfter(Instant.now()));
    }

    @Test
    void testAdminManualUnblock() {
        // Block an IP first
        String ip = "192.168.1.4";
        for (int i = 0; i < 50; i++) {
            ipAbuseGuard.recordRequest(ip);
        }
        assertTrue(ipAbuseGuard.isBlocked(ip));

        // Admin unblocks the IP
        ipAbuseGuard.unblockIp(ip);
        assertFalse(ipAbuseGuard.isBlocked(ip));
    }

    @Test
    void testNullAndBlankIp_NoError() {
        // These should not cause any error
        ipAbuseGuard.recordRequest(null);
        ipAbuseGuard.recordRequest("");
        ipAbuseGuard.recordRequest("   ");
        ipAbuseGuard.unblockIp(null);
    }

    @Test
    void testDifferentIps_IndependentCounting() {
        // Requests from different IPs should be counted separately
        String ip1 = "10.0.0.1";
        String ip2 = "10.0.0.2";

        // Send 50 from ip1 → should be blocked
        for (int i = 0; i < 50; i++) {
            ipAbuseGuard.recordRequest(ip1);
        }
        assertTrue(ipAbuseGuard.isBlocked(ip1));

        // ip2 should still be fine
        assertFalse(ipAbuseGuard.isBlocked(ip2));
    }
}
