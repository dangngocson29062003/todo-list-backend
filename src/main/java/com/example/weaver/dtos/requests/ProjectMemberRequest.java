package com.example.weaver.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMemberRequest {
    @NotNull(message = "Project Id is required")
    private UUID projectId;
    @NotNull(message = "User Id is required")
    private UUID userId;
}
