package com.acleda.bsonlineshop.dto.user;

import com.acleda.bsonlineshop.dto.common.ShippingAddressDto;
import com.acleda.bsonlineshop.enums.AccountStatus;
import com.acleda.bsonlineshop.enums.UserRole;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private UserRole role;
    private AccountStatus status;
    private UUID shopId;
    private String avatar;
    private String avatarAlt;
    private String phone;
    private Instant joinDate;
    private ShippingAddressDto shipping;
}