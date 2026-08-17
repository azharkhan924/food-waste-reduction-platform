package com.foodwaste.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class IpAbuseGuard {

    private static final Logger log = LoggerFactory.getLogger(IpAbuseGuard.class);
    private static final int MAX_REQUESTS_PER_MINUTE = 50;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Cache<String, AtomicInteger> requestCounts;
    private final Cache<String, Instant> blockedIps;

    public IpAbuseGuard() {
        this.requestCounts = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(1))
                .maximumSize(50_000)
                .build();

        this.blockedIps = Caffeine.newBuilder()
                .expireAfter(new Expiry<String, Instant>() {
                    @Override
                    public long expireAfterCreate(String key, Instant expiryInstant, long currentTime) {
                        long nanos = Duration.between(Instant.now(), expiryInstant).toNanos();
                        return Math.max(nanos, 0);
                    }

                    @Override
                    public long expireAfterUpdate(String key, Instant expiryInstant, long currentTime, long currentDuration) {
                        long nanos = Duration.between(Instant.now(), expiryInstant).toNanos();
                        return Math.max(nanos, 0);
                    }

                    @Override
                    public long expireAfterRead(String key, Instant expiryInstant, long currentTime, long currentDuration) {
                        return currentDuration;
                    }
                })
                .maximumSize(10_000)
                .build();
    }

    public boolean isBlocked(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        Instant unblockTime = blockedIps.getIfPresent(ip);
        if (unblockTime == null) {
            return false;
        }
        if (Instant.now().isAfter(unblockTime)) {
            blockedIps.invalidate(ip);
            return false;
        }
        return true;
    }

    public void recordRequest(String ip) {
        if (ip == null || ip.isBlank()) {
            return;
        }
        AtomicInteger counter = requestCounts.get(ip, k -> new AtomicInteger(0));
        if (counter != null) {
            int currentCount = counter.incrementAndGet();
            if (currentCount >= MAX_REQUESTS_PER_MINUTE && !isBlocked(ip)) {
                int jitterMinutes = RANDOM.nextInt(61); // 0 to 60 minutes
                Duration blockDuration = Duration.ofHours(3).plus(Duration.ofMinutes(jitterMinutes));
                Instant unblockTime = Instant.now().plus(blockDuration);
                blockedIps.put(ip, unblockTime);
                log.warn("IP {} hard-blocked for {} until {}", ip, blockDuration, unblockTime);
            }
        }
    }

    public void unblockIp(String ip) {
        if (ip != null) {
            blockedIps.invalidate(ip);
            requestCounts.invalidate(ip);
            log.info("IP {} manually unblocked by administrator", ip);
        }
    }

    public Map<String, Instant> getBlockedIps() {
        Map<String, Instant> active = new HashMap<>();
        Instant now = Instant.now();
        for (Map.Entry<String, Instant> entry : blockedIps.asMap().entrySet()) {
            if (entry.getValue().isAfter(now)) {
                active.put(entry.getKey(), entry.getValue());
            }
        }
        return Collections.unmodifiableMap(active);
    }
}
