package com.example.weaver.dtos.responses;

import com.example.weaver.enums.Priority;
import com.example.weaver.enums.Stage;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DeletedProjectResponse(UUID id,
                                     String name,
                                     String description,
                                     Boolean isDeleted,
                                     Instant deletedAt,
                                     UserResponse deletedBy) {
    public DeletedProjectResponse(
            UUID id,
            String name,
            String description,
            Boolean isDeleted,
            Instant deletedAt,
            UUID userId, String email, String fullName, String nickname, String phone, String address, String avatarUrl
    ) {
        this(id, name, description, isDeleted, deletedAt,
                new UserResponse(userId, email, fullName, nickname, phone, address, avatarUrl));
    }
}
