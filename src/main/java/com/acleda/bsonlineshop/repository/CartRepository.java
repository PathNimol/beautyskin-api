package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.Cart;
import com.acleda.bsonlineshop.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUserAndDeletedFalse(User user);
}
