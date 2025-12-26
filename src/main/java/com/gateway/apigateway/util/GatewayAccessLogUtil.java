package com.gateway.apigateway.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Kong-style access log utility
 * Writes JSON lines (one request per line)
 */
public final class GatewayAccessLogUtil {

    private static final String LOG_FILE = "gateway-access.log";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private GatewayAccessLogUtil() {
        // utility class
    }

    public static synchronized void log(
            String method,
            String path,
            String upstream,
            int status,
            long latencyMs) {
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("timestamp", Instant.now().toString());
        log.put("method", method);
        log.put("path", path);
        log.put("upstream", upstream);
        log.put("status", status);
        log.put("latency_ms", latencyMs);

        write(log);
    }

    private static void write(Map<String, Object> log) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            fw.write(MAPPER.writeValueAsString(log));
            fw.write("\n");
        } catch (IOException e) {
            // NEVER break gateway traffic because of logging
            e.printStackTrace();
        }
    }
}
