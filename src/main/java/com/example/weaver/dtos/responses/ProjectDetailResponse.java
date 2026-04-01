package com.example.weaver.dtos.responses;

import com.example.weaver.enums.Priority;
import com.example.weaver.enums.Stage;
import com.example.weaver.models.Project;

import java.time.Instant;
import java.time.LocalDate;
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

        Boolean isFavorite,

        LocalDate startDate,
        LocalDate endDate,
        Instant createdAt,
        Instant updatedAt,

        UserResponse createdBy,
        List<ProjectMemberResponse> members,
        StatsResponse stats
) {

    public static ProjectDetailResponse toResponse(Project p, Boolean isFavorite,  StatsResponse stats) {
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
                isFavorite,
                p.getStartDate(),
                p.getEndDate(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                UserResponse.toResponse(p.getCreatedBy()),
                p.getMembers() != null ? p.getMembers().stream().map(ProjectMemberResponse::toResponse).toList() : new ArrayList<>(),
                stats
        );
    }
}