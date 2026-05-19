package com.acleda.bsonlineshop.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlaceOrderRequest {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String email;
    private String phone;
    @NotBlank
    private String address;
    @NotBlank
    private String city;
    private String state;
    private String zip;
    @NotBlank
    private String country;
    @NotBlank
    private String paymentMethod;
    private boolean saveInfo;
    private String notes;
}
