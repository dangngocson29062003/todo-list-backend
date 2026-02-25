package com.example.weaver.services;

import com.example.weaver.dtos.responses.LoginResponse;
import com.example.weaver.exceptions.BadRequestException;
import com.example.weaver.exceptions.ForbiddenException;
import com.example.weaver.models.Project;
import com.example.weaver.models.ProjectMember;
import com.example.weaver.models.User;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppService {
    private final UserService userService;
    private final ProjectService projectService;
    private final ProjectMemberService projectMemberService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    //USER
    public LoginResponse login(String email, String password){
        Optional<User> user=userService.findByEmail(email);
        if(user.isEmpty() || !passwordEncoder.matches(password,user.get().getPassword())){
            throw new BadRequestException("Invalid email or password");
        }
        String accessToken= jwtService.generateAccessToken(user.get());
        String refreshToken= jwtService.generateRefreshToken(user.get());
        return new LoginResponse(accessToken,refreshToken);
    }

    @Transactional
    public LoginResponse register(String email,String password){
        String hashedPassword = passwordEncoder.encode(password);
        User user= userService.create(email,hashedPassword);
        String accessToken= jwtService.generateAccessToken(user);
        String refreshToken= jwtService.generateRefreshToken(user);
        return new LoginResponse(accessToken,refreshToken);
    }

    public String getNewAccessToken(String refreshToken){
        if(!jwtService.validateToken(refreshToken)){
            throw new BadRequestException("Invalid refresh token");
        }
        UUID userId = jwtService.getUserId(refreshToken);
        User user=userService.findById(userId);
        return jwtService.generateAccessToken(user);
    }

    //PROJECT
    @Transactional(readOnly = true)
    public Project getProject(UUID projectId,UUID requesterId){
        ProjectMember member=projectMemberService.getProjectMember(projectId,requesterId);
        return member.getProject();
    }

    public List<Project> getProjectsByUserId(UUID userId){
        return projectMemberService.getProjectsByUserId(userId);
    }

    @Transactional
    public Project createProject(UUID createdBy, String name,String description, Instant finishedAt){
        return projectService.create(createdBy, name, description, finishedAt);
    }
    @Transactional
    public Project updateProject(UUID requesterId, UUID id, String name,String description,Instant finishedAt){
        Project project=projectService.findById(id);
        if(!requesterId.equals(project.getId())){
            throw new ForbiddenException("You are not allowed to update this project");
        }
        return projectService.update(project, name, description, finishedAt);
    }
    @Transactional
    public void deleteProject(UUID id,UUID requesterId){
        Project project=projectService.findById(id);
        if(!requesterId.equals(project.getCreatedBy())) {
            throw new ForbiddenException("You are not allowed to delete this project");
        }
        projectService.delete(project);
    }

    //PROJECT_MEMBER
    @Transactional
    public ProjectMember addProjectMember(UUID requesterId,UUID projectId,UUID newMemberId){
        Project project=projectService.findById(projectId);
        if(!requesterId.equals(project.getCreatedBy())) {
            throw new ForbiddenException("You are not allowed to add new member to this project");
        }
        User user=userService.findById(newMemberId);
        return projectMemberService.addProjectMember(project,user);
    }

    @Transactional
    public void removeProjectMember(UUID requesterId,UUID projectId,UUID memberId){
        Project project=projectService.findById(projectId);
        if(!requesterId.equals(project.getCreatedBy())) {
            throw new ForbiddenException("You are not allowed to remove member from this project");
        }
        projectMemberService.removeProjectMember(projectId,memberId);
    }

    public List<ProjectMember> getProjectMembers(UUID projectId,UUID requesterId){
        projectMemberService.memberExists(projectId,requesterId);
        return projectMemberService.getProjectMembers(projectId);
    }

}
