package com.example.weaver.services;

import com.example.weaver.dtos.requests.TaskAssignmentRequest;
import com.example.weaver.dtos.responses.TaskAssignmentResponse;
import com.example.weaver.enums.Role;
import com.example.weaver.exceptions.BadRequestException;
import com.example.weaver.exceptions.NotFoundException;
import com.example.weaver.models.ProjectMember;
import com.example.weaver.models.Task;
import com.example.weaver.models.TaskAssignment;
import com.example.weaver.models.User;
import com.example.weaver.repositories.ProjectMemberRepository;
import com.example.weaver.repositories.TaskAssignmentRepository;
import com.example.weaver.repositories.TaskRepository;
import com.example.weaver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TaskAssignmentService {

    @Autowired
    TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectMemberRepository projectMemberRepository;

    public TaskAssignmentResponse assign(TaskAssignmentRequest request, UUID assignedBy) {
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new NotFoundException("Task not found"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        ProjectMember assigner = projectMemberRepository
                .findByProject_IdAndUser_Id(task.getProject().getId(), assignedBy)
                .orElseThrow(() -> new BadRequestException("You are not a project member"));

        if (assigner.getRole() != Role.MANAGER) {
            throw new BadRequestException("You don't have permission to assign task");
        }

        boolean isMember = projectMemberRepository
                .existsByProject_IdAndUser_Id(task.getProject().getId(), request.getUserId());

        if (!isMember) {
            throw new BadRequestException("User is not a project member");
        }
        TaskAssignment assignment = TaskAssignment.builder()
                .task(task)
                .user(user)
                .assignedBy(assigner.getUser())
                .build();

        taskAssignmentRepository.save(assignment);

        return TaskAssignmentResponse.toResponse(assignment);
    }
    public TaskAssignmentResponse update(Long id, UUID userId, UUID assignedBy){
        Task task = taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Task not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ProjectMember assigner = projectMemberRepository
                .findByProject_IdAndUser_Id(task.getProject().getId(), assignedBy)
                .orElseThrow(() -> new BadRequestException("You are not a project member"));

        if (assigner.getRole() != Role.MANAGER) {
            throw new BadRequestException("You don't have permission to assign task");
        }

        boolean isMember = projectMemberRepository.existsByProject_IdAndUser_Id(task.getProject().getId(), user.getId());

        if (!isMember) {
            throw new BadRequestException("User is not a project member");
        }

        TaskAssignment taskAssignment = taskAssignmentRepository.findById(id).orElseThrow(() -> new NotFoundException("Task assignment not found"));

        taskAssignment.setUser(user);
        taskAssignment.setAssignedBy(assigner.getUser());

        taskAssignmentRepository.save(taskAssignment);

        return TaskAssignmentResponse.toResponse(taskAssignment);
    }
}
