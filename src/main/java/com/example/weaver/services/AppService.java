package com.example.weaver.services;

import com.example.weaver.dtos.events.UserRegisteredEvent;
import com.example.weaver.dtos.events.EmailVerificationExpiredEvent;
import com.example.weaver.dtos.others.AuthUser;
import com.example.weaver.dtos.others.EmailVerificationResult;
import com.example.weaver.dtos.responses.LoginResponse;
import com.example.weaver.dtos.responses.ProjectMemberResponse;
import com.example.weaver.dtos.responses.ProjectResponse;
import com.example.weaver.enums.Role;
import com.example.weaver.enums.UserStatus;
import com.example.weaver.enums.EmailVerificationStatus;
import com.example.weaver.exceptions.BadRequestException;
import com.example.weaver.exceptions.ForbiddenException;
import com.example.weaver.models.Project;
import com.example.weaver.models.ProjectMember;
import com.example.weaver.models.User;
import com.example.weaver.services.Others.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppService {
    private final UserService userService;
    private final ProjectService projectService;
    private final ProjectMemberService projectMemberService;
    private final EmailVerificationTokenService emailService;
    private final JwtService jwtService;

    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    //USER
    public LoginResponse login(String email, String password) {
        Optional<User> user = userService.findByEmail(email);
        if (user.isEmpty() || !passwordEncoder.matches(password, user.get().getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }
        if (user.get().getStatus() != UserStatus.ACTIVE) {
            if (user.get().getStatus() == UserStatus.PENDING) {
                if (emailService.checkIfTokenExpiredByEmail(email))
                    eventPublisher.publishEvent
                            (new EmailVerificationExpiredEvent(user.get().getId(), email));

                throw new ForbiddenException("Please check your email for confirmation");
            } else {
                throw new ForbiddenException("You are banned");
            }
        }
        String accessToken = jwtService.generateAccessToken(user.get());
        String refreshToken = jwtService.generateRefreshToken(user.get());
        return new LoginResponse(accessToken, refreshToken);
    }

    @Transactional
    public void register(String email, String password) {
        String hashedPassword = passwordEncoder.encode(password);
        User user = userService.create(email, hashedPassword);

        eventPublisher.publishEvent
                (new UserRegisteredEvent(user.getId(),email));
//        emailService.sendVerificationEmail(user);
    }

    @Transactional
    public EmailVerificationResult verifyEmail(String token) {

        EmailVerificationResult result = emailService.verify(token);

        if (result.status() == EmailVerificationStatus.EXPIRED) {
            eventPublisher.publishEvent
                    (new EmailVerificationExpiredEvent(result.userId(), result.email()));
        }

        return result;
    }

    public String getNewAccessToken(String refreshToken) {
        Claims claims= jwtService.parseToken(refreshToken);
        UUID userId = jwtService.getUserId(claims);
        User user = userService.findById(userId);
        return jwtService.generateAccessToken(user);
    }

    public UUID getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return authUser.getId();
    }

    //PROJECT
    @Transactional(readOnly = true)
    public ProjectResponse getProject(UUID projectId, UUID requesterId) {
        ProjectMember member = projectMemberService.getProjectMember(projectId,requesterId);
        return ProjectResponse.toResponse(member.getProject());
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByUserId(UUID userId) {
        List<Project> projects= projectMemberService.getProjectsByUserId(userId);

        List<ProjectResponse> projectResponses = new ArrayList<>();
        for(Project project : projects) {
            projectResponses.add(ProjectResponse.toResponse(project));
        }
        return projectResponses;
    }

    @Transactional
    public ProjectResponse createProject(UUID createdBy, String name, String description, Instant finishedAt) {
        if(name == null || name.length() < 3)
            throw new BadRequestException("Please enter a name of at least 3 characters");
        User user = userService.findById(createdBy);
        Project project= projectService.create(createdBy, name, description, finishedAt);
        projectMemberService.addProjectMember(project, user, Role.MANAGER);
        return ProjectResponse.toResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID projectId,UUID requesterId,String name, String description, Instant finishedAt) {
        ProjectMember requester = projectMemberService.getProjectMember(projectId,requesterId);
        if (!requester.getRole().equals(Role.MANAGER)) {
            throw new ForbiddenException("You are not allowed to update this project");
        }
        return ProjectResponse.toResponse(
                projectService.update(requester.getProject(), name, description, finishedAt));
    }

    @Transactional
    public void deleteProject(UUID id, UUID requesterId) {
        Project project = projectService.findById(id);
        if (!requesterId.equals(project.getCreatedBy().getId())) {
            throw new ForbiddenException("You are not allowed to delete this project");
        }
        projectService.delete(project);
    }

    //PROJECT_MEMBER
    @Transactional
    public ProjectMemberResponse addProjectMember(UUID requesterId, UUID projectId, UUID newMemberId) {
        if(requesterId.equals(projectId)) {
            throw new ForbiddenException("You can't add yourself");
        }
        ProjectMember requester = projectMemberService.getProjectMember(projectId,requesterId);
        if (!requester.getRole().equals(Role.MANAGER)) {
            throw new ForbiddenException("You are not allowed to add new member to this project");
        }
        User user = userService.findById(newMemberId);
        ProjectMember newProjectMember= projectMemberService.addProjectMember(requester.getProject(), user,Role.VIEWER);
        return ProjectMemberResponse.toResponse(newProjectMember);
    }

    @Transactional
    public ProjectMemberResponse updateProjectMemberRole(UUID requesterId,
                                                 UUID projectId,
                                                 UUID userId,
                                                 Role newRole) {
        ProjectMember requester=projectMemberService.getProjectMember(projectId,requesterId);
        if(requester.getRole()!=Role.MANAGER){
            throw new ForbiddenException("You are not allowed to modify members role this project");
        }
        ProjectMember updatedMember= projectMemberService.updateProjectMemberRole(projectId,userId,newRole);
        return ProjectMemberResponse.toResponse(updatedMember);
    }

    @Transactional
    public void removeProjectMember(UUID requesterId, UUID projectId, UUID memberId) {
        //Decide what to do if the only manager requested to leave project

        ProjectMember requester=projectMemberService.getProjectMember(projectId,requesterId);
        if (!requester.getRole().equals(Role.MANAGER)) {
            throw new ForbiddenException("You are not allowed to remove member from this project");
        }
        projectMemberService.removeProjectMember(projectId, memberId);
    }

    public List<ProjectMemberResponse> getProjectMembers(UUID projectId, UUID requesterId) {
        if (!projectMemberService.memberExists(projectId, requesterId)) {
            throw new ForbiddenException("You do not belong to this project");
        }
        List<ProjectMember> projectMembers= projectMemberService.getProjectMembers(projectId);
        List<ProjectMemberResponse> projectMemberResponses = new ArrayList<>();
        for(ProjectMember projectMember : projectMembers) {
            projectMemberResponses.add(ProjectMemberResponse.toResponse(projectMember));
        }
        return projectMemberResponses;
    }

}
