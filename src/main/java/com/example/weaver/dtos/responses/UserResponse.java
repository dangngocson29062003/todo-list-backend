package com.example.weaver.dtos.responses;

import com.example.weaver.models.User;
import java.util.UUID;

public record UserResponse(UUID id,
                           String email,
                           String fullName,
                           String nickname,
                           String phone,
                           String address,
                           String avatarUrl) {
    public static UserResponse toResponse(User user){
        return new UserResponse(user.getId(),user.getEmail(), user.getFullName(), user.getNickname(),
                user.getPhone(),  user.getAddress(), user.getAvatarUrl());
    }
}
