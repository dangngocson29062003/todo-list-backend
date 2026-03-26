package com.example.weaver.controllers;

import com.example.weaver.dtos.others.AuthUser;
import com.example.weaver.dtos.requests.CreateProjectRequest;
import com.example.weaver.dtos.requests.UpdateProjectRequest;
import com.example.weaver.dtos.responses.ProjectDetailResponse;
import com.example.weaver.dtos.responses.ProjectSummaryResponse;
import com.example.weaver.dtos.responses.ProjectSummaryResponses;
import com.example.weaver.services.AppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController {
    private final AppService appService;

    @GetMapping("/{id}")
    public ProjectDetailResponse getProject(@PathVariable UUID id,
                                            @AuthenticationPrincipal AuthUser authUser) {
        return appService.getProject(authUser.getId(), id);
    }

    @GetMapping("")
    public ProjectSummaryResponses getProjects(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "sortBy", required = false, defaultValue = "recent") String sortBy,
            @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "limit", required = false, defaultValue = "5") Integer limit
    ) {
        return appService.getProjects(
                authUser.getId(),
                name,
                sortBy,
                page,
                limit
        );
    }

    @PostMapping
    public ProjectSummaryResponse createProject(@Valid @RequestBody CreateProjectRequest request,
                                               @AuthenticationPrincipal AuthUser authUser) {
        return appService.createProject(request, authUser.getId());
    }

    @PutMapping("/{id}")
    public ProjectDetailResponse updateProject(@PathVariable UUID id,
                                               @Valid @RequestBody UpdateProjectRequest request,
                                               @AuthenticationPrincipal AuthUser authUser) {
        return appService.updateProject(id, request, authUser.getId());
    }

    @DeleteMapping("/{id}")
    public void deleteProject(@PathVariable UUID id,
                              @AuthenticationPrincipal AuthUser authUser) {
        appService.deleteProject(id, authUser.getId());
    }

}
