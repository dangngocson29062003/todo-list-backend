package com.example.weaver.dtos.responses;

import com.example.weaver.models.TaskAssignment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskAssignmentResponse {

    private Long id;

    private Long taskId;

    private UUID userId;

    private UUID assignedBy;

    private Instant assignedAt;

    public static TaskAssignmentResponse toResponse(TaskAssignment taskAssignment) {
        return new TaskAssignmentResponse(taskAssignment.getId(),
                taskAssignment.getTask().getId(),
                taskAssignment.getUser().getId(),
                taskAssignment.getAssignedBy().getId(),
                taskAssignment.getAssignedAt());
    }
}
