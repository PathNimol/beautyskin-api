package com.acleda.bsonlineshop.dto.user;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class CustomerUpdateRequest {
    private String firstName;
    private String lastName;
    @Email
    private String email;
    private String phone;
}
