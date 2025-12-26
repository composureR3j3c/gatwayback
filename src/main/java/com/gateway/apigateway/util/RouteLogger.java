package com.gateway.apigateway.util;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class RouteLogger {

    private static final Path LOG_PATH = Paths.get("gateway-routes.log");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public RouteLogger() {
        try {
            if (!Files.exists(LOG_PATH)) {
                Files.createFile(LOG_PATH);
            }
        } catch (Exception ignored) {}
    }

    public Mono<Void> log(String path, String upstream, int status) {
        String entry = String.format(
                "[%s] PATH=%s → %s | STATUS=%d%n",
                LocalDateTime.now().format(FMT),
                path,
                upstream,
                status
        );

        return Mono.fromCallable(() -> {
            try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(
                    LOG_PATH, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.APPEND
            )) {
                ByteBuffer buffer = ByteBuffer.wrap(entry.getBytes());
                channel.write(buffer, Files.size(LOG_PATH));
            }
            return null;
        }).then();
    }
}
