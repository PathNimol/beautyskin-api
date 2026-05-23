package com.acleda.bsonlineshop.dto.common;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    @NotBlank
    private String status;
}
