package com.example.weaver.services;

import com.example.weaver.enums.Priority;
import com.example.weaver.enums.TaskStatus;
import com.example.weaver.enums.TaskType;
import com.example.weaver.exceptions.BadRequestException;
import com.example.weaver.exceptions.NotFoundException;
import com.example.weaver.models.Project;
import com.example.weaver.models.Task;
import com.example.weaver.repositories.ProjectMemberRepository;
import com.example.weaver.repositories.ProjectRepository;
import com.example.weaver.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskService {
    @Autowired
    TaskRepository taskRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectMemberRepository projectMemberRepository;

    @Autowired
    AppService appService;

    public Task create(UUID projectID, UUID currentUserId, String name, String description, Instant startedAt, Instant endedAt, TaskType type, Priority priority) {

        Project project = projectRepository.findById(projectID).orElseThrow(() -> new NotFoundException("Project not found"));

        boolean isMember = projectMemberRepository.existsByProject_IdAndUser_Id(projectID, currentUserId);

        if(!isMember) {
            throw new BadRequestException("Not a project member");
        }

        Task task = Task.builder().name(name).description(description).startedAt(startedAt).endedAt(endedAt).type(type).priority(priority).build();

        return taskRepository.save(task);
    }
}
