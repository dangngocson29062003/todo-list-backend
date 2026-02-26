package com.example.weaver.controllers;

import com.example.weaver.dtos.AuthUser;
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
@RequestMapping("/task")
public class TaskController {

    @Autowired
    TaskService taskService;

    @GetMapping("/{projectId}/{taskId}")
    public Task getTask(@PathVariable UUID projectId, @PathVariable Long taskId, @AuthenticationPrincipal AuthUser authUser){
        return taskService.getTask(taskId, projectId, authUser.getId());
    }

    @GetMapping
    public List<TaskResponse> getTasks(@RequestParam UUID projectId,
                               @AuthenticationPrincipal AuthUser authUser,
                               @RequestParam(required = false) TaskStatus status,
                               @RequestParam(required = false) Priority priority,
                               @RequestParam(required = false) TaskType type) {
        List<Task> tasks = taskService.getTasks(projectId, authUser.getId(), status, priority, type);
        List<TaskResponse> tasksResponse = new ArrayList<TaskResponse>();
        for(Task task : tasks) {
            TaskResponse taskResponse = new TaskResponse(task.getName(), task.getDescription(), task.getStartedAt(), task.getEndedAt(), task.getType(), task.getPriority(), task.getStatus(), task.getProject().getId(), task.getParent() != null ? task.getParent().getId() : null);
            tasksResponse.add(taskResponse);
        }
        return tasksResponse;
    }

    @PostMapping
    public Task createTask(@Valid @RequestBody CreateTaskRequest request, @AuthenticationPrincipal AuthUser authUser) {
        return taskService.create(request.getProjectId(), authUser.getId(), request);
    }

    @PutMapping("/{projectId}/{taskId}")
    public Task updateTask(@Valid @RequestBody UpdateTaskRequest request, @PathVariable UUID projectId, @PathVariable Long taskId, @AuthenticationPrincipal AuthUser authUser) {
        return taskService.update(projectId, taskId, authUser.getId(), request);
    }
}
