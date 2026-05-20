package com.acleda.bsonlineshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "otp_verifications")
public class OtpVerification extends BaseEntity {

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String purpose;

    @Column(nullable = false)
    private Instant expiresAt;

    private boolean used;
}
