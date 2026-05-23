package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.revoke.RevokeCreateRequest;
import com.acleda.bsonlineshop.dto.revoke.RevokeRequestResponse;
import com.acleda.bsonlineshop.dto.revoke.RevokeReviewRequest;
import com.acleda.bsonlineshop.entity.Product;
import com.acleda.bsonlineshop.entity.ProductRevokeRequest;
import com.acleda.bsonlineshop.entity.Shop;
import com.acleda.bsonlineshop.enums.RevokeRequestStatus;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.mapper.RevokeRequestMapper;
import com.acleda.bsonlineshop.repository.ProductRepository;
import com.acleda.bsonlineshop.repository.ProductRevokeRequestRepository;
import com.acleda.bsonlineshop.repository.ShopRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
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
        ProductRevokeRequest req = new ProductRevokeRequest();
        req.setShop(shop);
        req.setProduct(product);
        req.setSku(product.getSku());
        req.setQuantity(request.getQuantity());
        req.setReason(request.getReason());
        req.setDetail(request.getDetail() != null ? request.getDetail() : "");
        req.setStatus(RevokeRequestStatus.PENDING);
        return revokeRequestMapper.toResponse(revokeRepository.save(req));
    }

    @Override
    @Transactional
    public RevokeRequestResponse review(UUID id, RevokeReviewRequest request) {
        ProductRevokeRequest req = revokeRepository.findById(id).filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
        req.setStatus(request.getStatus());
        req.setReviewNotes(request.getNotes());
        req.setReviewedBy(SecurityUtils.currentUser().getEmail());
        if (request.getStatus() == RevokeRequestStatus.APPROVED) {
            Product product = req.getProduct();
            product.setRevoked(true);
            product.setVisible(false);
            productRepository.save(product);
        }
        return revokeRequestMapper.toResponse(revokeRepository.save(req));
    }
}
