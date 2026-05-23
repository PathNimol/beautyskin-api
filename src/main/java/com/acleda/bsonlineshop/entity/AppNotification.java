package com.acleda.bsonlineshop.entity;

import com.acleda.bsonlineshop.enums.NotificationType;
import jakarta.persistence.*;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "notifications")
public class AppNotification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private UUID shopId;

    @Enumerated(EnumType.STRING)
    private NotificationType type;
    private String title;

    @Column(length = 2000)
    private String message;

    private boolean read;
    private String link;
}
