package com.example.weaver.dtos.responses;

import com.example.weaver.models.Project;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        UUID createdBy,
        String createdByEmail,
        String createdByNickname,
        String createdByAvatarUrl,
        String name,
        String description,
        Instant finishedAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static ProjectResponse toResponse(Project p) {
        return new ProjectResponse(
                p.getId(),
                p.getCreatedBy() != null ? p.getCreatedBy().getId() : null,
                p.getCreatedBy() != null ? p.getCreatedBy().getEmail() : null,
                p.getCreatedBy() != null ? p.getCreatedBy().getNickname() : null,
                p.getCreatedBy() != null ? p.getCreatedBy().getAvatarUrl() : null,
                p.getName(),
                p.getDescription(),
                p.getFinishedAt(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}