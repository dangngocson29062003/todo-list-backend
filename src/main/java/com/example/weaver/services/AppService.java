package com.example.weaver.services;

import com.example.weaver.dtos.events.*;
import com.example.weaver.dtos.others.AuthUser;
import com.example.weaver.dtos.others.results.EmailVerificationResult;
import com.example.weaver.dtos.others.results.LocationResult;
import com.example.weaver.dtos.others.results.TokenResult;
import com.example.weaver.dtos.requests.*;
import com.example.weaver.dtos.responses.*;
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
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.util.function.SupplierUtils.resolve;

@Service
@RequiredArgsConstructor
public class AppService {
    private final UserService userService;
    private final ProjectService projectService;
    private final ProjectMemberService memberService;
    private final TaskService taskService;
    private final TaskAssignmentService taskAssignmentService;
    private final CommentService commentService;
    private final EmailVerificationTokenService emailService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final NotificationService notificationService;
    private final UserNotificationService userNotificationService;
    private final FileService fileService;

    private final OutboxEventService outboxEventService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final EntityManager entityManager;
    private final IpLocationService ipLocationService;
    private final ObjectMapper objectMapper;


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
                if (emailService.checkIfTokenExpiredByEmail(email)) {
                    EmailVerificationExpiredEvent event =new EmailVerificationExpiredEvent(user.getId(), email);
                    outboxEventService.create(OutboxEventTopic.NOTIFICATION_CREATED, event);
                }

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
                ? now.plus(30, ChronoUnit.DAYS)
                : now.plus(7, ChronoUnit.DAYS);
        refreshTokenService.save(hashToken(refreshToken), user.getId(), expiryDate, ip, device);

//        addRefreshTokenToCookie(refreshToken,expiryDate,response);

