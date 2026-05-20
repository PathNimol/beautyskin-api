package com.acleda.bsonlineshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class ShippingAddress {

    @Column(name = "shipping_first_name")
    private String firstName;

    @Column(name = "shipping_last_name")
    private String lastName;

    @Column(name = "shipping_address")
    private String address;

    @Column(name = "shipping_city")
    private String city;

    @Column(name = "shipping_state")
    private String state;

    @Column(name = "shipping_zip")
    private String zip;

    @Column(name = "shipping_country")
    private String country;
}