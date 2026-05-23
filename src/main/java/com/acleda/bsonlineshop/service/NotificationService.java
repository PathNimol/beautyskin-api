package com.acleda.bsonlineshop.service;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.notification.NotificationResponse;
import java.util.UUID;

public interface NotificationService {
    PageResponse<NotificationResponse> list(int page, int limit);

    NotificationResponse markRead(UUID id);

    void markAllRead();

    void delete(UUID id);
    void notifyAdmins(String title, String message);

    void notifyUser(UUID userId, String title, String message);
}
