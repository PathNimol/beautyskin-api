package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.entity.Shop;
import com.acleda.bsonlineshop.entity.ShopStaff;
import com.acleda.bsonlineshop.enums.AccountStatus;
import com.acleda.bsonlineshop.enums.ShopUserRole;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.repository.ShopRepository;
import com.acleda.bsonlineshop.repository.ShopStaffRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shops/{shopId}/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ShopUserController {

    private final ShopStaffRepository staffRepository;
    private final ShopRepository shopRepository;

    @GetMapping
    public ApiResponse<PageResponse<ShopStaff>> list(
            @PathVariable UUID shopId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        ShopUserRole shopRole = role != null ? ShopUserRole.valueOf(role.toUpperCase()) : null;
        AccountStatus accountStatus = status != null ? AccountStatus.valueOf(status.toUpperCase()) : null;
        var result = staffRepository.search(
                SecurityUtils.requireShopId(shopId), shopRole, accountStatus, search,
                PageRequest.of(Math.max(page - 1, 0), limit));
        return ApiResponse.success(PageResponse.from(result));
    }

    @PostMapping
    public ApiResponse<ShopStaff> create(@PathVariable UUID shopId, @RequestBody ShopStaff staff) {
        Shop shop = shopRepository.findByIdAndDeletedFalse(SecurityUtils.requireShopId(shopId))
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        staff.setShop(shop);
        return ApiResponse.success("User added", staffRepository.save(staff));
    }

    @PutMapping("/{userId}")
    public ApiResponse<ShopStaff> update(@PathVariable UUID shopId, @PathVariable UUID userId, @RequestBody ShopStaff body) {
        ShopStaff staff = staffRepository.findById(userId).filter(s -> !s.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        staff.setName(body.getName());
        staff.setEmail(body.getEmail());
        staff.setPhone(body.getPhone());
        staff.setRole(body.getRole());
        staff.setStatus(body.getStatus());
        return ApiResponse.success(staffRepository.save(staff));
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<Void> delete(@PathVariable UUID shopId, @PathVariable UUID userId) {
        ShopStaff staff = staffRepository.findById(userId).filter(s -> !s.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        staff.setDeleted(true);
        staffRepository.save(staff);
        return ApiResponse.success("User removed", null);
    }
}
