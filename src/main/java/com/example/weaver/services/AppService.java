package com.example.weaver.services;

import com.example.weaver.dtos.events.TaskAssignedEvent;
import com.example.weaver.dtos.events.MemberAddedEvent;
import com.example.weaver.dtos.events.UserRegisteredEvent;
import com.example.weaver.dtos.events.EmailVerificationExpiredEvent;
import com.example.weaver.dtos.others.AuthUser;
import com.example.weaver.dtos.others.results.EmailVerificationResult;
import com.example.weaver.dtos.others.results.LocationResult;
import com.example.weaver.dtos.others.results.TokenResult;
import com.example.weaver.dtos.requests.CreateTaskRequest;
import com.example.weaver.dtos.requests.TaskAssignmentRequest;
import com.example.weaver.dtos.requests.UpdateTaskRequest;
import com.example.weaver.dtos.responses.ActiveSessionResponse;
import com.example.weaver.dtos.responses.ProjectMemberResponse;
import com.example.weaver.dtos.responses.ProjectResponse;
import com.example.weaver.dtos.responses.UserNotificationResponse;
import com.example.weaver.dtos.responses.TaskResponse;
import com.example.weaver.enums.*;
import com.example.weaver.exceptions.BadRequestException;
import com.example.weaver.exceptions.ForbiddenException;
import com.example.weaver.exceptions.InvalidTokenException;
import com.example.weaver.models.*;
import com.example.weaver.services.Others.IpLocationService;
import com.example.weaver.services.Others.JwtService;
import com.example.weaver.services.Others.KafkaEventProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.util.function.SupplierUtils.resolve;

@Service
@RequiredArgsConstructor
public class AppService {
    private final UserService userService;
    private final ProjectService projectService;
    private final ProjectMemberService projectMemberService;
    private final TaskService taskService;
    private final TaskAssignmentService taskAssignmentService;
    private final EmailVerificationTokenService emailService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final NotificationService notificationService;
    private final UserNotificationService userNotificationService;

    private final OutboxEventService outboxEventService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final EntityManager entityManager;
    private final IpLocationService ipLocationService;
    private final ObjectMapper objectMapper;
    private final KafkaEventProducer kafkaEventProducer;


