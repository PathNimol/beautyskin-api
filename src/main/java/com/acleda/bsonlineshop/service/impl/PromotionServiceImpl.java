package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.promotion.PromotionCreateRequest;
import com.acleda.bsonlineshop.dto.promotion.PromotionResponse;
import com.acleda.bsonlineshop.dto.promotion.PromotionUpdateRequest;
import com.acleda.bsonlineshop.entity.Promotion;
import com.acleda.bsonlineshop.entity.Shop;
import com.acleda.bsonlineshop.enums.PromotionStatus;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.mapper.PromotionMapper;
import com.acleda.bsonlineshop.repository.PromotionRepository;
import com.acleda.bsonlineshop.repository.ShopRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import com.acleda.bsonlineshop.service.PromotionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final ShopRepository shopRepository;
    private final PromotionMapper promotionMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PromotionResponse> list(UUID shopId, String status, String search, int page, int limit) {
        PromotionStatus ps = status != null && !status.isBlank() ? PromotionStatus.valueOf(status.toUpperCase()) : null;
        var result = promotionRepository.search(
                SecurityUtils.requireShopId(shopId), ps, search, PageRequest.of(Math.max(page - 1, 0), limit));
        return PageResponse.from(result.map(promotionMapper::toResponse));
    }

    @Override
    @Transactional
    public PromotionResponse create(UUID shopId, PromotionCreateRequest request) {
        Shop shop = shopRepository.findByIdAndDeletedFalse(SecurityUtils.requireShopId(shopId))
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        Promotion p = new Promotion();
        promotionMapper.applyCreate(p, shop, request);
        return promotionMapper.toResponse(promotionRepository.save(p));
    }

    @Override
    @Transactional
    public PromotionResponse update(UUID id, PromotionUpdateRequest request) {
        Promotion p = load(id);
        promotionMapper.applyUpdate(p, request);
        return promotionMapper.toResponse(promotionRepository.save(p));
    }

    @Override
    @Transactional
    public PromotionResponse updateStatus(UUID id, PromotionStatus status) {
        Promotion p = load(id);
        p.setStatus(status);
        return promotionMapper.toResponse(promotionRepository.save(p));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Promotion p = load(id);
        p.setDeleted(true);
        promotionRepository.save(p);
    }

    private Promotion load(UUID id) {
        return promotionRepository.findById(id).filter(x -> !x.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
    }
}
