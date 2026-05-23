package com.acleda.bsonlineshop.dto.shopstaff;

import com.acleda.bsonlineshop.enums.AccountStatus;
import com.acleda.bsonlineshop.enums.ShopUserRole;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ShopStaffUpdateRequest {
    private String name;
    @Email
    private String email;
    private String phone;
    private ShopUserRole role;
    private AccountStatus status;
    private String avatar;
    private String avatarAlt;
}
