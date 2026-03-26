package com.example.weaver.dtos.requests;

import com.example.weaver.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMemberRequest {
    @NotNull(message = "User Id is required")
    private UUID userId;
    @NotNull(message = "Role is required")
    private Role role;
}
