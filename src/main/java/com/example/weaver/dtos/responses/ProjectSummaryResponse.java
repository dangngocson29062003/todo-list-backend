package com.example.weaver.dtos.responses;

import com.example.weaver.enums.Priority;
import com.example.weaver.enums.Stage;
import com.example.weaver.models.Project;
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
        long memberCount,
        long taskCount,
        long doneTaskCount,
        UserResponse createdBy
) {
    public ProjectSummaryResponse(
            UUID id, String name, String description, Stage stage, Priority priority, String tags, List<String> techStack,
            LocalDate startDate, LocalDate endDate,
            long memberCount, long taskCount, long doneTaskCount,
            UUID userId, String email, String fullName, String avatarUrl
    ) {
        this(id, name, description, stage, priority, tags, techStack, startDate, endDate,
                memberCount, taskCount, doneTaskCount,
                new UserResponse(userId, email, fullName, null, null, null, avatarUrl));
    }
}