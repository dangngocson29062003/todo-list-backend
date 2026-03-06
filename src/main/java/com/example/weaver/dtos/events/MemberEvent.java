package com.example.weaver.dtos.events;

import com.example.weaver.enums.NotificationCategory;
import com.example.weaver.enums.NotificationCode;
import com.example.weaver.enums.NotificationType;
import com.example.weaver.enums.Priority;

import java.util.UUID;

public record MemberEvent(
        UUID userId,
        NotificationCode code,
        Object projectResponse,
        NotificationCategory category,
        Priority priority,
        NotificationType type
) {}