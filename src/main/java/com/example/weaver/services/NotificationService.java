package com.example.weaver.services;

import com.example.weaver.enums.NotificationCategory;
import com.example.weaver.enums.NotificationType;
import com.example.weaver.enums.Priority;
import com.example.weaver.models.Notification;
import com.example.weaver.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public Notification create(String title, String message,
                               String actionUrl, NotificationCategory category,
                               int priorityRank, NotificationType type){
        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .actionUrl(actionUrl)
                .category(category)
                .priorityRank(priorityRank)
                .type(type)
                .build();
        return notificationRepository.save(notification);
    }

    public boolean existsById(Long notificationId) {
        return  notificationRepository.existsById(notificationId);
    }
}
