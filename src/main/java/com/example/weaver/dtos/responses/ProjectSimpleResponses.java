package com.example.weaver.dtos.responses;

import com.example.weaver.models.Project;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProjectSimpleResponses(List<ProjectSimpleResponse> projects,
                                     Instant lastAccessCursor,
                                     Instant createdAtCursor,
                                     boolean hasNext){

}

