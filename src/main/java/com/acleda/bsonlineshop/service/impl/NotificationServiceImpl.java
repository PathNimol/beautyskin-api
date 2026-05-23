package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.notification.NotificationResponse;
import com.acleda.bsonlineshop.entity.AppNotification;
import com.acleda.bsonlineshop.entity.User;
import com.acleda.bsonlineshop.enums.NotificationType;
import com.acleda.bsonlineshop.enums.UserRole;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.mapper.NotificationMapper;
import com.acleda.bsonlineshop.repository.AppNotificationRepository;
import com.acleda.bsonlineshop.repository.UserRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import com.acleda.bsonlineshop.service.NotificationService;

import java.time.Instant;
import java.util.List;
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
    public void notifyUser(UUID userId, String title, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        AppNotification n = new AppNotification();
        n.setUser(user);
        n.setTitle(title);
        n.setMessage(message);
        n.setRead(false);
        n.setCreatedAt(Instant.now());
        notificationRepository.save(n);
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

    @Override
    public void notifyAdmins(String title, String message) {
        List<User> admins = userRepository.findByRole(UserRole.ADMIN);
        List<AppNotification> notifications = admins.stream()
                .map(admin -> {
                    AppNotification n = new AppNotification();
                    n.setUser(admin);
                    n.setTitle(title);
                    n.setMessage(message);
                    n.setRead(false);
                    n.setCreatedAt(Instant.now());
                    n.setType(NotificationType.SHOP_APPROVAL);
                    return n;
                })
                .toList();
        notificationRepository.saveAll(notifications);
    }
}
