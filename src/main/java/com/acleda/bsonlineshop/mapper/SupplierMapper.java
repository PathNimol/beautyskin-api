package com.acleda.bsonlineshop.mapper;

import com.acleda.bsonlineshop.dto.supplier.SupplierCreateRequest;
import com.acleda.bsonlineshop.dto.supplier.SupplierResponse;
import com.acleda.bsonlineshop.dto.supplier.SupplierUpdateRequest;
import com.acleda.bsonlineshop.entity.Supplier;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {

    public SupplierResponse toResponse(Supplier s) {
        return SupplierResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .contactPerson(s.getContactPerson())
                .email(s.getEmail())
                .phone(s.getPhone())
                .address(s.getAddress())
                .country(s.getCountry())
                .category(s.getCategory())
                .totalOrders(s.getTotalOrders())
                .totalSpent(s.getTotalSpent())
                .rating(s.getRating())
                .status(s.getStatus())
                .joinDate(s.getJoinDate())
                .lastOrder(s.getLastOrder())
                .logo(s.getLogo())
                .logoAlt(s.getLogoAlt())
                .createdAt(s.getCreatedAt())
                .build();
    }

    public void applyCreate(Supplier s, SupplierCreateRequest req) {
        s.setName(req.getName());
        s.setContactPerson(req.getContactPerson());
        s.setEmail(req.getEmail());
        s.setPhone(req.getPhone());
        s.setAddress(req.getAddress());
        s.setCountry(req.getCountry());
        s.setCategory(req.getCategory());
        s.setLogo(req.getLogo());
        s.setLogoAlt(req.getLogoAlt());
    }

    public void applyUpdate(Supplier s, SupplierUpdateRequest req) {
        if (req.getName() != null) s.setName(req.getName());
        if (req.getContactPerson() != null) s.setContactPerson(req.getContactPerson());
        if (req.getEmail() != null) s.setEmail(req.getEmail());
        if (req.getPhone() != null) s.setPhone(req.getPhone());
        if (req.getAddress() != null) s.setAddress(req.getAddress());
        if (req.getCountry() != null) s.setCountry(req.getCountry());
        if (req.getCategory() != null) s.setCategory(req.getCategory());
        if (req.getLogo() != null) s.setLogo(req.getLogo());
        if (req.getLogoAlt() != null) s.setLogoAlt(req.getLogoAlt());
        if (req.getStatus() != null) s.setStatus(req.getStatus());
    }
}
