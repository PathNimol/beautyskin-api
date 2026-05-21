package com.acleda.bsonlineshop.dto.user;

import com.acleda.bsonlineshop.enums.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomerStatusRequest {
    @NotNull
    private AccountStatus status;
}
