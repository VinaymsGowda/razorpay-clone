package com.vinayms.razorpayclone.common.ratelimiting;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SlidingWindowRateLimiter {

    private static final String KEY_PREFIX = "apiKey:rate-limit:";
    private static final long WINDOW_MILLIS = Duration.ofMinutes(1).toMillis();

    private final StringRedisTemplate redisTemplate;

    @Value("${api-key.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    public RateLimitResult checkRequest(String keyId) {
        if (requestsPerMinute <= 0) {
            return new RateLimitResult(false, requestsPerMinute, 0);
        }

        String key = KEY_PREFIX + keyId;
        long now = System.currentTimeMillis();
        long windowStart = now - WINDOW_MILLIS;
        ZSetOperations<String, String> requests = redisTemplate.opsForZSet();

        requests.removeRangeByScore(key, 0, windowStart);
        Long requestCount = requests.zCard(key);
        if (requestCount != null && requestCount >= requestsPerMinute) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
            return new RateLimitResult(false, requestsPerMinute, requestCount.intValue());
        }

        requests.add(key, UUID.randomUUID().toString(), now);
        redisTemplate.expire(key, Duration.ofMinutes(1));
        int currentRequestCount = requestCount == null ? 1 : requestCount.intValue() + 1;
        return new RateLimitResult(true, requestsPerMinute, currentRequestCount);
    }

    public record RateLimitResult(
            boolean allowed,
            int allowedRequestsPerMinute,
            int currentRequestsPerMinute
    ) {
    }
}
