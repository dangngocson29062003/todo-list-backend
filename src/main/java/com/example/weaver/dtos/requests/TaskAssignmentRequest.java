package com.example.weaver.dtos.requests;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskAssignmentRequest {
    private UUID userId;
}
