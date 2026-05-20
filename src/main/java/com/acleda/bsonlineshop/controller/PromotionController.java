package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.entity.Promotion;
import com.acleda.bsonlineshop.entity.Shop;
import com.acleda.bsonlineshop.enums.PromotionStatus;
import com.acleda.bsonlineshop.enums.PromotionType;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.repository.PromotionRepository;
import com.acleda.bsonlineshop.repository.ShopRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("@authz.adminOrMerchant()")
public class PromotionController {

    private final PromotionRepository promotionRepository;
    private final ShopRepository shopRepository;

    @Operation(summary = "List promotions", description = "Paginated promotions for a shop with optional status filter and search.")
    @GetMapping
    public ApiResponse<PageResponse<Promotion>> list(
            @RequestParam(required = false) UUID shopId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        PromotionStatus ps = status != null && !status.isBlank() ? PromotionStatus.valueOf(status.toUpperCase()) : null;
        var result = promotionRepository.search(SecurityUtils.requireShopId(shopId), ps, search, PageRequest.of(Math.max(page - 1, 0), limit));
        return ApiResponse.success(PageResponse.from(result));
    }

    @Operation(summary = "Create promotion", description = "Create a shop promotion (code, type, value, dates, limits). Body is a flexible map matching server expectations.")
    @PostMapping
    public ApiResponse<Promotion> create(@RequestParam UUID shopId, @RequestBody Map<String, Object> body) {
        Shop shop = shopRepository.findByIdAndDeletedFalse(SecurityUtils.requireShopId(shopId))
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        Promotion p = new Promotion();
        p.setShop(shop);
        p.setName((String) body.get("name"));
        p.setCode(((String) body.get("code")).toUpperCase());
        p.setType(PromotionType.valueOf(((String) body.get("type")).toUpperCase()));
        p.setValue(new BigDecimal(body.get("value").toString()));
        p.setMinOrder(body.get("minOrder") != null ? new BigDecimal(body.get("minOrder").toString()) : BigDecimal.ZERO);
        p.setMaxUses(body.get("maxUses") != null ? (Integer) body.get("maxUses") : 100);
        p.setStartDate(LocalDate.parse(body.get("startDate").toString()));
        p.setEndDate(LocalDate.parse(body.get("endDate").toString()));
        p.setStatus(PromotionStatus.ACTIVE);
        p.setDescription((String) body.getOrDefault("description", ""));
        return ApiResponse.success("Promotion created", promotionRepository.save(p));
    }

    @Operation(summary = "Update promotion status", description = "Activate, pause, or expire a promotion by id.")
    @PatchMapping("/{id}/status")
    public ApiResponse<Promotion> updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        Promotion p = promotionRepository.findById(id).filter(x -> !x.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
        p.setStatus(PromotionStatus.valueOf(body.get("status").toUpperCase()));
        return ApiResponse.success(promotionRepository.save(p));
    }
}
