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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class TaskService {

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectMemberRepository projectMemberRepository;

    public TaskResponse getTask(Long id, UUID currentUserId){

        Task task = taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found"));

        boolean isMember = projectMemberRepository.existsByProject_IdAndUser_Id(task.getProject().getId(), currentUserId);

        if(!isMember) {
            throw new BadRequestException("Not a project member");
        }

        return TaskResponse.toResponse(task);
    }

    public List<TaskResponse> getTasks(
            UUID projectId,
            UUID currentUserId,
            TaskStatus status,
            Priority priority,
            TaskType type
    ) {
        boolean isMember = projectMemberRepository
                .existsByProject_IdAndUser_Id(projectId, currentUserId);

        if (!isMember) {
            throw new BadRequestException("Not a project member");
        }


        List<Task> tasks =  taskRepository.findByFilters(projectId, status, priority, type);
        List<TaskResponse> tasksResponse = new ArrayList<TaskResponse>();
        for(Task task : tasks) {
            tasksResponse.add(TaskResponse.toResponse(task));
        }
        return tasksResponse;
    }

    public Task create(UUID projectId, UUID currentUserId, CreateTaskRequest createTaskRequest) {

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Project not found"));

        boolean isMember = projectMemberRepository.existsByProject_IdAndUser_Id(projectId, currentUserId);

        if(!isMember) {
            throw new BadRequestException("Not a project member");
        }
        Task parent = null;
        if (createTaskRequest.getParentId() != null) {
            parent = taskRepository.findById(createTaskRequest.getParentId())
                    .orElseThrow(() -> new NotFoundException("Parent task not found"));

            if (!parent.getProject().getId().equals(projectId)) {
                throw new BadRequestException("Parent task must belong to same project");
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

    public Task update(Long id, UUID currentUserId, UpdateTaskRequest updateTaskRequest){

        Task task = taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found"));

        boolean isMember = projectMemberRepository
                .existsByProject_IdAndUser_Id(task.getProject().getId(), currentUserId);

        if (!isMember) {
            throw new BadRequestException("Not a project member");
        }

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

    public String delete(Long id, UUID userId) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found"));

        ProjectMember assigner = projectMemberRepository
                .findByProject_IdAndUser_Id(task.getProject().getId(), userId)
                .orElseThrow(() -> new BadRequestException("You are not a project member"));

        if (assigner.getRole() != Role.MANAGER) {
            throw new BadRequestException("You don't have permission to delete task");
        }

        taskRepository.delete(task);

        return "Deleted successfully";
    }
}
