package com.example.weaver.controllers;

import com.example.weaver.dtos.others.AuthUser;
import com.example.weaver.dtos.responses.UserNotificationResponse;
import com.example.weaver.enums.NotificationCategory;
import com.example.weaver.models.Notification;
import com.example.weaver.models.UserNotification;
import com.example.weaver.services.AppService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/user-notifications")
@RequiredArgsConstructor
public class UserNotificationController {
    private final AppService appService;

    @GetMapping("")
    public List<UserNotificationResponse> getNotifications(
            @RequestParam(required = false) NotificationCategory category,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) Integer cursorPriorityRank,
            @RequestParam(required = false) Instant cursorCreatedAt,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal AuthUser user
    ) {
        return appService.getUserNotifications(
                user.getId(),
                category,
                isRead,
                cursorPriorityRank,
                cursorCreatedAt,
                limit
        );
    }
    @PostMapping("/{id}")
    public void markAsRead(@PathVariable Long id, @AuthenticationPrincipal AuthUser user){
        appService.markUserNotificationAsRead(user.getId(),id);
    }
}
