package com.example.weaver.dtos.responses;

import com.example.weaver.enums.Priority;
import com.example.weaver.enums.TaskStatus;
import com.example.weaver.models.Task;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TaskSummaryResponse(
        UUID id,
        String name,
        String description,
        Priority priority,
        TaskStatus status,
        LocalDate startDate,
        LocalDate endDate,
        Instant createdAt,
        Long commentCount,
        List<UserResponse> assignees
) {
    public static TaskSummaryResponse toResponse(Task task, Long commentCount) {
        return new TaskSummaryResponse(
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getPriority(),
                task.getStatus(),
                task.getStartedAt(),
                task.getEndedAt(),
                task.getCreatedAt(),
                commentCount,
                task.getAssignments() != null
                        ? task.getAssignments().stream()
                        .map(a -> UserResponse.toResponse(a.getUser()))
                        .toList()
                        : List.of()
        );
    }
}