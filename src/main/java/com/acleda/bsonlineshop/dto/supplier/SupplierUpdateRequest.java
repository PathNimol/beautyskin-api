package com.acleda.bsonlineshop.dto.supplier;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class SupplierUpdateRequest {
    private String name;
    private String contactPerson;
    @Email
    private String email;
    private String phone;
    private String address;
    private String country;
    private String category;
    private String logo;
    private String logoAlt;
    private String status;
}
