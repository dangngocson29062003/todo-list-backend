package com.example.weaver.services;

import com.example.weaver.dtos.requests.CreateTaskRequest;
import com.example.weaver.dtos.requests.UpdateTaskRequest;
import com.example.weaver.dtos.responses.TaskResponse;
import com.example.weaver.enums.Priority;
import com.example.weaver.enums.Role;
import com.example.weaver.enums.TaskStatus;
import com.example.weaver.enums.TaskType;
import com.example.weaver.exceptions.BadRequestException;
import com.example.weaver.exceptions.NotFoundException;
import com.example.weaver.models.Project;
import com.example.weaver.models.ProjectMember;
import com.example.weaver.models.Task;
import com.example.weaver.repositories.ProjectMemberRepository;
import com.example.weaver.repositories.ProjectRepository;
import com.example.weaver.repositories.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public Task getTask(Long id){
        return taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found"));
    }

    public List<Task> getTasks(
            UUID projectId,
            TaskStatus status,
            Priority priority,
            TaskType type
    ) {
        return taskRepository.findByFilters(projectId, status, priority, type);
    }

    public Task create(UUID projectId, CreateTaskRequest createTaskRequest) {

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Project not found"));

        Task parent = null;

        if (createTaskRequest.getParentId() != null) {
            parent = taskRepository.findById(createTaskRequest.getParentId())
                    .orElseThrow(() -> new NotFoundException("Parent task not found"));

            if (!parent.getProject().getId().equals(projectId)) {
                throw new BadRequestException("Parent task must belong to same projectResponse");
            }
        }

        Task task = Task.builder().project(project)
                .name(createTaskRequest.getName())
                .description(createTaskRequest.getDescription())
                .startedAt(createTaskRequest.getStartedAt())
                .endedAt(createTaskRequest.getEndedAt()).type(createTaskRequest.getTaskType())
                .status(createTaskRequest.getTaskStatus() != null ? createTaskRequest.getTaskStatus() : TaskStatus.TODO)
                .priority(createTaskRequest.getPriority())
                .parent(parent)
                .build();

        return taskRepository.save(task);
    }

    public Task update(Long id, UpdateTaskRequest updateTaskRequest){
        Task task = taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found"));

        if (updateTaskRequest.getName() != null) {
            task.setName(updateTaskRequest.getName());
        }

        if (updateTaskRequest.getDescription() != null) {
            task.setDescription(updateTaskRequest.getDescription());
        }

        if (updateTaskRequest.getStartedAt() != null) {
            task.setStartedAt(updateTaskRequest.getStartedAt());
        }

        if (updateTaskRequest.getEndedAt() != null) {
            if (updateTaskRequest.getStartedAt() != null && updateTaskRequest.getEndedAt().isBefore(updateTaskRequest.getStartedAt())) {
                throw new BadRequestException("End date must be after start date");
            }
            task.setEndedAt(updateTaskRequest.getEndedAt());
        }

        if (updateTaskRequest.getTaskType() != null) {
            task.setType(updateTaskRequest.getTaskType());
        }

        if (updateTaskRequest.getTaskStatus() != null) {
            task.setStatus(updateTaskRequest.getTaskStatus());
        }

        if (updateTaskRequest.getPriority() != null) {
            task.setPriority(updateTaskRequest.getPriority());
        }

        return taskRepository.save(task);
    }

    public void delete(Task task) {
        taskRepository.delete(task);
    }
}
