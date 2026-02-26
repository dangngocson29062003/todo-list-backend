package com.example.weaver.dtos.others;

import com.example.weaver.models.User;

import java.util.UUID;

public record UserSnapshot(
        UUID id,
        String email
) {
    public static UserSnapshot from(User user) {
        return new UserSnapshot(
                user.getId(),
                user.getEmail()
        );
    }
}