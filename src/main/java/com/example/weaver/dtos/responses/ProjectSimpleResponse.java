package com.example.weaver.dtos.responses;

import com.example.weaver.models.Project;

import java.time.Instant;
import java.util.UUID;

public record ProjectSimpleResponse(UUID id,
                                    String name,
                                    String avatarUrl,
                                    Boolean isPinned,
                                    Instant lastAccess,
                                    Instant createdAt) {

    public static ProjectSimpleResponse toResponse(Project p) {
        return new ProjectSimpleResponse(
                p.getId(),
                p.getName(),
                p.getAvatarUrl(),
                false,
                null,
                null
        );
    }
}

