package com.example.weaver.repositories;

import com.example.weaver.enums.NotificationCategory;
import com.example.weaver.models.UserNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    @EntityGraph(attributePaths = {"user"})
    Optional<UserNotification> findById(Long id);

    @Query("""
        SELECT unoti
        FROM UserNotification unoti
        JOIN FETCH unoti.notification noti
        WHERE unoti.user.id = :userId
        AND (:category IS NULL OR noti.category = :category)
        AND (:isRead IS NULL OR unoti.isRead = :isRead)
        AND (:cursorCreatedAt IS NULL OR
                (
                    noti.priorityRank < :cursorPriorityRank
                    OR (
                        noti.priorityRank = :cursorPriorityRank
                        AND noti.createdAt < :cursorCreatedAt
                    )
                )
        )
        ORDER BY
            noti.priorityRank DESC,
            noti.createdAt DESC
        """)
    List<UserNotification> getNotificationsCursor(
            UUID userId,
            NotificationCategory category,
            Boolean isRead,
            Integer cursorPriorityRank,
            Instant cursorCreatedAt,
            Pageable pageable
    );
}
