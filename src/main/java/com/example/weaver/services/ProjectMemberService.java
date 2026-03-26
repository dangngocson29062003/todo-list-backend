package com.example.weaver.services;

import com.example.weaver.enums.MemberStatus;
import com.example.weaver.enums.Role;
import com.example.weaver.exceptions.BadRequestException;
import com.example.weaver.models.Project;
import com.example.weaver.models.ProjectMember;
import com.example.weaver.models.User;
import com.example.weaver.repositories.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {
    private final ProjectMemberRepository repository;

    private final Instant FAR_FUTURE = Instant.parse("9999-12-31T23:59:59Z");

    public ProjectMember addProjectMember(Project project, User user, Role role) {
        repository.findByProject_IdAndUser_Id(project.getId(), user.getId())
                .ifPresent(m -> {
                    throw new BadRequestException("User is already a member or has a pending invite");
                });

        ProjectMember projectMember = ProjectMember.builder()
                .project(project)
                .user(user)
                .name(user.getNickname() != null ? user.getNickname() : user.getFullName())
                .role(role)
                .status(MemberStatus.PENDING)
                .lastAccess(Instant.now())
                .build();

        return repository.save(projectMember);
    }

    public ProjectMember updateProjectMemberRole(UUID projectId, UUID userId, Role newRole) {
        ProjectMember projectMember = getProjectMember(projectId, userId);
        if (!projectMember.getRole().equals(newRole)) {
            projectMember.setRole(newRole);
        }
//        return projectMemberRepository.save(projectMember);
        return projectMember;
    }

    public void removeProjectMember(UUID projectId, UUID userId) {
        ProjectMember projectMember = getProjectMember(projectId, userId);
        repository.delete(projectMember);
    }


    public ProjectMember getProjectMember(UUID projectId, UUID userId) {
        return repository.findByProject_IdAndUser_Id(projectId, userId)
                .orElseThrow(() -> new BadRequestException("User does not belong to this project"));
    }

    public ProjectMember getProjectMemberWithProjectLoaded(UUID projectId, UUID userId) {
        return repository.findWithProjectByProject_IdAndUser_Id(projectId, userId)
                .orElseThrow(() -> new BadRequestException("User does not belong to this project"));
    }

    public boolean memberExists(UUID projectId, UUID userId) {
        return repository.existsByProject_IdAndUser_Id(projectId, userId);
    }

    public ProjectMember checkRole(UUID projectId, UUID userId) {
        ProjectMember member = getProjectMember(projectId, userId);

        if (member.getRole() != Role.MANAGER) {
            throw new BadRequestException("You don't have permission to delete task");
        }
        return member;
    }

    public List<ProjectMember> getWithUsers(UUID projectId) {
        return repository.findWithUsersByProject_Id(projectId);
    }

    public void updateProjectPinStatus(UUID projectId, UUID userId) {
        ProjectMember projectMember = getProjectMember(projectId, userId);
        if (!projectMember.isPinned()) {
            long projectMembers = repository.countByUser_IdAndIsPinnedTrue(userId);

            if (projectMembers > 5) {
                throw new BadRequestException("You can only pin 5 projects at a time");
            }
        }
        projectMember.setPinned(!projectMember.isPinned());
        repository.save(projectMember);
    }

    public void updateProjectLastAccess(UUID projectId, UUID userId) {
        repository.updateLastAccess(userId, projectId, Instant.now());
    }

}
