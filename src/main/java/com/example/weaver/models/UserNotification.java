package com.example.weaver.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "user_notifications",
        indexes = {
                @Index(name = "idx_user_notification_feed", columnList = "user_id, is_read, notification_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_notification_user_notification",
                        columnNames = {"user_id", "notification_id"}
                )
        }

)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Column(name = "is_read")
    private boolean isRead = false;
    @Column(name = "read_at")
    private Instant readAt;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
