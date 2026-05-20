package com.acleda.bsonlineshop.service;

import com.acleda.bsonlineshop.dto.common.ShippingAddressDto;
import com.acleda.bsonlineshop.dto.user.UpdateProfileRequest;
import com.acleda.bsonlineshop.dto.user.UserResponse;

public interface UserService {
    UserResponse getCurrentUser();
    UserResponse updateProfile(UpdateProfileRequest request);
    ShippingAddressDto getShipping();
    ShippingAddressDto updateShipping(ShippingAddressDto dto);
}
