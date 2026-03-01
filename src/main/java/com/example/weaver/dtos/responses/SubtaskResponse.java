package com.example.weaver.dtos.responses;

import com.example.weaver.enums.Priority;
import com.example.weaver.enums.TaskStatus;
import com.example.weaver.enums.TaskType;
import com.example.weaver.models.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubtaskResponse {
    private Long id;

    private String name;
    private String description;

    private Instant startedAt;
    private Instant endedAt;

    private TaskType taskType;
    private Priority priority;
    private TaskStatus taskStatus;

    private Long parentId;

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
