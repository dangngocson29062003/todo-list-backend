package com.example.weaver.dtos.responses;

import com.example.weaver.enums.TaskStatus;
import com.example.weaver.models.Task;

import java.time.Instant;
import java.util.UUID;

public record TaskSimpleResponse(UUID id, String name, TaskStatus status,
                                 Instant lastAccess, Boolean isPinned) {
    public static TaskSimpleResponse toResponse(Task task) {
        return new TaskSimpleResponse(
                task.getId(),
                task.getName(),
                task.getStatus(),
                null,
                false);
    }
}
