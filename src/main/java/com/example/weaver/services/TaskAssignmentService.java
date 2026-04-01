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
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    private final Instant FAR_FUTURE = Instant.parse("9999-12-31T23:59:59Z");

    public void assign(Task task, UUID userId, User assigner) {


        boolean alreadyAssigned = taskAssignmentRepository.findAllByTask_Id(task.getId())
                .stream()
                .anyMatch(taskAssignment -> taskAssignment.getUser().getId().equals(userId));

        if (alreadyAssigned) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        TaskAssignment assignment = TaskAssignment.builder()
                .task(task)
                .user(user)
                .assignedBy(assigner)
                .build();

        taskAssignmentRepository.save(assignment);
    }

    public void unassign(Task task, UUID userId) {
        TaskAssignment taskAssignment = checkAssigner(userId, task.getId());
        taskAssignmentRepository.delete(taskAssignment);
    }

    public TaskAssignment checkAssigner(UUID userId, UUID taskId) {
        return taskAssignmentRepository
                .findTaskAssignmentByTask_IdAndUser_Id(taskId, userId)
                .orElseThrow(() -> new NotFoundException("User is not assigned to this task"));
    }

    public TaskAssignment getTaskAssignedToAUser(UUID userId) {
        return null;
    }

    public TaskSimpleResponses getAssignedTasks(UUID userId, Instant lastAccessCursor, Long idCursor, int limit) {
        List<TaskSimpleResponse> responses = new ArrayList<>();
        if (idCursor == null) {
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

                return getTaskSimpleResponses(responses, tasksSlice);
            }

            // No unpinned fetched (pinned equal limit)
            return new TaskSimpleResponses(
                    responses,
                    FAR_FUTURE,
                    null,
                    true
            );
        }

        // NEXT LOAD (only unpinned)
        int pageSize = Math.min(Math.max(limit, 1), 10);

        Slice<TaskSimpleResponse> tasksSlice =
                taskAssignmentRepository.findUnpinnedTaskWithCursor(
                        userId,
                        lastAccessCursor,
                        idCursor,
                        PageRequest.of(0, pageSize)
                );

        return getTaskSimpleResponses(responses, tasksSlice);
    }

    @NonNull
    private TaskSimpleResponses getTaskSimpleResponses(List<TaskSimpleResponse> responses, Slice<TaskSimpleResponse> tasksSlice) {
        List<TaskSimpleResponse> tasks = tasksSlice.getContent();
        responses.addAll(tasks);

        TaskSimpleResponse lastTask = tasks.isEmpty()?null:tasks.getLast();

        Instant lastAccess = Optional.ofNullable(lastTask)
                .map(TaskSimpleResponse::lastAccess)
                .orElse(FAR_FUTURE);

        UUID lastId = Optional.ofNullable(lastTask)
                .map(TaskSimpleResponse::id).orElseThrow();

        return new TaskSimpleResponses(
                responses,
                lastAccess,
                lastId,
                tasksSlice.hasNext()
        );
    }

    public void updateTaskPinStatus(UUID taskId, UUID userId) {
        TaskAssignment taskAssignment = checkAssigner(userId,taskId);
        if (!taskAssignment.isPinned()) {
            long taskAssignments = taskAssignmentRepository.countByUser_IdAndIsPinnedTrue(userId);

            if (taskAssignments > 5) {
                throw new BadRequestException("You can only pin 5 tasks at a time");
            }
        }
        taskAssignment.setPinned(!taskAssignment.isPinned());
        taskAssignmentRepository.save(taskAssignment);
    }

    public void updateTaskLastAccess(UUID taskId, UUID userId) {
        TaskAssignment taskAssignment = checkAssigner(userId, taskId);
        taskAssignment.setLastAccess(Instant.now());
        taskAssignmentRepository.save(taskAssignment);
    }
}
