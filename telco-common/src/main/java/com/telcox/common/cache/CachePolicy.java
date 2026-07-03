package com.telcox.common.cache;

import java.time.Duration;

public final class CachePolicy {
    private CachePolicy() {}

    public static final Duration PROJECTION_TTL = Duration.ofMinutes(30);
    public static final Duration SHORT_TTL = Duration.ofMinutes(5);

    public static Duration withJitter(Duration base) {
        long millis = base.toMillis();
        long jitter = (long) (millis * (Math.random() * 0.2 - 0.1));
        return Duration.ofMillis(millis + jitter);
    }
}
