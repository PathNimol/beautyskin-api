package com.acleda.bsonlineshop.entity;

import com.acleda.bsonlineshop.enums.AccountStatus;
import com.acleda.bsonlineshop.enums.UserRole;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    private UUID shopId;

    private String avatar;
    private String avatarAlt;
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;

    private Instant joinDate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "firstName", column = @Column(name = "shipping_first_name")),
            @AttributeOverride(name = "lastName",  column = @Column(name = "shipping_last_name")),
            @AttributeOverride(name = "address",   column = @Column(name = "shipping_address")),
            @AttributeOverride(name = "city",      column = @Column(name = "shipping_city")),
            @AttributeOverride(name = "state",     column = @Column(name = "shipping_state")),
            @AttributeOverride(name = "zip",       column = @Column(name = "shipping_zip")),
            @AttributeOverride(name = "country",   column = @Column(name = "shipping_country"))
    })
    private ShippingAddress shipping;

    private boolean emailVerified;

    public String getFullName() {
        return firstName + " " + lastName;
    }


}