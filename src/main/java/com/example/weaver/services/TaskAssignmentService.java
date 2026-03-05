package com.example.weaver.services;

import com.example.weaver.dtos.requests.TaskAssignmentRequest;
import com.example.weaver.dtos.responses.TaskAssignmentResponse;
import com.example.weaver.dtos.responses.TaskResponse;
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

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public void assign(Task task, TaskAssignmentRequest request, User assigner) {
        List<UUID> projectMemberIds = projectMemberRepository
                .findAllByProject_Id(task.getProject().getId())
                .stream()
                .map(member -> member.getUser().getId())
                .toList();

        List<UUID> invalidIds = request.getUserIds().stream()
                .filter(userId -> !projectMemberIds.contains(userId))
                .toList();

        if (!invalidIds.isEmpty()) {
            throw new BadRequestException("Some users are not projectResponse members: " + invalidIds);
        }

        List<UUID> alreadyAssignedIds = taskAssignmentRepository.findAllByTask_Id(task.getId())
                .stream()
                .map(ta -> ta.getUser().getId())
                .toList();

        List<TaskAssignment> newAssignments = request.getUserIds().stream()
                .filter(userId -> !alreadyAssignedIds.contains(userId))
                .map(userId -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new NotFoundException("User not found: " + userId));
                    return TaskAssignment.builder()
                            .task(task)
                            .user(user)
                            .assignedBy(assigner)
                            .build();
                })
                .toList();

        taskAssignmentRepository.saveAll(newAssignments);
    }

    public void unassign(Task task, UUID userId) {
        TaskAssignment taskAssignment = taskAssignmentRepository.findTaskAssignmentByTask_IdAndUser_Id(task.getId(), userId).orElseThrow(() -> new NotFoundException("User is not assigned to this task"));

        taskAssignmentRepository.delete(taskAssignment);
    }


}