    //USER
    @Transactional
    public TokenResult login(String email, String password, Boolean rememberMe,
                             HttpServletRequest request) {
        Optional<User> optionalUser = userService.findByEmail(email);
        if (optionalUser.isEmpty() || !passwordEncoder.matches(password, optionalUser.get().getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }
        User user = optionalUser.get();
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
        String refreshToken = UUID.randomUUID().toString();
        String ip = extractIp(request);
        String device = request.getHeader("User-Agent");

        Instant now = Instant.now();
        Instant expiryDate = Boolean.TRUE.equals(rememberMe)
                ? now.plus(7, ChronoUnit.DAYS)
                : now.plus(6, ChronoUnit.HOURS);
        refreshTokenService.save(hashToken(refreshToken), user.getId(), expiryDate, ip, device);

//        addRefreshTokenToCookie(refreshToken,expiryDate,response);

        return new TokenResult(accessToken, refreshToken, expiryDate);
    }

    @Transactional
    public void register(String email, String password) {
        String hashedPassword = passwordEncoder.encode(password);
        User user = userService.create(email, hashedPassword);

        eventPublisher.publishEvent
                (new UserRegisteredEvent(user.getId(), email));
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
        if (oldRefreshToken == null) {
            throw new InvalidTokenException();
        }
        String hashedToken = hashToken(oldRefreshToken);
        RefreshToken refreshToken = refreshTokenService.findByToken(hashedToken);

        int result = refreshTokenService.revokeValidToken(hashedToken, Instant.now());
        if (result == 0) {
            throw new InvalidTokenException();
        }

        User user = entityManager.getReference(User.class, refreshToken.getUser().getId());
        String newRefreshToken = UUID.randomUUID().toString();
        String ip = extractIp(request);
        String device = request.getHeader("User-Agent");

        refreshTokenService.save(hashToken(newRefreshToken), user.getId(),
                refreshToken.getExpiryDate(), ip, device);

//        addRefreshTokenToCookie(newRefreshToken,result.expiryDate(),response);

        String accessToken = jwtService.generateAccessToken(user);
        return new TokenResult(accessToken, newRefreshToken, refreshToken.getExpiryDate());

    }

    public List<ActiveSessionResponse> getActiveSession(UUID userId) {
        List<RefreshToken> activeSessions = refreshTokenService.getActiveSessions(userId);

        List<ActiveSessionResponse> responses = new ArrayList<>();
        Set<String> ipsNeedToResolve = new HashSet<>();

        // Map IP -> tokens (many sessions can share same IP)
        Map<String, List<RefreshToken>> tokensByIp = new HashMap<>();

        for (RefreshToken session : activeSessions) {
            tokensByIp.computeIfAbsent(session.getIpAddress(), k -> new ArrayList<>())
                    .add(session);

            if (session.getCity() == null) {
                ipsNeedToResolve.add(session.getIpAddress());
            }
        }

        Map<String, LocationResult> locationResultMap = Map.of();

        if (!ipsNeedToResolve.isEmpty()) {
            locationResultMap = ipLocationService.resolve(new ArrayList<>(ipsNeedToResolve));
        }

        List<RefreshToken> tokensToUpdate = new ArrayList<>();
        for (Map.Entry<String, LocationResult> entry : locationResultMap.entrySet()) {
            String ip = entry.getKey();
            LocationResult loc = entry.getValue();
            if (loc == null) {
                continue;
            }

            List<RefreshToken> tokens = tokensByIp.get(ip);
            if (tokens == null) continue;

            for (RefreshToken token : tokens) {
                if (token.getCity() == null) {
                    token.setCity(loc.city());
                    token.setCountry(loc.country());
                    tokensToUpdate.add(token);
                }
            }
        }

        if (!tokensToUpdate.isEmpty()) {
            refreshTokenService.saveAll(tokensToUpdate);
        }

        for (RefreshToken session : activeSessions) {
            String location = "Unknown";
            if (session.getCity() != null && session.getCountry() != null) {
                location = session.getCity() + ", " + session.getCountry();
            }
            responses.add(new ActiveSessionResponse(
                    location,
                    session.getDeviceInfo(),
                    session.getLastUsedAt()
            ));
        }

        return responses;
    }

    @Transactional
    public void forceLogoutOtherSessions(UUID userId, String refreshToken) {
        if (refreshToken == null) {
            throw new InvalidTokenException();
        }
        refreshTokenService.forceLogoutOtherSessions(userId, hashToken(refreshToken));

    }

    public UUID getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return authUser.getId();
    }

