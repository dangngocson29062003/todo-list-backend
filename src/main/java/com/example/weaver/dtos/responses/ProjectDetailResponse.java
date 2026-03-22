package com.example.weaver.dtos.responses;

import com.example.weaver.enums.Priority;
import com.example.weaver.enums.Stage;
import com.example.weaver.models.Project;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ProjectDetailResponse(
        UUID id,
        String name,
        String description,

        String tags,


        Stage stage,
        Priority priority,

        List<String> goals,
        List<String> techStack,

        String githubUrl,
        String figmaUrl,

        Instant startDate,
        Instant endDate,
        Instant createdAt,
        Instant updatedAt,

        UserResponse createdBy,
        List<ProjectMemberResponse> members
) {

    public static ProjectDetailResponse toResponse(Project p) {
        return new ProjectDetailResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getTags(),
                p.getStage(),
                p.getPriority(),
                p.getGoals(),
                p.getTechStack(),
                p.getGithubUrl(),
                p.getFigmaUrl(),
                p.getStartDate(),
                p.getEndDate(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                UserResponse.toResponse(p.getCreatedBy()),
                p.getMembers() != null ? p.getMembers().stream().map(ProjectMemberResponse::toResponse).toList() : new ArrayList<>()
        );
    }
}