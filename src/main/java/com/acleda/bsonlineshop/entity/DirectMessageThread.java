package com.acleda.bsonlineshop.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "dm_threads")
public class DirectMessageThread extends BaseEntity {

    private UUID participantOneId;
    private UUID participantTwoId;
    private String subject;
}
