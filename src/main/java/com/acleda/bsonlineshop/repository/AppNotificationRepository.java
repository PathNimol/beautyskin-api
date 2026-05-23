package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.AppNotification;
import com.acleda.bsonlineshop.entity.User;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppNotificationRepository extends JpaRepository<AppNotification, UUID> {
    java.util.Optional<AppNotification> findByIdAndDeletedFalse(UUID id);

    java.util.List<AppNotification> findByUserAndReadFalseAndDeletedFalse(com.acleda.bsonlineshop.entity.User user);
    Page<AppNotification> findByUserAndDeletedFalseOrderByCreatedAtDesc(User user, Pageable pageable);
}
