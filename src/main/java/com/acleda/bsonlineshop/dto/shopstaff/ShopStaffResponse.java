package com.acleda.bsonlineshop.dto.shopstaff;

import com.acleda.bsonlineshop.enums.AccountStatus;
import com.acleda.bsonlineshop.enums.ShopUserRole;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShopStaffResponse {
    private UUID id;
    private UUID shopId;
    private String name;
    private String email;
    private String phone;
    private ShopUserRole role;
    private String avatar;
    private String avatarAlt;
    private AccountStatus status;
    private Instant createdAt;
}
