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
    private UUID userId;
    private String fullName;
    private String email;
    private String avatarUrl;
    private Instant assignedAt;
    private UUID assignedBy;

    public static TaskAssignmentResponse toResponse(TaskAssignment assignment) {
        return new TaskAssignmentResponse(
                assignment.getId(),
                assignment.getUser().getId(),
                assignment.getUser().getFullName(),
                assignment.getUser().getEmail(),
                assignment.getUser().getAvatarUrl(),
                assignment.getAssignedAt(),
                assignment.getAssignedBy() != null ? assignment.getAssignedBy().getId() : null
        );
    }
}
