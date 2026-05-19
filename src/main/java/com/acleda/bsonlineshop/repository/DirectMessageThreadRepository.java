package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.DirectMessageThread;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DirectMessageThreadRepository extends JpaRepository<DirectMessageThread, UUID> {

    @Query("""
            SELECT t FROM DirectMessageThread t WHERE t.deleted = false
            AND (t.participantOneId = :userId OR t.participantTwoId = :userId)
            """)
    List<DirectMessageThread> findForUser(@Param("userId") UUID userId);
}
