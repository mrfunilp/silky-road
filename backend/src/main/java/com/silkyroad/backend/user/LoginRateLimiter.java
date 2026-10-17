package com.silkyroad.backend.user;

import java.time.Duration;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.silkyroad.backend.common.TooManyRequestsException;

import io.github.bucket4j.Bucket;


@Component
public class LoginRateLimiter {

    private static final int USER_ATTEMPTS = 5;
    private static final int IP_ATTEMPTS = 20;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    // cache for ratelimiting both through ip and username 
    private final Cache<String, Bucket> byUser = Caffeine.newBuilder()
            .expireAfterAccess(WINDOW).maximumSize(100_000).build();
    private final Cache<String, Bucket> byIp = Caffeine.newBuilder()
            .expireAfterAccess(WINDOW).maximumSize(100_000).build();

    public void checkAllowed(String username, String ip) {
        long userWait = waitNanos(byUser.getIfPresent(key(username)));
        long ipWait = waitNanos(byIp.getIfPresent(ip));
        if (userWait > 0 || ipWait > 0) {
            throw new TooManyRequestsException("Too many failed login attempts. Try again later.");
        }
    }

    public void recordFailure(String username, String ip) {
        byUser.get(key(username), k -> newBucket(USER_ATTEMPTS)).tryConsume(1);
        byIp.get(ip, k -> newBucket(IP_ATTEMPTS)).tryConsume(1);
    }

    public void recordSuccess(String username) {
        byUser.invalidate(key(username));
    }

    private static long waitNanos(Bucket bucket) {
        return bucket == null ? 0 : bucket.estimateAbilityToConsume(1).getNanosToWaitForRefill();
    }

    private static Bucket newBucket(int attempts) {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(attempts).refillGreedy(attempts, WINDOW))
                .build();
    }

    private static String key(String username) {
        return username.toLowerCase(Locale.ROOT);
    }
}
