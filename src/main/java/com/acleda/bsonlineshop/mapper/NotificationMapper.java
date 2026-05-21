package com.acleda.bsonlineshop.mapper;

import com.acleda.bsonlineshop.dto.notification.NotificationResponse;
import com.acleda.bsonlineshop.entity.AppNotification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(AppNotification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .shopId(n.getShopId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .read(n.isRead())
                .link(n.getLink())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
