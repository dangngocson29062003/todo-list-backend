package com.example.weaver.dtos.responses;

import com.example.weaver.enums.Priority;
import com.example.weaver.enums.Stage;
import com.example.weaver.models.Project;
import com.example.weaver.models.ProjectMember;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjectSummaryResponse(
        UUID id,
        String name,
        String description,
        Stage stage,
        Priority priority,
        String tags,
        List<String> techStack,
        LocalDate startDate,
        LocalDate endDate,
        Instant createdAt,
        long memberCount,
        long taskCount,
        long doneTaskCount,
        Instant lastAccess,
        Boolean isFavorite,
        UserResponse createdBy
) {
    public ProjectSummaryResponse(
            UUID id, String name, String description, Stage stage, Priority priority,
            String tags, List<String> techStack, LocalDate startDate, LocalDate endDate, Instant createdAt,
            long memberCount, long taskCount, long doneTaskCount,
            Instant lastAccess, Boolean isFavorite,
            UUID userId, String email, String fullName, String avatarUrl
    ) {
        this(id, name, description, stage, priority, tags, techStack, startDate, endDate, createdAt,
                memberCount, taskCount, doneTaskCount, lastAccess, isFavorite,
                new UserResponse(userId, email, fullName, null, null, null, avatarUrl));
    }
    public static ProjectSummaryResponse toResponse(Project p, ProjectMember pm) {
        return new ProjectSummaryResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getStage(),
                p.getPriority(),
                p.getTags(),
                p.getTechStack(),
                p.getStartDate(),
                p.getEndDate(),
                p.getCreatedAt(),
                p.getMembers() != null ? p.getMembers().size() : 0,
                p.getTasks() != null ? p.getTasks().size() : 0,
                p.getTasks() != null ? p.getTasks().stream()
                        .filter(t -> t.getStatus().name().equals("DONE")).count() : 0,
                pm.getLastAccess() != null ? pm.getLastAccess() : Instant.now(),
                pm.isFavorited(),
                UserResponse.toResponse(p.getCreatedBy())
        );
    }
}
