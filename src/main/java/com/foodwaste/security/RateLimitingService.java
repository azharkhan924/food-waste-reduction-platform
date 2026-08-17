package com.foodwaste.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitingService {

    private final Cache<String, Bucket> buckets;

    public RateLimitingService() {
        this.buckets = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(1))
                .maximumSize(50_000)
                .build();
    }

    public boolean tryConsume(String ip, RateLimitTier tier) {
        String key = (ip != null ? ip : "127.0.0.1") + ":" + tier.name();
        Bucket bucket = buckets.get(key, k -> createNewBucket(tier));
        return bucket != null && bucket.tryConsume(1);
    }

    public long getAvailableTokens(String ip, RateLimitTier tier) {
        String key = (ip != null ? ip : "127.0.0.1") + ":" + tier.name();
        Bucket bucket = buckets.get(key, k -> createNewBucket(tier));
        return bucket != null ? bucket.getAvailableTokens() : tier.getCapacity();
    }

    private Bucket createNewBucket(RateLimitTier tier) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(tier.getCapacity())
                .refillGreedy(tier.getCapacity(), tier.getRefillDuration())
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
