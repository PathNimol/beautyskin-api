package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.shop.ShopCreateRequest;
import com.acleda.bsonlineshop.dto.shop.ShopResponse;
import com.acleda.bsonlineshop.entity.Shop;
import com.acleda.bsonlineshop.enums.NotificationType;
import com.acleda.bsonlineshop.enums.ShopPlan;
import com.acleda.bsonlineshop.enums.ShopStatus;
import com.acleda.bsonlineshop.enums.UserRole;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.repository.ShopRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import com.acleda.bsonlineshop.service.NotificationService;
import com.acleda.bsonlineshop.service.ShopService;

import java.util.UUID;

import com.acleda.bsonlineshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ShopResponse> list(String status, String search, int page, int limit) {
        ShopStatus shopStatus = status != null && !status.isBlank()
                ? ShopStatus.valueOf(status.toUpperCase())
                : null;
        Page<Shop> result = shopRepository.search(shopStatus, search, PageRequest.of(Math.max(page - 1, 0), limit));
        return PageResponse.from(result.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ShopResponse getById(UUID id) {
        return toResponse(shopRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found")));
    }

    @Override
    @Transactional
    public ShopResponse create(ShopCreateRequest request) {
        Shop shop = new Shop();
        shop.setName(request.getName());
        shop.setSlug(request.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-"));
        shop.setDescription(request.getDescription());
        shop.setCategory(request.getCategory());
        shop.setLogo(request.getLogo());
        shop.setLogoAlt(request.getLogoAlt());
        shop.setPlan(request.getPlan() != null ? request.getPlan() : ShopPlan.STARTER);

        // Admin-created → active immediately; owner-created → pending for approval
        UserRole currentUserRole = userService.getCurrentUser().getRole();
        if (currentUserRole == UserRole.ADMIN) {
            shop.setStatus(ShopStatus.ACTIVE);
        } else {
            shop.setStatus(ShopStatus.PENDING);
        }

        shop.setOwnerId(SecurityUtils.currentUserId());
        shop.setOwnerName(request.getOwnerName() != null ? request.getOwnerName() : SecurityUtils.currentUser().getUsername());

        Shop saved = shopRepository.save(shop);

        // Notify admins when an owner registers a new shop
        if (saved.getStatus() == ShopStatus.PENDING) {
            notificationService.notifyAdmins(
                    "New shop approval request",
                    saved.getOwnerName() + " registered \"" + saved.getName() + "\" and is awaiting approval.",
                    NotificationType.SHOP_APPROVAL,
                    "/admin/shops?pending=1",
                    saved.getId());
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public ShopResponse updateStatus(UUID id, ShopStatus status) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        shop.setStatus(status);
        Shop saved = shopRepository.save(shop);

        // Notify owner when admin approves or rejects
        if (status == ShopStatus.ACTIVE) {
            notificationService.notifyUser(
                    saved.getOwnerId(),
                    "🎉 Shop Approved!",
                    "Your shop \"" + saved.getName() + "\" has been approved and is now live.",
                    NotificationType.SHOP_APPROVAL,
                    "/owner/shops",
                    saved.getId());
        } else if (status == ShopStatus.SUSPENDED) {
            notificationService.notifyUser(
                    saved.getOwnerId(),
                    "Shop Registration Rejected",
                    "Your shop \"" + saved.getName() + "\" was not approved. Please contact support for more information.",
                    NotificationType.SHOP_APPROVAL,
                    "/owner/shops",
                    saved.getId());
        }

        return toResponse(saved);
    }

    private ShopResponse toResponse(Shop s) {
        return ShopResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .slug(s.getSlug())
                .ownerId(s.getOwnerId())
                .ownerName(s.getOwnerName())
                .logo(s.getLogo())
                .logoAlt(s.getLogoAlt())
                .description(s.getDescription())
                .status(s.getStatus())
                .plan(s.getPlan())
                .revenue(s.getRevenue())
                .ordersCount(s.getOrdersCount())
                .productsCount(s.getProductsCount())
                .customersCount(s.getCustomersCount())
                .category(s.getCategory())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
