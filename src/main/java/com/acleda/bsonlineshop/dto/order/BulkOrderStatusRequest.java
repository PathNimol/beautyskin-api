package com.acleda.bsonlineshop.dto.order;

import com.acleda.bsonlineshop.enums.OrderStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class BulkOrderStatusRequest {
    @NotEmpty
    private List<UUID> ids;
    @NotNull
    private OrderStatus status;
}
