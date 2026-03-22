package com.example.weaver.services;

import com.example.weaver.enums.Role;
import com.example.weaver.exceptions.BadRequestException;
import com.example.weaver.models.Project;
import com.example.weaver.models.ProjectMember;
import com.example.weaver.models.User;
import com.example.weaver.repositories.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {
    private final ProjectMemberRepository projectMemberRepository;

    public ProjectMember addProjectMember(Project project, User user, Role role) {
        ProjectMember projectMember = ProjectMember.builder()
                .project(project)
                .user(user)
                .name(user.getNickname())
                .role(role)
                .build();
        return projectMemberRepository.save(projectMember);
    }
    public ProjectMember updateProjectMemberRole(UUID projectId,UUID userId, Role newRole) {
        ProjectMember projectMember=getProjectMember(projectId,userId);
        if(!projectMember.getRole().equals(newRole)){
            projectMember.setRole(newRole);
        }
        return projectMember;
    }
    public void removeProjectMember(UUID projectId, UUID userId) {
        ProjectMember projectMember=getProjectMember(projectId,userId);
        projectMemberRepository.delete(projectMember);
    }

    public List<ProjectMember> getProjectMembers(UUID projectId) {
        return projectMemberRepository.findAllByProject_Id(projectId);
    }



    public ProjectMember getProjectMember(UUID projectId, UUID userId) {
        return projectMemberRepository.findByProject_IdAndUser_Id(projectId,userId)
                .orElseThrow(()->new BadRequestException("User does not belong to this project"));
    }
    public ProjectMember getProjectMemberWithProjectLoaded(UUID projectId, UUID userId) {
        return projectMemberRepository.findWithProjectByProject_IdAndUser_Id(projectId,userId)
                .orElseThrow(()->new BadRequestException("User does not belong to this project"));
    }
    public boolean memberExists(UUID projectId, UUID userId) {
        return projectMemberRepository.existsByProject_IdAndUser_Id(projectId,userId);
    }
    public ProjectMember checkRole(UUID projectId, UUID userId) {
        ProjectMember member = getProjectMember(projectId, userId);

        if (member.getRole() != Role.MANAGER) {
            throw new BadRequestException("You don't have permission to delete task");
        }
        return member;
    }
}
