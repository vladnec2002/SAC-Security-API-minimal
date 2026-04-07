package com.example.gateway_service.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final int publicMaxRequests;
    private final long publicWindowSeconds;
    private final int privateMaxRequests;
    private final long privateWindowSeconds;

    private final Map<String, ClientWindow> storage = new ConcurrentHashMap<>();

    public RateLimiterService(
            @Value("${rate.limit.public.max-requests}") int publicMaxRequests,
            @Value("${rate.limit.public.window-seconds}") long publicWindowSeconds,
            @Value("${rate.limit.private.max-requests}") int privateMaxRequests,
            @Value("${rate.limit.private.window-seconds}") long privateWindowSeconds
    ) {
        this.publicMaxRequests = publicMaxRequests;
        this.publicWindowSeconds = publicWindowSeconds;
        this.privateMaxRequests = privateMaxRequests;
        this.privateWindowSeconds = privateWindowSeconds;
    }

    public boolean allowRequest(String clientId, boolean isPrivate) {
        long now = Instant.now().getEpochSecond();
        int maxRequests = isPrivate ? privateMaxRequests : publicMaxRequests;
        long windowSeconds = isPrivate ? privateWindowSeconds : publicWindowSeconds;

        String key = clientId + ":" + (isPrivate ? "private" : "public");

        storage.compute(key, (k, current) -> {
            if (current == null || now - current.windowStart >= windowSeconds) {
                return new ClientWindow(now, 1);
            }
            current.requestCount++;
            return current;
        });

        ClientWindow window = storage.get(key);
        return window.requestCount <= maxRequests;
    }

    private static class ClientWindow {
        long windowStart;
        int requestCount;

        ClientWindow(long windowStart, int requestCount) {
            this.windowStart = windowStart;
            this.requestCount = requestCount;
        }
    }
}