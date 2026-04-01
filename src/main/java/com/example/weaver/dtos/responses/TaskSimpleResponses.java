package com.example.weaver.dtos.responses;

import com.example.weaver.enums.TaskStatus;
import com.example.weaver.models.Task;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TaskSimpleResponses(List<TaskSimpleResponse> tasks,
                                  Instant lastAccessCursor,
                                  UUID idCursor,
                                  boolean hasNext) {
}
