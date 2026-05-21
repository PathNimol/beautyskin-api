package com.acleda.bsonlineshop.dto.message;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class MarkThreadReadRequest {
    @NotNull
    private UUID threadId;
}
