package com.example.weaver.controllers;

import com.example.weaver.dtos.others.AuthUser;
import com.example.weaver.dtos.requests.CreateProjectRequest;
import com.example.weaver.dtos.requests.UpdateProjectRequest;
import com.example.weaver.dtos.responses.ProjectDetailResponse;
import com.example.weaver.dtos.responses.ProjectSummaryResponse;
import com.example.weaver.services.AppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController {
    private final AppService appService;

    @GetMapping("")
    public List<ProjectSummaryResponse> getProjects(@AuthenticationPrincipal AuthUser authUser) {
        return appService.getProjects(authUser.getId());
    }

    @GetMapping("/{id}")
    public ProjectDetailResponse getProject(@PathVariable UUID id, @AuthenticationPrincipal AuthUser authUser) {
        return appService.getProject(authUser.getId(), id);
    }

    @PostMapping("")
    public ProjectDetailResponse createProject(@Valid @RequestBody CreateProjectRequest request, @AuthenticationPrincipal AuthUser authUser) {
        return appService.createProject(request, authUser.getId());
    }

    @PutMapping("/{id}")
    public ProjectDetailResponse updateProject(@RequestBody UpdateProjectRequest request, @PathVariable UUID id, @AuthenticationPrincipal AuthUser authUser) {
        return appService.updateProject(request, id, authUser.getId());
    }
    @DeleteMapping("/{id}")
    public void deleteProject(@PathVariable UUID id,
                              @AuthenticationPrincipal AuthUser authUser) {
        appService.deleteProject(id,authUser.getId());
    }

}
