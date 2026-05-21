package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.shopstaff.ShopStaffCreateRequest;
import com.acleda.bsonlineshop.dto.shopstaff.ShopStaffResponse;
import com.acleda.bsonlineshop.dto.shopstaff.ShopStaffUpdateRequest;
import com.acleda.bsonlineshop.entity.Shop;
import com.acleda.bsonlineshop.entity.ShopStaff;
import com.acleda.bsonlineshop.enums.AccountStatus;
import com.acleda.bsonlineshop.enums.ShopUserRole;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.mapper.ShopStaffMapper;
import com.acleda.bsonlineshop.repository.ShopRepository;
import com.acleda.bsonlineshop.repository.ShopStaffRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import com.acleda.bsonlineshop.service.ShopStaffService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShopStaffServiceImpl implements ShopStaffService {

    private final ShopStaffRepository staffRepository;
    private final ShopRepository shopRepository;
    private final ShopStaffMapper shopStaffMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ShopStaffResponse> list(
            UUID shopId, String role, String status, String search, int page, int limit) {
        ShopUserRole shopRole = role != null ? ShopUserRole.valueOf(role.toUpperCase()) : null;
        AccountStatus accountStatus = status != null ? AccountStatus.valueOf(status.toUpperCase()) : null;
        var result = staffRepository.search(
                SecurityUtils.requireShopId(shopId), shopRole, accountStatus, search,
                PageRequest.of(Math.max(page - 1, 0), limit));
        return PageResponse.from(result.map(shopStaffMapper::toResponse));
    }

    @Override
    @Transactional
    public ShopStaffResponse create(UUID shopId, ShopStaffCreateRequest request) {
        Shop shop = shopRepository.findByIdAndDeletedFalse(SecurityUtils.requireShopId(shopId))
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        ShopStaff staff = new ShopStaff();
        shopStaffMapper.applyCreate(staff, shop, request);
        return shopStaffMapper.toResponse(staffRepository.save(staff));
    }

    @Override
    @Transactional
    public ShopStaffResponse update(UUID shopId, UUID userId, ShopStaffUpdateRequest request) {
        UUID scopedShopId = SecurityUtils.requireShopId(shopId);
        ShopStaff staff = loadStaff(userId, scopedShopId);
        shopStaffMapper.applyUpdate(staff, request);
        return shopStaffMapper.toResponse(staffRepository.save(staff));
    }

    @Override
    @Transactional
    public void delete(UUID shopId, UUID userId) {
        UUID scopedShopId = SecurityUtils.requireShopId(shopId);
        ShopStaff staff = loadStaff(userId, scopedShopId);
        staff.setDeleted(true);
        staffRepository.save(staff);
    }

    private ShopStaff loadStaff(UUID userId, UUID scopedShopId) {
        ShopStaff staff = staffRepository.findById(userId).filter(s -> !s.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!staff.getShop().getId().equals(scopedShopId)) {
            throw new ResourceNotFoundException("User not found");
        }
        return staff;
    }
}
