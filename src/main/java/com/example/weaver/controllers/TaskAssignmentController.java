package com.example.weaver.controllers;

import com.example.weaver.dtos.others.AuthUser;
import com.example.weaver.dtos.requests.TaskAssignmentRequest;
import com.example.weaver.dtos.responses.TaskAssignmentResponse;
import com.example.weaver.services.TaskAssignmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/assignments")
public class TaskAssignmentController {

    @Autowired
    TaskAssignmentService taskAssignmentService;

    @PostMapping
    public TaskAssignmentResponse assign(@Valid @RequestBody TaskAssignmentRequest request, @AuthenticationPrincipal AuthUser authUser) {
        return taskAssignmentService.assign(request, authUser.getId());
    }

    @PutMapping("/{id}")
    public TaskAssignmentResponse update(@Valid @PathVariable Long id, @RequestBody UUID userId, @AuthenticationPrincipal AuthUser authUser){
        return taskAssignmentService.update(id, userId, authUser.getId());
    }
}
