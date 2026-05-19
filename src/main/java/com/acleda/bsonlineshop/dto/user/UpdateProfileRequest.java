package com.acleda.bsonlineshop.dto.user;

import com.acleda.bsonlineshop.dto.common.ShippingAddressDto;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String avatar;
    private String avatarAlt;
    private ShippingAddressDto shipping;
}
