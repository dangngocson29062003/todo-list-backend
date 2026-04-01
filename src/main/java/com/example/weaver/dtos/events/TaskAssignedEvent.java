package com.example.weaver.dtos.events;

import com.example.weaver.dtos.responses.TaskResponse;
import com.example.weaver.enums.NotificationCategory;
import com.example.weaver.enums.NotificationCode;
import com.example.weaver.enums.NotificationType;
import com.example.weaver.enums.Priority;
import com.example.weaver.models.Task;

import java.util.List;
import java.util.UUID;

public record TaskAssignedEvent(UUID userId, TaskResponse taskResponse,
                                NotificationCode code,
                                NotificationCategory category,
                                Priority priority,
                                NotificationType type) {
}
