package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.entity.InventoryItem;
import com.acleda.bsonlineshop.enums.InventoryStatus;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.repository.InventoryItemRepository;
import com.acleda.bsonlineshop.repository.ShopRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

    private final InventoryItemRepository inventoryRepository;
    private final ShopRepository shopRepository;

    @GetMapping
    public ApiResponse<PageResponse<InventoryItem>> list(
            @RequestParam UUID shopId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        UUID scoped = SecurityUtils.requireShopId(shopId);
        InventoryStatus invStatus = status != null && !status.isBlank()
                ? InventoryStatus.valueOf(status.toUpperCase().replace(' ', '_'))
                : null;
        var result = inventoryRepository.search(scoped, invStatus, search, PageRequest.of(Math.max(page - 1, 0), limit));
        return ApiResponse.success(PageResponse.from(result));
    }

    @PatchMapping("/{id}/restock")
    public ApiResponse<InventoryItem> restock(@PathVariable UUID id, @RequestBody Map<String, Integer> body) {
        InventoryItem item = inventoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));
        int qty = body.getOrDefault("quantity", 0);
        item.setCurrentStock(item.getCurrentStock() + qty);
        item.setLastRestocked(LocalDate.now());
        if (item.getCurrentStock() <= item.getReorderPoint()) {
            item.setInvStatus(InventoryStatus.LOW);
        } else {
            item.setInvStatus(InventoryStatus.HEALTHY);
        }
        return ApiResponse.success("Restocked", inventoryRepository.save(item));
    }
}
