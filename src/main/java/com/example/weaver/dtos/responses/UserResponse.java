package com.example.weaver.dtos.responses;

import com.example.weaver.enums.AuthProvider;
import com.example.weaver.enums.UserStatus;
import com.example.weaver.models.User;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id,String email,String fullName,String nickname
        ,String phone,String address,String avatarUrl,Instant createdAt,Instant updatedAt) {
    public static UserResponse toResponse(User user){
        return new UserResponse(user.getId(),user.getEmail(), user.getFullName(), user.getNickname(),
                user.getPhone(),  user.getAddress(), user.getAvatarUrl(), user.getCreatedAt(), user.getUpdatedAt());
    }
}
