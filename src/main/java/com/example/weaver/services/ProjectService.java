package com.example.weaver.services;

import com.example.weaver.exceptions.BadRequestException;
import com.example.weaver.exceptions.ForbiddenException;
import com.example.weaver.exceptions.NotFoundException;
import com.example.weaver.models.Project;
import com.example.weaver.models.User;
import com.example.weaver.repositories.ProjectRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final EntityManager entityManager;

    public Project create(UUID createdBy, String name,String description, Instant finishedAt) {
        User userRef=entityManager.getReference(User.class, createdBy);
        Project project=Project.builder()
                .createdBy(userRef)
                .name(name)
                .description(description)
                .finishedAt(finishedAt)
                .build();
        return projectRepository.save(project);
    }
    public Project update(Project project, String name,String description,Instant finishedAt) {
        if (name != null) {
            if (name.length() < 3) {
                throw new BadRequestException("Project name must be at least 3 characters");
            }
            if (!name.equals(project.getName())) {
                project.setName(name);
            }
        }
        project.setDescription(description);
        project.setFinishedAt(finishedAt);

//        return projectRepository.save(project);
        return project;
    }
    public void delete(Project project) {
        projectRepository.delete(project);
    }

    public Project findById(UUID id) {
        return projectRepository.findById(id).orElseThrow(()-> new NotFoundException("Project not found"));
    }
}
