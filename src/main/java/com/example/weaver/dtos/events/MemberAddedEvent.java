package com.example.weaver.dtos.events;

import com.example.weaver.dtos.responses.ProjectResponse;
import com.example.weaver.enums.NotificationCategory;
import com.example.weaver.enums.NotificationCode;
import com.example.weaver.enums.NotificationType;
import com.example.weaver.enums.Priority;

import java.util.UUID;

public record MemberAddedEvent(
        UUID userId,
        NotificationCode code,
        ProjectResponse projectResponse,
        NotificationCategory category,
        Priority priority,
        NotificationType type
) {}