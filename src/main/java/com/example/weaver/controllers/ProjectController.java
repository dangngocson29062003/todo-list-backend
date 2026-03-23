package com.example.weaver.controllers;

import com.example.weaver.dtos.others.AuthUser;
import com.example.weaver.dtos.requests.CreateProjectRequest;
import com.example.weaver.dtos.requests.UpdateProjectRequest;
import com.example.weaver.dtos.responses.ProjectDetailResponse;
import com.example.weaver.dtos.responses.ProjectSimpleResponse;
import com.example.weaver.dtos.responses.ProjectSimpleResponses;
import com.example.weaver.models.Project;
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
        return appService.getProjectDetail(id,authUser.getId());
    }
    @GetMapping("/simple")
    public ProjectSimpleResponses getAllProjects(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(name = "limit",required = false)  Integer limit,
            @RequestParam(name = "lastAccessCursor",required = false) Instant lastLastAccessCursor,
            @RequestParam(name = "createdAtCursor",required = false) Instant lastCreatedAtCursor) {
        return appService.getProjectsByUserId(authUser.getId(),lastLastAccessCursor,lastCreatedAtCursor,limit);
    }

    @PostMapping
    public ProjectDetailResponse createProject(@Valid @RequestBody CreateProjectRequest request,
                                               @AuthenticationPrincipal AuthUser authUser) {
        return appService.createProject(authUser.getId(),
                request.getName().trim(),
                request.getDescription()!=null? request.getDescription().trim():null,
                request.getEndDate()!=null? request.getEndDate():null);
    }

    @PutMapping("/{id}")
    public ProjectDetailResponse updateProject(@PathVariable UUID id,
                                               @Valid @RequestBody UpdateProjectRequest request,
                                               @AuthenticationPrincipal AuthUser authUser) {
        return appService.updateProject(id,request,authUser.getId());
    }
    @DeleteMapping("/{id}")
    public void deleteProject(@PathVariable UUID id,
                              @AuthenticationPrincipal AuthUser authUser) {
        appService.deleteProject(id,authUser.getId());
    }

}
