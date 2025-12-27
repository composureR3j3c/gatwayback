package com.gateway.apigateway.util;

import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.UUID;

public final class GatewayRequestUtil {

    private GatewayRequestUtil() {
    }

    public static String getRequestId(ServerHttpRequest request) {
        String rid = request.getHeaders().getFirst("X-Request-Id");
        return (rid != null && !rid.isBlank())
                ? rid
                : UUID.randomUUID().toString();
    }

    public static String getClientIp(ServerHttpRequest request) {

        String xff = request.getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }

        if (request.getRemoteAddress() != null) {
            String ip = request.getRemoteAddress()
                    .getAddress()
                    .getHostAddress();

            // Normalize IPv6 loopback → IPv4
            if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
                return "127.0.0.1";
            }

            // Handle IPv4-mapped IPv6 (::ffff:127.0.0.1)
            if (ip.startsWith("::ffff:")) {
                return ip.substring(7);
            }

            return ip;
        }

        return "unknown";
    }

}
