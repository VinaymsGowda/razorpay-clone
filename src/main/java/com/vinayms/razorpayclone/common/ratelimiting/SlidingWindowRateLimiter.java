package com.vinayms.razorpayclone.common.ratelimiting;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SlidingWindowRateLimiter {

    private static final String KEY_PREFIX = "api-key:rate-limit:";
    private static final long WINDOW_MILLIS = Duration.ofMinutes(1).toMillis();

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT = new DefaultRedisScript<>("""
            local now = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local limit = tonumber(ARGV[3])
            local member = ARGV[4]

            redis.call('ZREMRANGEBYSCORE', KEYS[1], '-inf', now - window)
            local currentCount = redis.call('ZCARD', KEYS[1])

            if currentCount >= limit then
                redis.call('EXPIRE', KEYS[1], math.ceil(window / 1000))
                return -currentCount
            end

            redis.call('ZADD', KEYS[1], now, member)
            currentCount = currentCount + 1
            redis.call('EXPIRE', KEYS[1], math.ceil(window / 1000))
            return currentCount
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    @Value("${api-key.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    public RateLimitResult checkRequest(String keyId) {
        if (requestsPerMinute <= 0) {
            return new RateLimitResult(false, requestsPerMinute, 0);
        }

        String key = KEY_PREFIX + keyId;
        long now = System.currentTimeMillis();
        Long result = redisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                List.of(key),
                String.valueOf(now),
                String.valueOf(WINDOW_MILLIS),
                String.valueOf(requestsPerMinute),
                UUID.randomUUID().toString()
        );

        if (result == null) {
            return new RateLimitResult(false, requestsPerMinute, 0);
        }

        boolean allowed = result >= 0;
        int currentRequestCount = Math.toIntExact(Math.abs(result));
        return new RateLimitResult(allowed, requestsPerMinute, currentRequestCount);
    }

    public record RateLimitResult(
            boolean allowed,
            int allowedRequestsPerMinute,
            int currentRequestsPerMinute
    ) {
    }
}
