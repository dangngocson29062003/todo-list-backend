package com.example.weaver.services;

import com.example.weaver.exceptions.NotFoundException;
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

    public ProjectMember addProjectMember(Project project, User user) {
        ProjectMember projectMember = ProjectMember.builder()
                .id(UUID.randomUUID())
                .project(project)
                .user(user)
                .name(user.getNickname())
                .build();
        return projectMemberRepository.save(projectMember);
    }
    public void removeProjectMember(UUID projectId, UUID userId) {
        ProjectMember projectMember=getProjectMember(projectId,userId);
        projectMemberRepository.delete(projectMember);
    }

    public List<ProjectMember> getProjectMembers(UUID projectId) {
        return projectMemberRepository.findAllByProject_Id(projectId);
    }
    public List<Project> getProjectsByUserId(UUID userId){
        return projectMemberRepository.findProjectsByUserId(userId);
    }
    public ProjectMember getProjectMember(UUID projectId, UUID userId) {
        return projectMemberRepository.findByProject_IdAndUser_Id(projectId,userId)
                .orElseThrow(()->new NotFoundException("User does not belong to this project"));
    }
    public boolean memberExists(UUID projectId, UUID userId) {
        return projectMemberRepository.existsByProject_IdAndUser_Id(projectId,userId);
    }
}
