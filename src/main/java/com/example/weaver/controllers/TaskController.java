package com.example.weaver.controllers;

import com.example.weaver.dtos.others.AuthUser;
import com.example.weaver.dtos.requests.CreateTaskRequest;
import com.example.weaver.dtos.requests.UpdateTaskRequest;
import com.example.weaver.dtos.responses.TaskResponse;
import com.example.weaver.dtos.responses.TaskSimpleResponses;
import com.example.weaver.dtos.responses.TaskSummaryResponse;
import com.example.weaver.enums.Priority;
import com.example.weaver.enums.TaskStatus;
import com.example.weaver.enums.TaskType;
import com.example.weaver.services.AppService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects/{projectId}/tasks")
public class TaskController {

    @Autowired
    private AppService appService;

    @GetMapping("/{id}")
    public TaskResponse getTask(@PathVariable UUID id, @AuthenticationPrincipal AuthUser authUser){
        return appService.getTask(id, authUser.getId());
    }

    @GetMapping("/simple")
    public TaskSimpleResponses getTasksAssignedToUser(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(name = "lastAccessCursor",required = false) Instant lastAccessCursor,
            @RequestParam(name = "idCursor",required = false) Long idCursor,
            @RequestParam(name = "limit",required = false) Integer limit) {
        return appService.getAssignedTasks(authUser.getId(),lastAccessCursor,idCursor,limit);
    }

    @GetMapping
    public List<TaskSummaryResponse> getTasks(@PathVariable UUID projectId,
                                              @AuthenticationPrincipal AuthUser authUser,
                                              @RequestParam(required = false) TaskStatus status,
                                              @RequestParam(required = false) Priority priority,
                                              @RequestParam(required = false) TaskType type) {
        return appService.getTasks(projectId, authUser.getId(), status, priority, type);
    }

    @PostMapping
    public TaskResponse createTask(@PathVariable UUID projectId, @Valid @RequestBody CreateTaskRequest request, @AuthenticationPrincipal AuthUser authUser) {
        return appService.createTask(projectId, authUser.getId(), request);
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(@Valid @RequestBody UpdateTaskRequest request, @PathVariable UUID projectId, @PathVariable UUID id, @AuthenticationPrincipal AuthUser authUser) {
        return appService.updateTask(id, projectId, authUser.getId(), request);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable UUID id, @AuthenticationPrincipal AuthUser authUser) {
        appService.deleteTask(id, authUser.getId());
    }
}
