package com.acleda.bsonlineshop.dto.common;

import lombok.Data;

@Data
public class ShippingAddressDto {
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String country;
}
