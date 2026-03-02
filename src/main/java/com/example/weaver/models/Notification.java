package com.example.weaver.models;

import com.example.weaver.enums.NotificationCategory;
import com.example.weaver.enums.NotificationType;
import com.example.weaver.enums.Priority;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notification_priority_rank_created_at", columnList = "priority_rank DESC, created_at DESC, category")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String message;

    @Column(name = "action_url")
    private String actionUrl;

//    private String icon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationCategory category;

    @Column(name="priority_rank",nullable = false)
    private int priorityRank; //1.LOW 2.MEDIUM 3.HIGH 4.URGENT

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

}
