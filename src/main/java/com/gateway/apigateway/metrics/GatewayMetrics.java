package com.gateway.apigateway.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

@Component
public class GatewayMetrics {

    private final AtomicLong totalRequests = new AtomicLong();
    private final AtomicLong activeRequests = new AtomicLong();

    private final Map<String, AtomicLong> routeCounts = new ConcurrentHashMap<>();
    private final Map<Integer, AtomicLong> statusCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> latencyTotals = new ConcurrentHashMap<>();

    public void onRequestStart() {
        totalRequests.incrementAndGet();
        activeRequests.incrementAndGet();
    }

    public void onRequestEnd(String route, int status, long latencyMs) {
        activeRequests.decrementAndGet();

        routeCounts
                .computeIfAbsent(route, k -> new AtomicLong())
                .incrementAndGet();

        statusCounts
                .computeIfAbsent(status, k -> new AtomicLong())
                .incrementAndGet();

        latencyTotals
                .computeIfAbsent(route, k -> new AtomicLong())
                .addAndGet(latencyMs);
    }

    public Map<String, Object> snapshot() {
        return Map.of(
                "totalRequests", totalRequests.get(),
                "activeRequests", activeRequests.get(),
                "routes", routeCounts,
                "statusCodes", statusCounts,
                "latencyTotalsMs", latencyTotals
        );
    }
}
