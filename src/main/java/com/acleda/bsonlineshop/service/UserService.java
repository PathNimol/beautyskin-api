package com.acleda.bsonlineshop.service;

import com.acleda.bsonlineshop.dto.common.ShippingAddressDto;
import com.acleda.bsonlineshop.dto.user.UpdateProfileRequest;
import com.acleda.bsonlineshop.dto.user.UserResponse;
import com.acleda.bsonlineshop.enums.UserRole;

import java.util.List;

public interface UserService {
    UserResponse getCurrentUser();
    UserResponse updateProfile(UpdateProfileRequest request);
    ShippingAddressDto getShipping();
    ShippingAddressDto updateShipping(ShippingAddressDto dto);

    List<UserResponse> getUsersByRole(UserRole role);
}
