package com.example.weaver.services;

import com.example.weaver.dtos.requests.TaskAssignmentRequest;
import com.example.weaver.dtos.responses.TaskSimpleResponse;
import com.example.weaver.dtos.responses.TaskSimpleResponses;
import com.example.weaver.exceptions.BadRequestException;
import com.example.weaver.exceptions.NotFoundException;
import com.example.weaver.models.Task;
import com.example.weaver.models.TaskAssignment;
import com.example.weaver.models.User;
import com.example.weaver.repositories.ProjectMemberRepository;
import com.example.weaver.repositories.TaskAssignmentRepository;
import com.example.weaver.repositories.TaskRepository;
import com.example.weaver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
        TaskAssignment taskAssignment = checkAssigner(userId, task.getId());
        taskAssignmentRepository.delete(taskAssignment);
    }

    public TaskAssignment checkAssigner(UUID userId, Long taskId) {
        return taskAssignmentRepository
                .findTaskAssignmentByTask_IdAndUser_Id(taskId, userId)
                .orElseThrow(() -> new NotFoundException("User is not assigned to this task"));
    }

    public TaskAssignment getTaskAssignedToAUser(UUID userId){
        return null;
    }

    public TaskSimpleResponses getAssignedTasks(UUID userId, Integer cursor, int limit) {
        List<TaskSimpleResponse> responses=new ArrayList<>();
        if (cursor == null) {
            List<TaskSimpleResponse> pinned = taskAssignmentRepository.findPinnedTask(userId);
            responses.addAll(pinned);
            int size = limit - responses.size();

            // Only fetch unpinned if there's space
            if (size > 0) {
                Slice<TaskSimpleResponse> tasksSlice =
                        taskAssignmentRepository.findUnpinnedTask(
                                userId,
                                PageRequest.of(0, size)
                        );

                List<TaskSimpleResponse> tasks = tasksSlice.getContent();
                responses.addAll(tasks);

                Integer lastCursor = tasks.isEmpty() ? null : tasks.getLast().index();

                return new TaskSimpleResponses(
                        responses,
                        lastCursor,
                        tasksSlice.hasNext()
                );
            }

            // No unpinned fetched (pinned equal limit)
            return new TaskSimpleResponses(
                    responses,
                    Integer.MAX_VALUE,
                    true
            );
        }

        // NEXT LOAD (only unpinned)
        int pageSize = Math.min(Math.max(limit, 1), 10);

        Slice<TaskSimpleResponse> tasksSlice =
                taskAssignmentRepository.findUnpinnedTaskWithCursor(
                        userId,
                        cursor,
                        PageRequest.of(0, pageSize)
                );

        List<TaskSimpleResponse> tasks = tasksSlice.getContent();
        responses.addAll(tasks);

        Integer lastCursor = tasks.isEmpty() ? null : tasks.getLast().index();

        return new TaskSimpleResponses(
                responses,
                lastCursor,
                tasksSlice.hasNext()
        );}

    public void updateTaskIndex(Long taskId, UUID userId, int index) {
        TaskAssignment taskAssignment= checkAssigner(userId, taskId);
        taskAssignment.setTaskIndex(index);
        taskAssignmentRepository.save(taskAssignment);
    }

    public void updateTaskLastAccess(Long taskId, UUID userId, Instant lastAccess) {
        TaskAssignment taskAssignment= checkAssigner(userId, taskId);
        taskAssignment.setLastAccess(lastAccess);
        taskAssignmentRepository.save(taskAssignment);
    }
}
