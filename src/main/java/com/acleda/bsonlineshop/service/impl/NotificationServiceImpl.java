package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.notification.NotificationResponse;
import com.acleda.bsonlineshop.entity.AppNotification;
import com.acleda.bsonlineshop.entity.User;
import com.acleda.bsonlineshop.entity.UserPreferences;
import com.acleda.bsonlineshop.enums.NotificationType;
import com.acleda.bsonlineshop.enums.UserRole;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.mapper.NotificationMapper;
import com.acleda.bsonlineshop.repository.AppNotificationRepository;
import com.acleda.bsonlineshop.repository.ShopRepository;
import com.acleda.bsonlineshop.repository.UserPreferencesRepository;
import com.acleda.bsonlineshop.repository.UserRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import com.acleda.bsonlineshop.service.NotificationService;
import com.acleda.bsonlineshop.service.NotificationStreamHub;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private final UserPreferencesRepository preferencesRepository;
    private final ShopRepository shopRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationStreamHub notificationStreamHub;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> list(int page, int limit) {
        User user = currentUser();
        return PageResponse.from(notificationRepository
                .findByUserAndDeletedFalseOrderByCreatedAtDesc(user, PageRequest.of(Math.max(page - 1, 0), limit))
                .map(notificationMapper::toResponse));
    }

    @Override
    public void notifyAdmins(String title, String message) {
        notifyAdmins(title, message, NotificationType.SHOP_APPROVAL, null, null);
    }

    @Override
    public void notifyUser(UUID userId, String title, String message) {
        notifyUser(userId, title, message, NotificationType.SHOP_APPROVAL, null, null);
    }

    @Override
    public void notifyAdmins(String title, String message, NotificationType type, String link, UUID shopId) {
        List<User> admins = userRepository.findByRole(UserRole.ADMIN);
        for (User admin : admins) {
            deliverIfAllowed(admin, title, message, type, link, shopId);
        }
    }

    @Override
    public void notifyUser(UUID userId, String title, String message, NotificationType type, String link, UUID shopId) {
        userRepository.findById(userId).ifPresent(user -> deliverIfAllowed(user, title, message, type, link, shopId));
    }

    @Override
    public void notifyShopOwners(
            UUID shopId, String title, String message, NotificationType type, String link) {
        if (shopId == null) {
            return;
        }
        Set<UUID> notified = new HashSet<>();
        userRepository.findByShopIdAndRoleAndDeletedFalse(shopId, UserRole.OWNER).forEach(owner -> {
            if (owner.getId() != null && notified.add(owner.getId())) {
                deliverIfAllowed(owner, title, message, type, link, shopId);
            }
        });
        shopRepository.findByIdAndDeletedFalse(shopId).ifPresent(shop -> {
            UUID ownerId = shop.getOwnerId();
            if (ownerId != null && notified.add(ownerId)) {
                userRepository.findById(ownerId).ifPresent(owner -> deliverIfAllowed(owner, title, message, type, link, shopId));
            }
        });
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

    private void deliverIfAllowed(
            User user, String title, String message, NotificationType type, String link, UUID shopId) {
        if (!allowsInAppNotification(user, type)) {
            return;
        }
        AppNotification saved = notificationRepository.save(
                buildNotification(user, title, message, type, link, shopId));
        notificationStreamHub.publish(user.getId(), notificationMapper.toResponse(saved));
    }

    private boolean allowsInAppNotification(User user, NotificationType type) {
        UserPreferences prefs = preferencesRepository.findByUserAndDeletedFalse(user).orElse(null);
        if (prefs == null) {
            return true;
        }
        if (type == null) {
            return true;
        }
        return switch (type) {
            case NEW_ORDER -> prefs.isOrderUpdates();
            case LOW_STOCK -> prefs.isLowStockAlerts();
            case EXPIRY_ALERT -> prefs.isExpiryAlerts();
            case REVIEW -> prefs.isReviewAlerts();
            case PROMOTION -> prefs.isPromotions();
            case PRODUCT_REVOKE, SHOP_APPROVAL, SHOP_NAME_CHANGE, SYSTEM -> true;
        };
    }

    private User currentUser() {
        return userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private static AppNotification buildNotification(
            User user, String title, String message, NotificationType type, String link, UUID shopId) {
        AppNotification n = new AppNotification();
        n.setUser(user);
        n.setTitle(title);
        n.setMessage(message);
        n.setRead(false);
        n.setCreatedAt(Instant.now());
        n.setType(type != null ? type : NotificationType.SYSTEM);
        n.setLink(link);
        n.setShopId(shopId);
        return n;
    }
}
