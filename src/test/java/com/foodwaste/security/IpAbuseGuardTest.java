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
        assertFalse(ipAbuseGuard.isBlocked("192.168.1.1"));
        assertFalse(ipAbuseGuard.isBlocked(null));
        assertFalse(ipAbuseGuard.isBlocked(""));
    }

    @Test
    void testUnderThreshold_NotBlocked() {
        String ip = "192.168.1.2";
        for (int i = 0; i < 49; i++) {
            ipAbuseGuard.recordRequest(ip);
        }
        assertFalse(ipAbuseGuard.isBlocked(ip));
    }

    @Test
    void testReachingThreshold_TriggersHardBlock() {
        String ip = "192.168.1.3";
        for (int i = 0; i < 50; i++) {
            ipAbuseGuard.recordRequest(ip);
        }
        assertTrue(ipAbuseGuard.isBlocked(ip));

        Map<String, Instant> blockedIps = ipAbuseGuard.getBlockedIps();
        assertTrue(blockedIps.containsKey(ip));
        assertTrue(blockedIps.get(ip).isAfter(Instant.now()));
    }

    @Test
    void testAdminManualUnblock() {
        String ip = "192.168.1.4";
        for (int i = 0; i < 50; i++) {
            ipAbuseGuard.recordRequest(ip);
        }
        assertTrue(ipAbuseGuard.isBlocked(ip));

        ipAbuseGuard.unblockIp(ip);
        assertFalse(ipAbuseGuard.isBlocked(ip));
    }
}
