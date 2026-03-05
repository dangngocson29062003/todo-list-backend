package com.example.weaver.dtos.events;

import com.example.weaver.enums.NotificationCategory;
import com.example.weaver.enums.NotificationCode;
import com.example.weaver.enums.NotificationType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record NotificationCreatedEvent(List<UUID> userIds, List<Long> userNotificationIds,
                                       NotificationCode code, Object payload,
                                       NotificationCategory category, int priorityRank,
                                       NotificationType type, Instant createdAt) {
}
