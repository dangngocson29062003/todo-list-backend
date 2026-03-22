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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {
    private final ProjectMemberRepository repository;

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

    public ProjectSimpleResponses getProjectsByUserId(UUID userId, Instant lastLastAccessCursor,
                                                      Instant lastCreatedAtCursor, int limit) {
        List<ProjectSimpleResponse> responses = new ArrayList<>();

        // FIRST LOAD
        if (lastCreatedAtCursor == null) {
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
                    Instant.MAX,
                    Instant.MAX,
                    true
            );
        }

        // NEXT LOAD (only unpinned)
        int pageSize = Math.min(Math.max(limit, 1), 10);

        Slice<ProjectSimpleResponse> projectsSlice =
                repository.findUnpinnedProjectWithCursor(
                        userId,
                        lastCreatedAtCursor,
                        lastLastAccessCursor,
                        PageRequest.of(0, pageSize)
                );

        return getProjectSimpleResponses(responses, projectsSlice);
    }

    @NonNull
    private ProjectSimpleResponses getProjectSimpleResponses(List<ProjectSimpleResponse> responses,
                                                             Slice<ProjectSimpleResponse> projectsSlice) {
        List<ProjectSimpleResponse> projects = projectsSlice.getContent();
        responses.addAll(projects);

        ProjectSimpleResponse lastProject = projects.getLast();
        Instant lastAccess = lastProject==null ? null : lastProject.lastAccess();
        Instant lastCreatedAt = lastProject==null ? null : lastProject.createdAt();

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
        if(!projectMember.getIsPinned()){
            List<ProjectMember> projectMembers = repository.findAllByUser_IdAndIsPinnedTrue(userId);

            if(projectMembers.size() > 5) {
                throw new BadRequestException("You can only pin 5 projects at a time");
            }
        }
       projectMember.setIsPinned(!projectMember.getIsPinned());
    }

    public void updateProjectLastAccess(UUID projectId, UUID userId, Instant lastAccess) {
        ProjectMember projectMember = getProjectMember(projectId, userId);
        projectMember.setLastAccess(lastAccess);
        repository.save(projectMember);
    }

}
