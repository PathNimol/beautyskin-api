package com.acleda.bsonlineshop.dto.shopstaff;

import com.acleda.bsonlineshop.enums.ShopUserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShopStaffCreateRequest {
    @NotBlank
    private String name;
    @NotBlank
    @Email
    private String email;
    private String phone;
    @NotNull
    private ShopUserRole role;
    private String avatar;
    private String avatarAlt;
}
