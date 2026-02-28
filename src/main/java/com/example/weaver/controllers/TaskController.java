package com.example.weaver.controllers;

import com.example.weaver.dtos.others.AuthUser;
import com.example.weaver.dtos.requests.CreateTaskRequest;
import com.example.weaver.dtos.requests.UpdateTaskRequest;
import com.example.weaver.dtos.responses.TaskResponse;
import com.example.weaver.enums.Priority;
import com.example.weaver.enums.TaskStatus;
import com.example.weaver.enums.TaskType;
import com.example.weaver.models.Task;
import com.example.weaver.services.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    TaskService taskService;

    @GetMapping("/{id}")
    public TaskResponse getTask(@PathVariable Long id, @AuthenticationPrincipal AuthUser authUser){
        return taskService.getTask(id, authUser.getId());
    }

    @GetMapping
    public List<TaskResponse> getTasks(@RequestParam UUID projectId,
                               @AuthenticationPrincipal AuthUser authUser,
                               @RequestParam(required = false) TaskStatus status,
                               @RequestParam(required = false) Priority priority,
                               @RequestParam(required = false) TaskType type) {
        return taskService.getTasks(projectId, authUser.getId(), status, priority, type);
    }

    @PostMapping
    public Task createTask(@Valid @RequestBody CreateTaskRequest request, @AuthenticationPrincipal AuthUser authUser) {
        return taskService.create(request.getProjectId(), authUser.getId(), request);
    }

    @PutMapping("/{id}")
    public Task updateTask(@Valid @RequestBody UpdateTaskRequest request, @PathVariable Long id, @AuthenticationPrincipal AuthUser authUser) {
        return taskService.update(id, authUser.getId(), request);
    }

    @DeleteMapping("/{id}")
    public String deleteTask(@PathVariable Long id, @AuthenticationPrincipal AuthUser authUser) {
        return taskService.delete(id, authUser.getId());
    }
}
