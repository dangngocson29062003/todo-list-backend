package com.example.weaver.dtos.requests;

import com.example.weaver.enums.NotificationCategory;
import com.example.weaver.enums.NotificationType;
import com.example.weaver.enums.Priority;

import java.util.List;
import java.util.UUID;

public record Test(List<UUID> userIds, String title, String message, String actionUrl,
                   NotificationCategory category, Priority priority, NotificationType type) {
}
