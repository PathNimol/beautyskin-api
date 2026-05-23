package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.shop.ShopNameChangeCreateRequest;
import com.acleda.bsonlineshop.dto.shop.ShopNameChangeResponse;
import com.acleda.bsonlineshop.dto.shop.ShopNameChangeReviewRequest;
import com.acleda.bsonlineshop.entity.Shop;
import com.acleda.bsonlineshop.entity.ShopNameChangeRequest;
import com.acleda.bsonlineshop.enums.NotificationType;
import com.acleda.bsonlineshop.enums.RevokeRequestStatus;
import com.acleda.bsonlineshop.enums.UserRole;
import com.acleda.bsonlineshop.exception.BusinessException;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.mapper.ShopNameChangeMapper;
import com.acleda.bsonlineshop.repository.ShopNameChangeRequestRepository;
import com.acleda.bsonlineshop.repository.ShopRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import com.acleda.bsonlineshop.service.NotificationService;
import com.acleda.bsonlineshop.service.ShopNameChangeService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShopNameChangeServiceImpl implements ShopNameChangeService {

    private final ShopNameChangeRequestRepository requestRepository;
    private final ShopRepository shopRepository;
    private final ShopNameChangeMapper mapper;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ShopNameChangeResponse> list(UUID shopId, String status, int page, int limit) {
        RevokeRequestStatus requestStatus = parseStatus(status);
        UserRole role = SecurityUtils.currentRole();
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), limit);

        if (role == UserRole.ADMIN) {
            if (shopId != null) {
                return PageResponse.from(requestRepository
                        .findByShop(shopId, requestStatus, pageable)
                        .map(mapper::toResponse));
            }
            return PageResponse.from(requestRepository
                    .findAllByStatus(requestStatus, pageable)
                    .map(mapper::toResponse));
        }

        UUID resolvedShopId = resolveOwnerShopId(shopId);
        requireShopOwner(resolvedShopId);
        return PageResponse.from(requestRepository
                .findByShop(resolvedShopId, requestStatus, pageable)
                .map(mapper::toResponse));
    }

    @Override
    @Transactional
    public ShopNameChangeResponse create(UUID shopId, ShopNameChangeCreateRequest request) {
        UUID resolvedShopId = resolveOwnerShopId(shopId);
        Shop shop = shopRepository.findByIdAndDeletedFalse(resolvedShopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        requireShopOwner(shop.getId());

        String requestedName = request.getRequestedName().trim();
        if (requestedName.isBlank()) {
            throw new BusinessException("Requested name cannot be empty");
        }
        if (requestedName.equalsIgnoreCase(shop.getName())) {
            throw new BusinessException("Requested name is the same as the current shop name");
        }
        if (requestRepository.existsByShop_IdAndStatusAndDeletedFalse(shop.getId(), RevokeRequestStatus.PENDING)) {
            throw new BusinessException("A name change request is already pending for this shop");
        }

        ShopNameChangeRequest entity = new ShopNameChangeRequest();
        entity.setShop(shop);
        entity.setCurrentName(shop.getName());
        entity.setRequestedName(requestedName);
        entity.setStatus(RevokeRequestStatus.PENDING);
        entity.setRequestedBy(SecurityUtils.currentUserId());
        ShopNameChangeRequest saved = requestRepository.save(entity);

        notificationService.notifyAdmins(
                "Shop name change request",
                shop.getOwnerName() + " requested to rename \"" + shop.getName() + "\" to \"" + requestedName + "\".",
                NotificationType.SHOP_NAME_CHANGE,
                "/admin/shops?nameRequests=1",
                shop.getId());

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ShopNameChangeResponse review(UUID id, ShopNameChangeReviewRequest request) {
        if (request.getStatus() != RevokeRequestStatus.APPROVED
                && request.getStatus() != RevokeRequestStatus.REJECTED) {
            throw new BusinessException("Review status must be APPROVED or REJECTED");
        }

        ShopNameChangeRequest entity = requestRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
        if (entity.getStatus() != RevokeRequestStatus.PENDING) {
            throw new BusinessException("This request has already been reviewed");
        }

        entity.setStatus(request.getStatus());
        entity.setReviewNotes(request.getNotes());
        entity.setReviewedBy(SecurityUtils.currentUser().getEmail());

        Shop shop = entity.getShop();
        if (request.getStatus() == RevokeRequestStatus.APPROVED) {
            shop.setName(entity.getRequestedName());
            shop.setSlug(uniqueSlug(entity.getRequestedName(), shop.getId()));
            shopRepository.save(shop);
            if (shop.getOwnerId() != null) {
                notificationService.notifyUser(
                        shop.getOwnerId(),
                        "Shop name approved",
                        "Your shop name has been updated to \"" + entity.getRequestedName() + "\".",
                        NotificationType.SHOP_NAME_CHANGE,
                        "/owner/shops",
                        shop.getId());
            }
        } else if (shop.getOwnerId() != null) {
            notificationService.notifyUser(
                    shop.getOwnerId(),
                    "Shop name change rejected",
                    "Your request to rename \"" + entity.getCurrentName() + "\" to \""
                            + entity.getRequestedName() + "\" was not approved."
                            + (request.getNotes() != null && !request.getNotes().isBlank()
                                    ? " Note: " + request.getNotes()
                                    : ""),
                    NotificationType.SHOP_NAME_CHANGE,
                    "/owner/shops",
                    shop.getId());
        }

        return mapper.toResponse(requestRepository.save(entity));
    }

    private UUID resolveOwnerShopId(UUID shopId) {
        if (shopId != null) {
            return shopId;
        }
        UUID fromPrincipal = SecurityUtils.currentUser().getShopId();
        if (fromPrincipal == null) {
            throw new BusinessException("shopId is required");
        }
        return fromPrincipal;
    }

    private void requireShopOwner(UUID shopId) {
        if (SecurityUtils.currentRole() == UserRole.ADMIN) {
            return;
        }
        Shop shop = shopRepository.findByIdAndDeletedFalse(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        if (!SecurityUtils.currentUserId().equals(shop.getOwnerId())) {
            throw new BusinessException("Only the shop owner can manage name change requests for this shop");
        }
    }

    private static RevokeRequestStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return RevokeRequestStatus.valueOf(status.toUpperCase());
    }

    private String uniqueSlug(String name, UUID shopId) {
        String base = name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
        if (base.isBlank()) {
            base = "shop";
        }
        String candidate = base;
        int suffix = 0;
        while (shopRepository.findBySlugAndDeletedFalse(candidate)
                .filter(s -> !s.getId().equals(shopId))
                .isPresent()) {
            candidate = base + "-" + (++suffix);
        }
        return candidate;
    }
}
