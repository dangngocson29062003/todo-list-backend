package com.example.weaver.dtos.responses;

import com.example.weaver.enums.Priority;
import com.example.weaver.enums.TaskStatus;
import com.example.weaver.enums.TaskType;
import com.example.weaver.models.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskResponse {

    private Long id;

    private String name;
    private String description;

    private Instant startedAt;
    private Instant endedAt;

    private TaskType taskType;
    private Priority priority;
    private TaskStatus taskStatus;

    private UUID projectId;

    private Long parentId;

    private List<TaskAssignmentResponse> assignees;

    private List<SubtaskResponse> subtasks;

    public static TaskResponse toResponse(Task task) {
        List<TaskAssignmentResponse> assignees = task.getAssignments() != null
                ? task.getAssignments().stream()
                .map(TaskAssignmentResponse::toResponse)
                .toList()
                : List.of();
        List<SubtaskResponse> subtasks = task.getChildren() != null
                ? task.getChildren().stream()
                .map(SubtaskResponse::toResponse)
                .toList()
                : List.of();
        return new TaskResponse(
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getStartedAt(),
                task.getEndedAt(),
                task.getType(),
                task.getPriority(),
                task.getStatus(),
                task.getProject() != null ? task.getProject().getId() : null,
                task.getParent() != null ? task.getParent().getId() : null,
                assignees,
                subtasks
        );
    }
}
