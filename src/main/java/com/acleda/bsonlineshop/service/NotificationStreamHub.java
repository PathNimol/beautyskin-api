package com.acleda.bsonlineshop.service;

import com.acleda.bsonlineshop.dto.notification.NotificationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * In-memory SSE fan-out per user. Pushes {@link NotificationResponse} when notifications are created.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationStreamHub {

    private static final long TIMEOUT_MS = 30L * 60L * 1000L;

    private final ObjectMapper objectMapper;
    private final CopyOnWriteArrayList<UserSubscription> subscriptions = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        UserSubscription sub = new UserSubscription(userId, emitter);
        subscriptions.add(sub);

        Runnable cleanup = () -> subscriptions.remove(sub);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        try {
            emitter.send(SseEmitter.event().name("connected").data("{\"status\":\"ok\"}"));
        } catch (IOException e) {
            cleanup.run();
            emitter.completeWithError(e);
        }
        return emitter;
    }

    public void publish(UUID userId, NotificationResponse notification) {
        if (userId == null || notification == null) {
            return;
        }
        String json;
        try {
            json = objectMapper.writeValueAsString(notification);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize notification for SSE: {}", e.getMessage());
            return;
        }
        for (UserSubscription sub : subscriptions) {
            if (!sub.userId.equals(userId)) {
                continue;
            }
            try {
                sub.emitter.send(SseEmitter.event().name("notification").data(json));
            } catch (IOException e) {
                subscriptions.remove(sub);
                sub.emitter.completeWithError(e);
            }
        }
    }

    private record UserSubscription(UUID userId, SseEmitter emitter) {}
}
