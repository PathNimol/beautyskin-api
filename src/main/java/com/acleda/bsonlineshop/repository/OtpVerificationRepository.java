package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.OtpVerification;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, UUID> {
    Optional<OtpVerification> findTopByEmailAndPurposeAndUsedFalseAndDeletedFalseOrderByCreatedAtDesc(
            String email, String purpose);
}
