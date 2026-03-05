package com.example.weaver.services;

import com.example.weaver.enums.NotificationCategory;
import com.example.weaver.exceptions.NotFoundException;
import com.example.weaver.models.Notification;
import com.example.weaver.models.User;
import com.example.weaver.models.UserNotification;
import com.example.weaver.repositories.UserNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserNotificationService {
    private final UserNotificationRepository repository;

    public UserNotification create(User user, Notification notification) {
        UserNotification userNotification = UserNotification.builder()
                .user(user)
                .notification(notification)
                .build();
        return repository.save(userNotification);
    }

    public List<UserNotification> createMultiple(List<User> users, Notification notification) {
        List<UserNotification> userNotifications = new ArrayList<>();
        for (User user : users) {
            UserNotification userNotification = UserNotification.builder()
                    .user(user)
                    .notification(notification)
                    .build();
            userNotifications.add(userNotification);
        }

        return repository.saveAll(userNotifications);
    }

    public List<UserNotification> getUserNotifications(UUID userId, NotificationCategory category,
                                                       Boolean isRead, Integer cursorPriorityRank,
                                                       Instant cursorCreatedAt, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        if (cursorPriorityRank == null || cursorCreatedAt == null) {
            cursorPriorityRank = Integer.MAX_VALUE;
            cursorCreatedAt = Instant.parse("9999-12-31T23:59:59Z");
        }
        return repository.getNotificationsCursor(
                userId,
                category,
                isRead,
                cursorPriorityRank,
                cursorCreatedAt,
                pageable
        );
    }

    public void setRead(UserNotification userNotification) {
        userNotification.setRead(true);
        userNotification.setReadAt(Instant.now());
        repository.save(userNotification);
    }

    public UserNotification findById(Long userNotificationId) {
        return repository.findById(userNotificationId)
                .orElseThrow(() -> new NotFoundException("UserNotification not found"));
    }

    public UserNotification findByIdWithUserLoaded(Long userNotificationId) {
        return repository.findById(userNotificationId)
                .orElseThrow(() -> new NotFoundException("UserNotification not found"));
    }
}
