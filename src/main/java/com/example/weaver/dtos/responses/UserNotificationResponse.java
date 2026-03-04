package com.example.weaver.dtos.responses;

import com.example.weaver.enums.NotificationCategory;
import com.example.weaver.enums.NotificationCode;
import com.example.weaver.enums.NotificationType;
import com.example.weaver.models.Notification;
import com.example.weaver.models.UserNotification;

import java.time.Instant;

public record UserNotificationResponse(Long id, NotificationCode code,String payload, NotificationCategory category,
                                       int priorityRank, NotificationType type, Instant createdAt,
                                       boolean isRead) {
    public static UserNotificationResponse toResponse(UserNotification u){
        Notification notification = u.getNotification();
        return new UserNotificationResponse(u.getId(), notification.getCode(),notification.getPayload()
                ,notification.getCategory(), notification.getPriorityRank(),notification.getType(),u.getCreatedAt(),u.isRead());
    }
}
