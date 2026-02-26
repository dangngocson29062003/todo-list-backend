package com.example.weaver.dtos.requests;

import com.example.weaver.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProjectMemberRoleRequest {
    @NotNull(message = "New role is required")
    private Role newRole;
}
