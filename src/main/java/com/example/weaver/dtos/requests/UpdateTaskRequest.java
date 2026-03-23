package com.example.weaver.dtos.requests;

import com.example.weaver.enums.Priority;
import com.example.weaver.enums.TaskStatus;
import com.example.weaver.enums.TaskType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTaskRequest {
    private String name;
    private String description;

    private Instant startedAt;
    private Instant endedAt;

    private TaskType taskType;
    private Priority priority;
    private TaskStatus taskStatus;
}
