package com.example.weaver.controllers;

import com.example.weaver.dtos.AuthUser;
import com.example.weaver.dtos.requests.CreateTaskRequest;
import com.example.weaver.dtos.responses.TaskResponse;
import com.example.weaver.models.Task;
import com.example.weaver.services.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    TaskService taskService;

    @GetMapping
    public String getTasks() {
        return "ABC";
    }

    @PostMapping
    public Task createTask(@Valid @RequestBody CreateTaskRequest request, @AuthenticationPrincipal AuthUser authUser) {
        return taskService.create(request.getProjectId(), authUser.getId(), request.getName(), request.getDescription(), request.getStartedAt(), request.getEndedAt(), request.getTaskType(), request.getPriority());
    }
}
