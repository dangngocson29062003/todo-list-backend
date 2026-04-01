package com.example.weaver.dtos.responses;

import com.example.weaver.enums.Priority;
import com.example.weaver.enums.TaskStatus;
import com.example.weaver.enums.TaskType;
import com.example.weaver.models.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubtaskResponse {
    private UUID id;

    private String name;
    private String description;

    private LocalDate startedAt;
    private LocalDate endedAt;

    private TaskType taskType;
    private Priority priority;
    private TaskStatus taskStatus;

    private UUID parentId;

    public static SubtaskResponse toResponse(Task task ) {
        return new SubtaskResponse(task.getId(),
                task.getName(),
                task.getDescription(),
                task.getStartedAt(),
                task.getEndedAt(),
                task.getType(),
                task.getPriority(),
                task.getStatus(),
                task.getParent().getId());
    }
}