    //PROJECT
//    @Transactional(readOnly = true)
    public ProjectResponse getProject(UUID projectId, UUID requesterId) {
        ProjectMember member = projectMemberService.getProjectMember(projectId, requesterId);
        return ProjectResponse.toResponse(member.getProject());
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByUserId(UUID userId) {
        List<Project> projects = projectMemberService.getProjectsByUserId(userId);

        List<ProjectResponse> projectResponses = new ArrayList<>();
        for (Project project : projects) {
            projectResponses.add(ProjectResponse.toResponse(project));
        }
        return projectResponses;
    }

    @Transactional
    public ProjectResponse createProject(UUID createdBy, String name, String description, Instant finishedAt) {
        if (name == null || name.length() < 3)
            throw new BadRequestException("Please enter a name of at least 3 characters");
        User user = userService.findById(createdBy);
        Project project = projectService.create(createdBy, name, description, finishedAt);
        projectMemberService.addProjectMember(project, user, Role.MANAGER);
        return ProjectResponse.toResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID projectId, UUID requesterId, String name, String description, Instant finishedAt) {
        ProjectMember requester = projectMemberService.getProjectMemberWithProjectLoaded(projectId, requesterId);
        if (!requester.getRole().equals(Role.MANAGER)) {
            throw new ForbiddenException("You are not allowed to update this projectResponse");
        }
        return ProjectResponse.toResponse(
                projectService.update(requester.getProject(), name, description, finishedAt));
    }

    @Transactional
    public void deleteProject(UUID id, UUID requesterId) {
        Project project = projectService.findById(id);
        if (!requesterId.equals(project.getCreatedBy().getId())) {
            throw new ForbiddenException("You are not allowed to delete this projectResponse");
        }
        projectService.delete(project);
    }

    //PROJECT_MEMBER
    @Transactional
    public ProjectMemberResponse addProjectMember(UUID requesterId, UUID projectId, UUID newMemberId) {
        if (requesterId.equals(projectId)) {
            throw new ForbiddenException("You can't add yourself");
        }
        ProjectMember requester = projectMemberService.getProjectMemberWithProjectLoaded(projectId, requesterId);
        if (!requester.getRole().equals(Role.MANAGER)) {
            throw new ForbiddenException("You are not allowed to add new member to this projectResponse");
        }
        User user = userService.findById(newMemberId);
        ProjectMember newProjectMember = projectMemberService.addProjectMember(requester.getProject(), user, Role.VIEWER);

        ProjectResponse projectResponse=ProjectResponse.toResponse(requester.getProject());
        MemberAddedEvent event = new MemberAddedEvent(
                newMemberId,
                NotificationCode.MEMBER_ADDED,
                projectResponse,
                NotificationCategory.ANNOUNCEMENT,
                Priority.NORMAL,
                NotificationType.ANNOUNCEMENT);
        outboxEventService.create(OutboxEventTopic.MemberAdded, event);

        return ProjectMemberResponse.toResponse(newProjectMember);
    }

    @Transactional
    public ProjectMemberResponse updateProjectMemberRole(UUID requesterId,
                                                         UUID projectId,
                                                         UUID userId,
                                                         Role newRole) {
        ProjectMember requester = projectMemberService.getProjectMember(projectId, requesterId);
        if (requester.getRole() != Role.MANAGER) {
            throw new ForbiddenException("You are not allowed to modify members role this projectResponse");
        }
        ProjectMember updatedMember = projectMemberService.updateProjectMemberRole(projectId, userId, newRole);
        return ProjectMemberResponse.toResponse(updatedMember);
    }

    @Transactional
    public void removeProjectMember(UUID requesterId, UUID projectId, UUID memberId) {
        //Decide what to do if the only manager requested to leave projectResponse

        ProjectMember requester = projectMemberService.getProjectMember(projectId, requesterId);
        if (!requester.getRole().equals(Role.MANAGER)) {
            throw new ForbiddenException("You are not allowed to remove member from this projectResponse");
        }
        projectMemberService.removeProjectMember(projectId, memberId);
    }

    public List<ProjectMemberResponse> getProjectMembers(UUID projectId, UUID requesterId) {
        if (!projectMemberService.memberExists(projectId, requesterId)) {
            throw new ForbiddenException("You do not belong to this projectResponse");
        }
        List<ProjectMember> projectMembers = projectMemberService.getProjectMembers(projectId);
        List<ProjectMemberResponse> projectMemberResponses = new ArrayList<>();
        for (ProjectMember projectMember : projectMembers) {
            projectMemberResponses.add(ProjectMemberResponse.toResponse(projectMember));
        }
        return projectMemberResponses;
    }

    //NOTIFICATION
    //USER_NOTIFICATION
    @Transactional(readOnly = true)
    public List<UserNotificationResponse> getUserNotifications(UUID userId, NotificationCategory category,
                                                               Boolean isRead, Integer cursorPriorityRank,
                                                               Instant cursorCreatedAt, int limit) {
        List<UserNotification> userNotifications =
                userNotificationService.getUserNotifications(userId, category, isRead,
                        cursorPriorityRank, cursorCreatedAt, limit);

        return userNotifications.stream().map(UserNotificationResponse::toResponse).toList();
    }

    @Transactional
    public void createNotifications(List<UUID> userIds,
                                    NotificationCode code, Object payloadObject,
                                    NotificationCategory category,
                                    Priority priority, NotificationType type) {
        //UserNotification userId+notificationId unique constraint will catch duplicate error
        Notification notification = notificationService.create(code, payloadObject, category, priority.getRank(), type);
        List<User> users = new ArrayList<>();
        for (UUID userId : userIds) {
            users.add(entityManager.getReference(User.class, userId));
        }
        userNotificationService.createMultiple(users, notification);
    }

    public void markUserNotificationAsRead(UUID userId, Long userNotificationId) {
        UserNotification userNotification = userNotificationService.findByIdWithUserLoaded(userNotificationId);
        if (!userId.equals(userNotification.getUser().getId())) {
            throw new ForbiddenException("You don't have permission to set this notification as read");
        }
        userNotificationService.setRead(userNotification);
    }

    // Tasks
    @Transactional(readOnly = true)
    public TaskResponse getTask(Long taskId, UUID requesterId) {

        Task task = taskService.getTask(taskId);
        projectMemberService.getProjectMember(task.getProject().getId(), requesterId);

        return TaskResponse.toResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasks(UUID projectId,
                                       UUID requesterId,
                                       TaskStatus status,
                                       Priority priority,
                                       TaskType type) {

        projectMemberService.getProjectMember(projectId, requesterId);

        return taskService.getTasks(projectId, status, priority, type)
                .stream()
                .map(TaskResponse::toResponse)
                .toList();

    }


    @Transactional
    public TaskResponse createTask(UUID projectId,
                                   UUID requesterId,
                                   CreateTaskRequest createTaskRequest) {

        projectMemberService.checkRole(projectId, requesterId);

        return TaskResponse.toResponse(taskService.create(projectId, createTaskRequest));
    }

    @Transactional
    public TaskResponse updateTask(Long id, UUID requesterId, UpdateTaskRequest updateTaskRequest) {

        Task task = taskService.getTask(id);

        projectMemberService.checkRole(task.getProject().getId(), requesterId);

        return TaskResponse.toResponse(taskService.update(id, updateTaskRequest));
    }

    @Transactional
    public void deleteTask(Long id, UUID requesterId) {

        Task task = taskService.getTask(id);

        projectMemberService.checkRole(task.getProject().getId(), requesterId);

        taskService.delete(task);
    }

    // Task Assignment
    @Transactional
    public TaskResponse assignTask(Long id, TaskAssignmentRequest request, UUID requesterId) {
        Task task = taskService.getTask(id);

        ProjectMember assigner = projectMemberService.checkRole(task.getProject().getId(), requesterId);

        taskAssignmentService.assign(task, request, assigner.getUser());

        TaskResponse taskResponse = TaskResponse.toResponse(task);

        TaskAssignedEvent taskAssignedEvent =new TaskAssignedEvent(
                request.getUserIds(),
                taskResponse,
                NotificationCode.TASK_ASSIGNED,
                NotificationCategory.TASK,
                Priority.HIGH,
                NotificationType.ANNOUNCEMENT
                );
        outboxEventService.create(OutboxEventTopic.TaskAssigned, taskAssignedEvent);

        return taskResponse;
    }

    @Transactional
    public void unassignTask(Long id, UUID userId, UUID requesterId) {
        Task task = taskService.getTask(id);

        projectMemberService.checkRole(task.getProject().getId(), requesterId);

        taskAssignmentService.unassign(task, userId);

    }

    /// /////////////////////
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
