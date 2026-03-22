package com.example.weaver.dtos.responses;

import com.example.weaver.enums.TaskStatus;
import com.example.weaver.models.Task;

import java.util.List;

public record TaskSimpleResponses(List<TaskSimpleResponse> tasks,
                                  Integer cursor,
                                  boolean hasNext) {
}
