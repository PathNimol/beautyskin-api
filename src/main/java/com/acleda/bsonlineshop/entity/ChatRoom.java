package com.acleda.bsonlineshop.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "chat_rooms")
public class ChatRoom extends BaseEntity {

    private String name;
    private String roomType;
    private String allowedRoles;
}
