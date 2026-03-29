package com.example.weaver.services;

import com.example.weaver.dtos.requests.CreateProjectRequest;
import com.example.weaver.dtos.requests.UpdateProjectRequest;
import com.example.weaver.dtos.responses.DeletedProjectResponse;
import com.example.weaver.dtos.responses.ProjectSummaryResponse;
import com.example.weaver.dtos.responses.ProjectSummaryResponses;
import com.example.weaver.enums.Priority;
import com.example.weaver.enums.Stage;
import com.example.weaver.exceptions.BadRequestException;
import com.example.weaver.exceptions.NotFoundException;
import com.example.weaver.models.Project;
import com.example.weaver.models.User;
import com.example.weaver.repositories.ProjectRepository;
import com.example.weaver.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final EntityManager entityManager;
    private final UserRepository userRepository;

    public Project getProject(UUID projectId, UUID userId) {
        return projectRepository.findDetailById(projectId, userId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
    }

    public ProjectSummaryResponses getProjects(UUID userId,
                                               String name,
                                               String sortBy,
                                               int page,
                                               int limit,
                                               Boolean favorite) {
        int pageSize = Math.min(Math.max(limit, 1), 50);
        Sort sort = switch (sortBy.toLowerCase()) {
            case "alphabetical" -> Sort.by(Sort.Order.asc("p.name"));
            case "created" -> Sort.by(Sort.Order.desc("pm.createdAt"));
            default -> Sort.by(Sort.Order.desc("pm.lastAccess"));
        };
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Slice<ProjectSummaryResponse> projectsSlice = projectRepository.findAllSummaries(
                userId, name, favorite, pageable
        );
        return new ProjectSummaryResponses(
                projectsSlice.getContent(),
                projectsSlice.getNumber(),
                projectsSlice.hasNext()
        );
    }

    public Project createProject(CreateProjectRequest request, User creator) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setTags(request.getTags());
        project.setStage(request.getStage() != null ? request.getStage() : Stage.PLANNING);
        project.setPriority(request.getPriority() != null ? request.getPriority() : Priority.NORMAL);
        project.setGoals(request.getGoals());
        project.setTechStack(request.getTechStack());
        project.setGithubUrl(request.getGithubUrl());
        project.setFigmaUrl(request.getFigmaUrl());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setCreatedBy(creator);
        return projectRepository.save(project);
    }

    public Project updateProject(UUID projectId, UpdateProjectRequest request, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        if (request.getName() != null) {
            if (request.getName().trim().length() < 3) throw new BadRequestException("Name too short");
            project.setName(request.getName());
        }

        if (request.getDescription() != null) project.setDescription(request.getDescription().trim());
        if (request.getStage() != null) project.setStage(request.getStage());
        if (request.getPriority() != null) project.setPriority(request.getPriority());
        if (request.getGoals() != null) project.setGoals(request.getGoals());
        if (request.getTechStack() != null) project.setTechStack(request.getTechStack());

        boolean startChanged = request.getStartDate() != null;
        boolean endChanged = request.getEndDate() != null;

        if (startChanged || endChanged) {
            LocalDate newStart = startChanged ? request.getStartDate() : project.getStartDate();
            LocalDate newEnd = endChanged ? request.getEndDate() : project.getEndDate();

            if (newStart != null && newEnd != null && newEnd.isBefore(newStart)) {
                throw new BadRequestException("End date cannot be before start date");
            }

            project.setStartDate(newStart);
            project.setEndDate(newEnd);
        }
        return projectRepository.save(project);
    }

    public List<DeletedProjectResponse> getDeletedProjects(UUID userId) {
        return projectRepository.findAllDeletedSummaries(userId);
    }

    public void softDeleteProject(UUID projectId, User user) {
        Project project = findById(projectId);
        project.setIsDeleted(true);
        project.setDeletedAt(Instant.now());
        project.setDeletedBy(user);
        projectRepository.save(project);
    }

    public void restoreProject(UUID projectId) {
        Project project = findById(projectId);

        project.setIsDeleted(false);
        project.setDeletedAt(null);
        project.setDeletedBy(null);

        projectRepository.save(project);
    }

    public void hardDeleteProject(UUID projectId) {
        Project project = findById(projectId);

        projectRepository.delete(project);
    }

    public Project findById(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found"));
    }

}
