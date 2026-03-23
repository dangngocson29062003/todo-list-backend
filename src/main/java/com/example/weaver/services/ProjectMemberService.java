package com.example.weaver.services;

import com.example.weaver.dtos.responses.ProjectSimpleResponse;
import com.example.weaver.dtos.responses.ProjectSimpleResponses;
import com.example.weaver.enums.Role;
import com.example.weaver.exceptions.BadRequestException;
import com.example.weaver.models.Project;
import com.example.weaver.models.ProjectMember;
import com.example.weaver.models.User;
import com.example.weaver.repositories.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {
    private final ProjectMemberRepository repository;

    private final Instant FAR_FUTURE = Instant.parse("9999-12-31T23:59:59Z");

    public ProjectMember addProjectMember(Project project, User user, Role role) {
        ProjectMember projectMember = ProjectMember.builder()
                .project(project)
                .user(user)
                .name(user.getNickname())
                .role(role)
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

    public List<ProjectMember> getProjectMembers(UUID projectId) {
        return repository.findAllByProject_Id(projectId);
    }

    public ProjectSimpleResponses getProjectsByUserId(UUID userId, Instant lastAccessCursor,
                                                      Instant createdAtCursor, int limit) {
        List<ProjectSimpleResponse> responses = new ArrayList<>();

        // FIRST LOAD
        if (createdAtCursor == null) {
            List<ProjectSimpleResponse> pinned = repository.findPinnedProject(userId);
            responses.addAll(pinned);
            int size = limit - responses.size();

            // Only fetch unpinned if there's space
            if (size > 0) {
                Slice<ProjectSimpleResponse> projectsSlice =
                        repository.findUnpinnedProject(
                                userId,
                                PageRequest.of(0, size)
                        );

                return getProjectSimpleResponses(responses, projectsSlice);
            }

            // No unpinned fetched (pinned equal limit)
            return new ProjectSimpleResponses(
                    responses,
                    FAR_FUTURE,
                    FAR_FUTURE,
                    true
            );
        }

        // NEXT LOAD (only unpinned)
        int pageSize = Math.min(Math.max(limit, 1), 10);

        Slice<ProjectSimpleResponse> projectsSlice =
                repository.findUnpinnedProjectWithCursor(
                        userId,
                        lastAccessCursor,
                        createdAtCursor,
                        PageRequest.of(0, pageSize)
                );

        return getProjectSimpleResponses(responses, projectsSlice);
    }

    @NonNull
    private ProjectSimpleResponses getProjectSimpleResponses(List<ProjectSimpleResponse> responses,
                                                             Slice<ProjectSimpleResponse> projectsSlice) {
        List<ProjectSimpleResponse> projects = projectsSlice.getContent();
        responses.addAll(projects);

        ProjectSimpleResponse lastProject = projects.isEmpty() ? null : projects.getLast();

        Instant lastAccess = Optional.ofNullable(lastProject)
                .map(ProjectSimpleResponse::lastAccess)
                .orElse(FAR_FUTURE);

        Instant lastCreatedAt = Optional.ofNullable(lastProject)
                .map(ProjectSimpleResponse::createdAt)
                .orElse(FAR_FUTURE);

        return new ProjectSimpleResponses(
                responses,
                lastAccess,
                lastCreatedAt,
                projectsSlice.hasNext()
        );
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
        ProjectMember projectMember = getProjectMember(projectId, userId);
        projectMember.setLastAccess(Instant.now());
        repository.save(projectMember);
    }

}
