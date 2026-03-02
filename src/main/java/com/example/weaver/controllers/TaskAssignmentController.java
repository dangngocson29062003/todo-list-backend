package com.example.weaver.controllers;

import com.example.weaver.dtos.others.AuthUser;
import com.example.weaver.dtos.requests.TaskAssignmentRequest;
import com.example.weaver.dtos.responses.TaskAssignmentResponse;
import com.example.weaver.dtos.responses.TaskResponse;
import com.example.weaver.services.AppService;
import com.example.weaver.services.TaskAssignmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks/{id}/assignments")
public class TaskAssignmentController {

    @Autowired
    private AppService appService;

    @PostMapping
    public TaskResponse assign(@PathVariable Long id,
                               @RequestBody TaskAssignmentRequest request,
                               @AuthenticationPrincipal AuthUser authUser) {
        return appService.assignTask(id, request, authUser.getId());
    }

    @DeleteMapping("/{userId}")
    public void unassign(@Valid @PathVariable Long id,
                                 @PathVariable UUID userId,
                                 @AuthenticationPrincipal AuthUser authUser){
        appService.unassignTask(id, userId, authUser.getId());
    }
}
