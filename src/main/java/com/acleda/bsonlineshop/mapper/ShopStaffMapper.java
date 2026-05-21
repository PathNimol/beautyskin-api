package com.acleda.bsonlineshop.mapper;

import com.acleda.bsonlineshop.dto.shopstaff.ShopStaffCreateRequest;
import com.acleda.bsonlineshop.dto.shopstaff.ShopStaffResponse;
import com.acleda.bsonlineshop.dto.shopstaff.ShopStaffUpdateRequest;
import com.acleda.bsonlineshop.entity.Shop;
import com.acleda.bsonlineshop.entity.ShopStaff;
import com.acleda.bsonlineshop.enums.AccountStatus;
import org.springframework.stereotype.Component;

@Component
public class ShopStaffMapper {

    public ShopStaffResponse toResponse(ShopStaff s) {
        return ShopStaffResponse.builder()
                .id(s.getId())
                .shopId(s.getShop() != null ? s.getShop().getId() : null)
                .name(s.getName())
                .email(s.getEmail())
                .phone(s.getPhone())
                .role(s.getRole())
                .avatar(s.getAvatar())
                .avatarAlt(s.getAvatarAlt())
                .status(s.getStatus())
                .createdAt(s.getCreatedAt())
                .build();
    }

    public void applyCreate(ShopStaff s, Shop shop, ShopStaffCreateRequest req) {
        s.setShop(shop);
        s.setName(req.getName());
        s.setEmail(req.getEmail());
        s.setPhone(req.getPhone());
        s.setRole(req.getRole());
        s.setAvatar(req.getAvatar());
        s.setAvatarAlt(req.getAvatarAlt());
        s.setStatus(AccountStatus.ACTIVE);
    }

    public void applyUpdate(ShopStaff s, ShopStaffUpdateRequest req) {
        if (req.getName() != null) s.setName(req.getName());
        if (req.getEmail() != null) s.setEmail(req.getEmail());
        if (req.getPhone() != null) s.setPhone(req.getPhone());
        if (req.getRole() != null) s.setRole(req.getRole());
        if (req.getStatus() != null) s.setStatus(req.getStatus());
        if (req.getAvatar() != null) s.setAvatar(req.getAvatar());
        if (req.getAvatarAlt() != null) s.setAvatarAlt(req.getAvatarAlt());
    }
}
