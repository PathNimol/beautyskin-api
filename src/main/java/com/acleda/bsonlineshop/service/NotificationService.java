package com.acleda.bsonlineshop.service;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.notification.NotificationResponse;
import com.acleda.bsonlineshop.enums.NotificationType;
import java.util.UUID;

public interface NotificationService {
    PageResponse<NotificationResponse> list(int page, int limit);

    NotificationResponse markRead(UUID id);

    void markAllRead();

    void delete(UUID id);
    void notifyAdmins(String title, String message);

    void notifyUser(UUID userId, String title, String message);

    void notifyAdmins(String title, String message, NotificationType type, String link, UUID shopId);

    void notifyUser(UUID userId, String title, String message, NotificationType type, String link, UUID shopId);

    /** Notify the shop owner record and any OWNER users assigned to the shop. */
    void notifyShopOwners(UUID shopId, String title, String message, NotificationType type, String link);
}
