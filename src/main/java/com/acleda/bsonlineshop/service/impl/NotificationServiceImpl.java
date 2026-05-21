package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.notification.NotificationResponse;
import com.acleda.bsonlineshop.entity.AppNotification;
import com.acleda.bsonlineshop.entity.User;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.mapper.NotificationMapper;
import com.acleda.bsonlineshop.repository.AppNotificationRepository;
import com.acleda.bsonlineshop.repository.UserRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import com.acleda.bsonlineshop.service.NotificationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final AppNotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> list(int page, int limit) {
        User user = currentUser();
        return PageResponse.from(notificationRepository
                .findByUserAndDeletedFalseOrderByCreatedAtDesc(user, PageRequest.of(Math.max(page - 1, 0), limit))
                .map(notificationMapper::toResponse));
    }

    @Override
    @Transactional
    public NotificationResponse markRead(UUID id) {
        AppNotification n = notificationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        n.setRead(true);
        return notificationMapper.toResponse(notificationRepository.save(n));
    }

    @Override
    @Transactional
    public void markAllRead() {
        User user = currentUser();
        notificationRepository.findByUserAndReadFalseAndDeletedFalse(user).forEach(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        AppNotification n = notificationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        n.setDeleted(true);
        notificationRepository.save(n);
    }

    private User currentUser() {
        return userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
