package com.example.weaver.services;

import com.example.weaver.dtos.events.UserRegisteredEvent;
import com.example.weaver.dtos.events.EmailVerificationExpiredEvent;
import com.example.weaver.dtos.others.AuthUser;
import com.example.weaver.dtos.others.EmailVerificationResult;
import com.example.weaver.dtos.others.RevokeValidTokenResult;
import com.example.weaver.dtos.others.TokenResult;
import com.example.weaver.dtos.responses.ProjectMemberResponse;
import com.example.weaver.dtos.responses.ProjectResponse;
import com.example.weaver.enums.Role;
import com.example.weaver.enums.UserStatus;
import com.example.weaver.enums.EmailVerificationStatus;
import com.example.weaver.exceptions.BadRequestException;
import com.example.weaver.exceptions.ForbiddenException;
import com.example.weaver.exceptions.InvalidTokenException;
import com.example.weaver.models.Project;
import com.example.weaver.models.ProjectMember;
import com.example.weaver.models.User;
import com.example.weaver.services.Others.JwtService;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AppService {
    private final UserService userService;
    private final ProjectService projectService;
    private final ProjectMemberService projectMemberService;
    private final EmailVerificationTokenService emailService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final EntityManager entityManager;

    //USER
    @Transactional
    public TokenResult login(String email, String password, Boolean rememberMe,
                             HttpServletRequest request) {
        Optional<User> optionalUser = userService.findByEmail(email);
        if (optionalUser.isEmpty() || !passwordEncoder.matches(password, optionalUser.get().getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }
        User user=optionalUser.get();
        if (user.getStatus() != UserStatus.ACTIVE) {
            if (user.getStatus() == UserStatus.PENDING) {
                if (emailService.checkIfTokenExpiredByEmail(email))
                    eventPublisher.publishEvent
                            (new EmailVerificationExpiredEvent(user.getId(), email));

                throw new ForbiddenException("Please check your email for confirmation");
            } else {
                throw new ForbiddenException("You are banned");
            }
        }
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken=UUID.randomUUID().toString();
        String ip = extractIp(request);
        String device = request.getHeader("User-Agent");

        Instant now = Instant.now();
        Instant expiryDate = Boolean.TRUE.equals(rememberMe)
                ? now.plus(30, ChronoUnit.DAYS)
                : now.plus(6, ChronoUnit.HOURS);
        refreshTokenService.save(hashToken(refreshToken),user.getId(), expiryDate, ip,device);

//        addRefreshTokenToCookie(refreshToken,expiryDate,response);

        return new TokenResult(accessToken,refreshToken,expiryDate);
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

    @Transactional
    public TokenResult getNewAccessToken(String oldRefreshToken,
                                  HttpServletRequest request) {
        String hashedToken=hashToken(oldRefreshToken);
        RevokeValidTokenResult result =refreshTokenService.revokeValidToken(hashedToken,Instant.now());
        if (result== null) {
            throw new InvalidTokenException();
        }

        User user= entityManager.getReference(User.class, result.userId());
        String newRefreshToken=UUID.randomUUID().toString();
        String ip = extractIp(request);
        String device = request.getHeader("User-Agent");

        refreshTokenService.save(hashToken(newRefreshToken),user.getId(),
                result.expiryDate(),ip,device);

//        addRefreshTokenToCookie(newRefreshToken,result.expiryDate(),response);

        String accessToken= jwtService.generateAccessToken(user);
        return new TokenResult(accessToken,newRefreshToken,result.expiryDate());

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


    ////////////////////////
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of()
                    .formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private String extractIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header != null && !header.isBlank()) {
            return header.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    public void addRefreshTokenToCookie(String refreshToken,
                                         Instant expiryDate,
                                         HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) ///////////set true when not on local dev
                .path("/")
                .sameSite("Lax")
                .maxAge(cookieMaxAge(expiryDate))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
    private Duration cookieMaxAge(Instant expiryDate) {
        Duration duration = Duration.between(Instant.now(), expiryDate);
        return duration.isNegative() ? Duration.ZERO : duration;
    }
}
