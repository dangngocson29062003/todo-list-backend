package com.example.weaver.dtos.responses;

import com.example.weaver.models.Project;
import com.example.weaver.models.Task;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProjectDetailResponse(
        UUID id,
        UUID createdBy,
        String createdByEmail,
        String createdByNickname,
        String createdByAvatarUrl,
        String name,
        String description,
        Instant finishedAt,
        List<String> links,
        List<String> tags,
        List<UserResponse> members,
        List<TaskResponse> tasks,
        Instant createdAt,
        Instant updatedAt
) {

    public static ProjectDetailResponse toResponse(Project p, List<Task> tasks) {
        return new ProjectDetailResponse(
                p.getId(),
                p.getCreatedBy() != null ? p.getCreatedBy().getId() : null,
                p.getCreatedBy() != null ? p.getCreatedBy().getEmail() : null,
                p.getCreatedBy() != null ? p.getCreatedBy().getNickname() : null,
                p.getCreatedBy() != null ? p.getCreatedBy().getAvatarUrl() : null,
                p.getName(),
                p.getDescription(),
                p.getFinishedAt(),
                p.getLinks(),
                p.getTags(),
                //su dung entity graph
                p.getMembers()!=null? p.getMembers().stream().map(member->UserResponse.toResponse(member.getUser())).toList():null,
                tasks!=null? p.getTasks().stream().map(TaskResponse::toResponse).toList():null,
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}