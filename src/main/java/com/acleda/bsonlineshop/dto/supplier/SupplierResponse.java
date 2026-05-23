package com.acleda.bsonlineshop.dto.supplier;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupplierResponse {
    private UUID id;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String country;
    private String category;
    private int totalOrders;
    private BigDecimal totalSpent;
    private double rating;
    private String status;
    private LocalDate joinDate;
    private LocalDate lastOrder;
    private String logo;
    private String logoAlt;
    private Instant createdAt;
}
