package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.entity.AppNotification;
import com.acleda.bsonlineshop.entity.User;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.repository.AppNotificationRepository;
import com.acleda.bsonlineshop.repository.UserRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import java.util.UUID;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final AppNotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<PageResponse<AppNotification>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        User user = userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ApiResponse.success(PageResponse.from(
                notificationRepository.findByUserAndDeletedFalseOrderByCreatedAtDesc(
                        user, PageRequest.of(Math.max(page - 1, 0), limit))));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<AppNotification> markRead(@PathVariable UUID id) {
        AppNotification n = notificationRepository.findById(id).filter(x -> !x.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        n.setRead(true);
        return ApiResponse.success(notificationRepository.save(n));
    }
}
