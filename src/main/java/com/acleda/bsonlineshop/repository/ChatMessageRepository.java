package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.ChatMessage;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    Page<ChatMessage> findByRoom_IdAndDeletedFalseOrderByCreatedAtAsc(UUID roomId, Pageable pageable);
}
