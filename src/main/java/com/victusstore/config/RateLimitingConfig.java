package com.victusstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;

@Configuration
public class RateLimitingConfig {

    // Simple in-memory rate limiter
    // For production, consider using Redis-based rate limiting
    private final Map<String, RateLimitInfo> rateLimitStore = new ConcurrentHashMap<>();

    @Bean
    public RateLimiter rateLimiter() {
        return new RateLimiter() {
            @Override
            public boolean allowRequest(String key, int maxRequests, long windowMs) {
                long now = System.currentTimeMillis();
                RateLimitInfo info = rateLimitStore.computeIfAbsent(key, k -> new RateLimitInfo());
                
                synchronized (info) {
                    if (now - info.windowStart > windowMs) {
                        // Reset window
                        info.count.set(0);
                        info.windowStart = now;
                    }
                    
                    if (info.count.get() < maxRequests) {
                        info.count.incrementAndGet();
                        return true;
                    }
                    return false;
                }
            }
        };
    }

    public interface RateLimiter {
        boolean allowRequest(String key, int maxRequests, long windowMs);
    }

    private static class RateLimitInfo {
        AtomicInteger count = new AtomicInteger(0);
        long windowStart = System.currentTimeMillis();
    }
}

