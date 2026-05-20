package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.User;
import com.acleda.bsonlineshop.entity.UserPreferences;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, UUID> {
    Optional<UserPreferences> findByUserAndDeletedFalse(User user);
}
