package com.foodwaste.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IpAbuseGuard {

    private static final Logger log = LoggerFactory.getLogger(IpAbuseGuard.class);
    private static final int MAX_REQUESTS_PER_MINUTE = 50;
    private static final long ONE_MINUTE_SECONDS = 60;
    private static final long BLOCK_DURATION_SECONDS = 3 * 60 * 60;

    private final ConcurrentHashMap<String, Integer> requestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant> windowStart = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant> blockedIps = new ConcurrentHashMap<>();

    public boolean isBlocked(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }

        Instant unblockTime = blockedIps.get(ip);

        if (unblockTime == null) {
            return false;
        }

        if (Instant.now().isAfter(unblockTime)) {
            blockedIps.remove(ip);
            return false;
        }

        return true;
    }

    public void recordRequest(String ip) {
        if (ip == null || ip.isBlank()) {
            return;
        }

        Instant now = Instant.now();
        Instant start = windowStart.get(ip);

        if (start == null || now.getEpochSecond() - start.getEpochSecond() > ONE_MINUTE_SECONDS) {
            windowStart.put(ip, now);
            requestCounts.put(ip, 1);
            return;
        }

        int count = requestCounts.getOrDefault(ip, 0) + 1;
        requestCounts.put(ip, count);

        if (count >= MAX_REQUESTS_PER_MINUTE && !isBlocked(ip)) {
            Instant unblockTime = now.plusSeconds(BLOCK_DURATION_SECONDS);
            blockedIps.put(ip, unblockTime);
            log.warn("IP {} blocked for 3 hours (sent {} requests in 1 minute). Unblock at: {}", ip, count, unblockTime);
            requestCounts.remove(ip);
            windowStart.remove(ip);
        }
    }

    public void unblockIp(String ip) {
        if (ip != null) {
            blockedIps.remove(ip);
            requestCounts.remove(ip);
            windowStart.remove(ip);
            log.info("IP {} manually unblocked by administrator", ip);
        }
    }

    public Map<String, Instant> getBlockedIps() {
        Map<String, Instant> active = new HashMap<>();
        Instant now = Instant.now();

        for (Map.Entry<String, Instant> entry : blockedIps.entrySet()) {
            if (entry.getValue().isAfter(now)) {
                active.put(entry.getKey(), entry.getValue());
            }
        }

        return Collections.unmodifiableMap(active);
    }
}
