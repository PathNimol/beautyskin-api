package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.AppNotification;
import com.acleda.bsonlineshop.entity.User;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppNotificationRepository extends JpaRepository<AppNotification, UUID> {
    Page<AppNotification> findByUserAndDeletedFalseOrderByCreatedAtDesc(User user, Pageable pageable);
}
