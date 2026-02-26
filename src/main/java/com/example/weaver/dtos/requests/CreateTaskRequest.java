package com.example.weaver.dtos.requests;

import com.example.weaver.enums.Priority;
import com.example.weaver.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTaskRequest {
    @NotBlank(message = "Name is not empty")
    private String name;
    private String description;

    private Instant startedAt;
    private Instant endedAt;

    private TaskType taskType;
    private Priority priority;

    @NotNull(message = "Project ID is not empty")
    private UUID projectId;
}
