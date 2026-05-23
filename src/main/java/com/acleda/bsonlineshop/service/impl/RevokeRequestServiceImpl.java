package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.revoke.RevokeCreateRequest;
import com.acleda.bsonlineshop.dto.revoke.RevokeRequestResponse;
import com.acleda.bsonlineshop.dto.revoke.RevokeReviewRequest;
import com.acleda.bsonlineshop.entity.Product;
import com.acleda.bsonlineshop.entity.ProductRevokeRequest;
import com.acleda.bsonlineshop.entity.Shop;
import com.acleda.bsonlineshop.entity.User;
import com.acleda.bsonlineshop.enums.NotificationType;
import com.acleda.bsonlineshop.enums.RevokeRequestStatus;
import com.acleda.bsonlineshop.enums.UserRole;
import com.acleda.bsonlineshop.exception.BusinessException;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.mapper.RevokeRequestMapper;
import com.acleda.bsonlineshop.repository.ProductRepository;
import com.acleda.bsonlineshop.repository.ProductRevokeRequestRepository;
import com.acleda.bsonlineshop.repository.ShopRepository;
import com.acleda.bsonlineshop.repository.UserRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import com.acleda.bsonlineshop.service.NotificationService;
import com.acleda.bsonlineshop.service.RevokeRequestService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RevokeRequestServiceImpl implements RevokeRequestService {

    private final ProductRevokeRequestRepository revokeRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final RevokeRequestMapper revokeRequestMapper;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RevokeRequestResponse> list(UUID shopId, String status, int page, int limit) {
        RevokeRequestStatus rs = status != null ? RevokeRequestStatus.valueOf(status.toUpperCase()) : null;
        return PageResponse.from(revokeRepository
                .findByShop(SecurityUtils.requireShopId(shopId), rs, PageRequest.of(Math.max(page - 1, 0), limit))
                .map(revokeRequestMapper::toResponse));
    }

    @Override
    @Transactional
    public RevokeRequestResponse create(UUID shopId, RevokeCreateRequest request) {
        Shop shop = shopRepository.findByIdAndDeletedFalse(SecurityUtils.requireShopId(shopId))
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        Product product = productRepository.findByIdAndDeletedFalse(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (product.getShop() == null || !product.getShop().getId().equals(shop.getId())) {
            throw new BusinessException("Product does not belong to this shop");
        }

        ProductRevokeRequest req = new ProductRevokeRequest();
        req.setShop(shop);
        req.setProduct(product);
        req.setSku(product.getSku());
        req.setQuantity(request.getQuantity());
        req.setReason(request.getReason());
        req.setDetail(request.getDetail() != null ? request.getDetail() : "");
        req.setStatus(RevokeRequestStatus.PENDING);
        UUID requesterId = SecurityUtils.currentUserId();
        req.setRequestedBy(requesterId);
        req.setRequesterEmail(SecurityUtils.currentUser().getEmail());
        userRepository.findById(requesterId).ifPresent(u -> req.setRequesterName(formatUserName(u)));
        ProductRevokeRequest saved = revokeRepository.save(req);

        String requesterLabel = req.getRequesterName() != null && !req.getRequesterName().isBlank()
                ? req.getRequesterName()
                : req.getRequesterEmail();
        notificationService.notifyShopOwners(
                shop.getId(),
                "New product revoke request",
                requesterLabel
                        + " requested to revoke "
                        + request.getQuantity()
                        + " unit(s) of \""
                        + product.getName()
                        + "\".",
                NotificationType.PRODUCT_REVOKE,
                "/owner/revoke-requests");

        return revokeRequestMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public RevokeRequestResponse review(UUID id, RevokeReviewRequest request) {
        if (request.getStatus() != RevokeRequestStatus.APPROVED
                && request.getStatus() != RevokeRequestStatus.REJECTED) {
            throw new BusinessException("Review status must be APPROVED or REJECTED");
        }

        UserRole role = SecurityUtils.currentRole();
        if (role != UserRole.OWNER && role != UserRole.ADMIN) {
            throw new BusinessException("Only shop owners can review revoke requests");
        }

        ProductRevokeRequest req = revokeRepository.findById(id).filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
        if (role != UserRole.ADMIN) {
            SecurityUtils.requireShopId(req.getShop().getId());
        }
        if (req.getStatus() != RevokeRequestStatus.PENDING) {
            throw new BusinessException("This request has already been reviewed");
        }

        req.setStatus(request.getStatus());
        req.setReviewNotes(request.getNotes());
        req.setReviewedBy(SecurityUtils.currentUser().getEmail());
        if (request.getStatus() == RevokeRequestStatus.APPROVED) {
            Product product = req.getProduct();
            product.setRevoked(true);
            product.setVisible(false);
            productRepository.save(product);
        }
        ProductRevokeRequest saved = revokeRepository.save(req);

        UUID shopId = req.getShop() != null ? req.getShop().getId() : null;
        String productName = req.getProduct() != null ? req.getProduct().getName() : "product";
        if (req.getRequestedBy() != null) {
            String title = request.getStatus() == RevokeRequestStatus.APPROVED
                    ? "Revoke request approved"
                    : "Revoke request rejected";
            String message = request.getStatus() == RevokeRequestStatus.APPROVED
                    ? "Your request to revoke \"" + productName + "\" was approved."
                    : "Your request to revoke \"" + productName + "\" was rejected."
                            + (request.getNotes() != null && !request.getNotes().isBlank()
                                    ? " Note: " + request.getNotes()
                                    : "");
            notificationService.notifyUser(
                    req.getRequestedBy(),
                    title,
                    message,
                    NotificationType.PRODUCT_REVOKE,
                    "/staff/revoke-requests",
                    shopId);
        }

        return revokeRequestMapper.toResponse(saved);
    }

    private static String formatUserName(User user) {
        String first = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String last = user.getLastName() != null ? user.getLastName().trim() : "";
        String full = (first + " " + last).trim();
        return full.isEmpty() ? user.getEmail() : full;
    }
}