        return new TokenResult(UserResponse.toResponse(user), accessToken, refreshToken, expiryDate);
    }

    @Transactional
    public void register(String email, String password) {
        String hashedPassword = passwordEncoder.encode(password);
        User user = userService.create(email, hashedPassword);
        UserRegisteredEvent event = new UserRegisteredEvent(user.getId(), email);
        outboxEventService.create(OutboxEventTopic.USER_REGISTERED, event);
    }

    public UserResponse getMe(UUID userId) {
        User user = userService.findById(userId);
        return UserResponse.toResponse(user);
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


    public void logout(String refreshToken, UUID userId) {
        String hashedToken = hashToken(refreshToken);
        RefreshToken rt = refreshTokenService.findByToken(hashedToken);
        if (!rt.getUser().getId().equals(userId)) {
            throw new BadRequestException("Invalid token");
        }
        refreshTokenService.revokeValidToken(hashToken(refreshToken), Instant.now());

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
        return new TokenResult(null, accessToken, newRefreshToken, refreshToken.getExpiryDate());

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

    public List<UserResponse> searchUsers(String query, UUID userId) {
        return userService.searchUsers(query, userId).stream().map(UserResponse::toResponse).toList();
    }

    //EMAIL
    public void sendVerificationEmail(UUID uuid, String email) {
        emailService.sendVerificationEmail(uuid, email);
    }

    //PROJECT

    @Transactional
    public ProjectDetailResponse getProject(UUID userId, UUID projectId) {
        Project project = projectService.getProject(projectId, userId);
        memberService.updateProjectLastAccess(projectId, userId);
        StatsResponse stats = taskService.getStats(projectId);
        Boolean isFavorite = memberService.findIsFavorite(projectId, userId);
        return ProjectDetailResponse.toResponse(project, isFavorite, stats);
    }

    @Transactional(readOnly = true)
    public ProjectSummaryResponses getProjects(
            UUID userId,
            String name,
            String sortBy,
            Integer page,
            Integer limit,
            Boolean favorite) {
        int p = (page != null && page >= 0) ? page : 0;
        int l = (limit != null && limit > 0) ? limit : 5;
        String s = (sortBy != null) ? sortBy : "recent";

        return projectService.getProjects(userId, name, s, p, l, favorite);
    }


    @Transactional(readOnly = true)
    public List<DeletedProjectResponse> getDeletedProjects(UUID userId) {
        return projectService.getDeletedProjects(userId);
    }

    @Transactional
    public ProjectSummaryResponse createProject(CreateProjectRequest request, UUID createdBy) {
        User creator = userService.findById(createdBy);
        Project project = projectService.createProject(request, creator);
        ProjectMember manager = memberService.addProjectMember(project, creator, Role.MANAGER);
        if(!request.getMembers().isEmpty()) {
            List<UUID> userIds = request.getMembers()
                    .stream()
                    .map(ProjectMemberRequest::getUserId)
                    .toList();
            Map<UUID, User> userMap = userService.findAllByIds(userIds)
                    .stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
            for (ProjectMemberRequest member : request.getMembers()) {
                User newUser = userMap.get(member.getUserId());
                memberService.addProjectMember(project, newUser, member.getRole());
            }
        }
        return ProjectSummaryResponse.toResponse(project, manager);
    }

    @Transactional
    public ProjectDetailResponse updateProject(UUID projectId, UpdateProjectRequest request, UUID userId) {
        ProjectMember requester = memberService.getProjectMemberWithProjectLoaded(projectId, userId);
        if (!requester.getRole().equals(Role.MANAGER)) {
            throw new ForbiddenException("You are not allowed to update this projectResponse");
        }
        Project project = projectService.updateProject(projectId, request, userId);
        return ProjectDetailResponse.toResponse(project, null, null);
    }

    @Transactional
    public void softDeleteProject(UUID id, UUID requesterId) {
        ProjectMember requester = memberService.getProjectMemberWithProjectLoaded(id, requesterId);
        if (!requester.getRole().equals(Role.MANAGER)) {
            throw new ForbiddenException("You are not allowed to delete this projectResponse");
        }
        projectService.softDeleteProject(id, requester.getUser());
    }

    @Transactional
    public void restoreProject(UUID id, UUID requesterId) {
        ProjectMember requester = memberService.getProjectMemberWithProjectLoaded(id, requesterId);
        if (!requester.getRole().equals(Role.MANAGER)) {
            throw new ForbiddenException("You are not allowed to delete this projectResponse");
        }
        projectService.restoreProject(id);
    }
    @Transactional
    public void hardDeleteProject(UUID id, UUID requesterId) {
        ProjectMember requester = memberService.getProjectMemberWithProjectLoaded(id, requesterId);
        if (!requester.getRole().equals(Role.MANAGER)) {
            throw new ForbiddenException("You are not allowed to delete this projectResponse");
        }
        projectService.hardDeleteProject(id);
    }
    //PROJECT_MEMBER
    @Transactional
    public List<ProjectMemberResponse> getMembers(UUID requesterId, UUID projectId) {
        return memberService.getMembers(projectId).stream().map(ProjectMemberResponse::toResponse).toList();
    }

    @Transactional
    public ProjectMemberResponse addProjectMember(UUID requesterId, UUID projectId, ProjectMemberRequest request) {
        if (requesterId.equals(request.getUserId())) {
            throw new ForbiddenException("You can't add yourself");
        }
        ProjectMember requester = memberService.getProjectMemberWithProjectLoaded(projectId, requesterId);
        if (requester.getRole() != Role.MANAGER) {
            throw new ForbiddenException("Only managers can invite new members");
        }
        User newUser = userService.findById(request.getUserId());
        ProjectMember newProjectMember = memberService.addProjectMember(
                requester.getProject(),
                newUser,
                request.getRole()
        );

//        ProjectSummaryResponse projectSimpleResponse = ProjectSummaryResponse.toResponse(requester.getProject(), newProjectMember);
//        MemberEvent event = new MemberEvent(
//                request.getUserId(),
//                NotificationCode.MEMBER_ADDED,
//                projectSimpleResponse,
//                NotificationCategory.ANNOUNCEMENT,
//                Priority.NORMAL,
//                NotificationType.ANNOUNCEMENT);
//        outboxEventService.create(OutboxEventTopic.MEMBER_ADDED, event);

        return ProjectMemberResponse.toResponse(newProjectMember);
    }

    public void updateProjectPinStatus(UUID projectId, UUID userId) {
        memberService.updateProjectPinStatus(projectId, userId);
    }
    @Transactional
    public void updateProjectFavorite(UUID projectId, UUID userId, boolean isFavorite) {
        memberService.updateProjectFavorite(projectId, userId, isFavorite);
    }
    public void updateProjectLastAccess(UUID projectId, UUID userId) {
        memberService.updateProjectLastAccess(projectId, userId);
    }

    @Transactional
    public ProjectMemberResponse updateProjectMemberRole(UUID requesterId,
                                                         UUID projectId,
                                                         UUID userId,
                                                         Role newRole) {
        ProjectMember requester = memberService.getProjectMember(projectId, requesterId);
        if (requester.getRole() != Role.MANAGER) {
            throw new ForbiddenException("You are not allowed to modify members role this projectResponse");
        }
        ProjectMember updatedMember = memberService.updateProjectMemberRole(projectId, userId, newRole);

        ProjectMemberResponse response = ProjectMemberResponse.toResponse(updatedMember);
        MemberEvent event = new MemberEvent(
                userId,
                NotificationCode.MEMBER_ROLE_UPDATED,
                response,
                NotificationCategory.ANNOUNCEMENT,
                Priority.NORMAL,
                NotificationType.ANNOUNCEMENT
        );
        outboxEventService.create(OutboxEventTopic.MEMBER_ROLE_UPDATED, event);

        return response;
    }

    @Transactional
    public void removeProjectMember(UUID requesterId, UUID projectId, UUID memberId) {
        //Decide what to do if the only manager requested to leave projectResponse

        ProjectMember requester = memberService.getProjectMember(projectId, requesterId);
        if (!requester.getRole().equals(Role.MANAGER)) {
            throw new ForbiddenException("You are not allowed to remove member from this projectResponse");
        }
        memberService.removeProjectMember(projectId, memberId);
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
        List<UserNotification> userNotifications = userNotificationService.createMultiple(users, notification);

        NotificationCreatedEvent event =
                new NotificationCreatedEvent(userIds,
                        userNotifications.stream().map(UserNotification::getId).toList(),
                        code, payloadObject, category, priority.getRank(), type, notification.getCreatedAt());
        outboxEventService.create(OutboxEventTopic.NOTIFICATION_CREATED, event);
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
    public TaskResponse getTask(UUID taskId, UUID requesterId) {

        Task task = taskService.getTask(taskId);
        memberService.getProjectMember(task.getProject().getId(), requesterId);

        return TaskResponse.toResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskSummaryResponse> getTasks(UUID projectId,
                                       UUID requesterId,
                                       TaskStatus status,
                                       Priority priority,
                                       TaskType type) {

        memberService.getProjectMember(projectId, requesterId);
        List<Task> tasks = taskService.getTasks(projectId, status, priority, type);
        List<UUID> taskIds = tasks.stream()
                .map(Task::getId)
                .toList();
        Map<UUID, Long> commentCountMap = commentService.getCommentCountMap(taskIds);
        return tasks.stream()
                .map(task -> TaskSummaryResponse.toResponse(
                        task,
                        commentCountMap.getOrDefault(task.getId(), 0L)
                ))
                .toList();

    }


    @Transactional
    public TaskResponse createTask(UUID projectId,
                                   UUID requesterId,
                                   CreateTaskRequest createTaskRequest) {

        memberService.checkRole(projectId, requesterId);

        List<UUID> assigneeIds = createTaskRequest.getAssignees() == null
                ? List.of()
                : createTaskRequest.getAssignees().stream()
                .map(TaskAssignmentRequest::getUserId)
                .peek(userId -> {
                    if (userId == null) {
                        throw new BadRequestException("Assignee userId must not be null");
                    }
                })
                .distinct()
                .toList();

        for (UUID assigneeId : assigneeIds) {
            boolean isMember = memberService.existsMember(projectId, assigneeId);
            if (!isMember) {
                throw new BadRequestException("User is not a project member: " + assigneeId);
            }
        }

        Task task = taskService.create(projectId, createTaskRequest);

        if (!assigneeIds.isEmpty()) {
            User assigner = userService.findById(requesterId);
            for (UUID assigneeId : assigneeIds) {
                taskAssignmentService.assign(task, assigneeId, assigner);
            }
        }

        return TaskResponse.toResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(UUID id, UUID projectId, UUID requesterId, UpdateTaskRequest updateTaskRequest) {

        memberService.checkRole(projectId, requesterId);

        return TaskResponse.toResponse(taskService.update(id, updateTaskRequest));
    }

    @Transactional
    public void deleteTask(UUID id, UUID requesterId) {

        Task task = taskService.getTask(id);

        memberService.checkRole(task.getProject().getId(), requesterId);

        taskService.delete(task);
    }

    // Task Assignment
    public TaskSimpleResponses getAssignedTasks(UUID userId, Instant lastAccessCursor, Long isCursor, Integer limit) {
        return taskAssignmentService.getAssignedTasks(userId, lastAccessCursor, isCursor,
                limit != null && limit < 11 && limit > 0 ? limit : 5);
    }

    @Transactional
    public TaskResponse assignTask(UUID id, TaskAssignmentRequest request, UUID requesterId) {
        Task task = taskService.getTask(id);

        ProjectMember assigner = memberService.checkRole(task.getProject().getId(), requesterId);

        taskAssignmentService.assign(task, request.getUserId(), assigner.getUser());

        TaskResponse taskResponse = TaskResponse.toResponse(task);

        TaskAssignedEvent taskAssignedEvent = new TaskAssignedEvent(
                request.getUserId(),
                taskResponse,
                NotificationCode.TASK_ASSIGNED,
                NotificationCategory.TASK,
                Priority.HIGH,
                NotificationType.ANNOUNCEMENT
        );
        outboxEventService.create(OutboxEventTopic.TASK_ASSIGNED, taskAssignedEvent);

        return taskResponse;
    }

    @Transactional
    public void unassignTask(UUID id, UUID userId, UUID requesterId) {
        Task task = taskService.getTask(id);

        memberService.checkRole(task.getProject().getId(), requesterId);

        taskAssignmentService.unassign(task, userId);

    }

    public void updateTaskPinStatus(UUID taskId, UUID userId) {
        taskAssignmentService.updateTaskPinStatus(taskId, userId);
    }

    public void updateTaskLastAccess(UUID taskId, UUID userId) {
        taskAssignmentService.updateTaskLastAccess(taskId, userId);
    }

    //Comment
    @Transactional
    public CommentResponse createComment(UUID taskId, UUID userId, CommentRequest request) {
        Task task = taskService.getTask(taskId);
        User user = userService.findById(userId);
        memberService.checkRole(task.getProject().getId(), userId);

        return CommentResponse.toResponse(commentService.create(task, user, request));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(UUID taskId, UUID userId) {
        Task task = taskService.getTask(taskId);
        memberService.checkRole(task.getProject().getId(), userId);

        return commentService.getComments(taskId)
                .stream()
                .map(CommentResponse::toResponse)
                .toList();
    }

    @Transactional
    public CommentResponse updateComment(UUID taskId, Long id, UUID userId, String content) {
        Task task = taskService.getTask(taskId);
        memberService.checkRole(task.getProject().getId(), userId);

        Comment comment = commentService.checkAuthor(id, userId);
        return CommentResponse.toResponse(commentService.update(comment, content));

    }

    @Transactional
    public void deleteComment(UUID taskId, UUID userId, Long id) {
        Task task = taskService.getTask(taskId);
        memberService.checkRole(task.getProject().getId(), userId);

        Comment comment = commentService.checkAuthor(id, userId);

        commentService.delete(comment);
    }

    //Files
    @Transactional(readOnly = true)
    public List<FileResponse> getFiles(UUID taskId, UUID userId) {
        Task task = taskService.getTask(taskId);
        User user = userService.findById(userId);
        memberService.checkRole(task.getProject().getId(), userId);

        return fileService.getFiles(taskId).stream().map(FileResponse::toResponse).toList();
    }

    @Transactional
    public FileResponse uploadFiles(UUID taskId, UUID userId, MultipartFile file) {
        Task task = taskService.getTask(taskId);
        User user = userService.findById(userId);
        memberService.checkRole(task.getProject().getId(), userId);
        taskAssignmentService.checkAssigner(userId, taskId);

        return FileResponse.toResponse(fileService.upload(task, user, file));
    }

    @Transactional
    public void deleteFile(UUID taskId, UUID userId, UUID id) {
        Task task = taskService.getTask(taskId);
        User user = userService.findById(userId);
        memberService.checkRole(task.getProject().getId(), userId);
        taskAssignmentService.checkAssigner(userId, taskId);

        fileService.delete(id, taskId);
    }

    /// /////////////////////
    public static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of()
                    .formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String extractIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header != null && !header.isBlank()) {
            return header.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public static void addRefreshTokenToCookie(String refreshToken,
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

    private static Duration cookieMaxAge(Instant expiryDate) {
        Duration duration = Duration.between(Instant.now(), expiryDate);
        return duration.isNegative() ? Duration.ZERO : duration;
    }


}
