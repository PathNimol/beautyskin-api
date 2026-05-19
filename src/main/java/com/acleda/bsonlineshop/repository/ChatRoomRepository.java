package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.ChatRoom;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {}
