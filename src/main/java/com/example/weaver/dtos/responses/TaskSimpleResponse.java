package com.example.weaver.dtos.responses;

import com.example.weaver.enums.TaskStatus;
import com.example.weaver.models.Task;

import java.time.Instant;

public record TaskSimpleResponse(Long id, String name, TaskStatus status,
                                 Integer index,Boolean pinned) {
    public static TaskSimpleResponse toResponse(Task task) {
        return new TaskSimpleResponse(
                task.getId(),
                task.getName(),
                task.getStatus(),
                0,
                false);
    }
}
