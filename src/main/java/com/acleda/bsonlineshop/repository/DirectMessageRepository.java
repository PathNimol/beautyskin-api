package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.DirectMessage;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID> {
    Page<DirectMessage> findByThread_IdAndDeletedFalseOrderByCreatedAtAsc(UUID threadId, Pageable pageable);
}
