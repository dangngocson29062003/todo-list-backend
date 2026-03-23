package com.example.weaver.dtos.responses;

import com.example.weaver.enums.Role;
import com.example.weaver.models.Project;
import com.example.weaver.models.ProjectMember;

import java.time.Instant;
import java.util.UUID;

public record ProjectMemberResponse(
        UUID id,
        UUID userId,
        String email,
        String nickname,
        String fullName,
        String avatarUrl,
        UUID projectId,
        String name,
        Role role,
        Instant createdAt
) {

    public static ProjectMemberResponse toResponse(ProjectMember p) {
        return new ProjectMemberResponse(
                p.getId(),
                p.getUser() != null ? p.getUser().getId() : null,
                p.getUser() != null ? p.getUser().getEmail() : null,
                p.getUser() != null ? p.getUser().getNickname() : null,
                p.getUser() != null ? p.getUser().getFullName() : null,
                p.getUser() != null ? p.getUser().getAvatarUrl() : null,
                p.getProject()!=null?p.getProject().getId() : null,
                p.getName(),
                p.getRole(),
                p.getCreatedAt()
        );
    }
}