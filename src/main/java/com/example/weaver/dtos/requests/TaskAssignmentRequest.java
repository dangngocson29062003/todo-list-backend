package com.example.weaver.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskAssignmentRequest {

    private Long taskId;

    private UUID userId;

}
