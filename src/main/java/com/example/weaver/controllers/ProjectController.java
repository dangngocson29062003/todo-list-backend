package com.example.weaver.controllers;

import com.example.weaver.dtos.others.AuthUser;
import com.example.weaver.dtos.requests.CreateProjectRequest;
import com.example.weaver.models.Project;
import com.example.weaver.models.ProjectMember;
import com.example.weaver.services.AppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/project")
public class ProjectController {
    private final AppService appService;

    @GetMapping("/{id}")
    public Project getProject(@PathVariable UUID id,
                              @AuthenticationPrincipal AuthUser authUser) {
        return appService.getProject(id,authUser.getId());
    }
    @GetMapping
    public List<Project> getAllProjects(@AuthenticationPrincipal AuthUser authUser) {
        return appService.getProjectsByUserId(authUser.getId());
    }
    @GetMapping("/{id}/member")
    public List<ProjectMember> getProjectMembers(@PathVariable UUID id,
                                                 @AuthenticationPrincipal AuthUser authUser) {
        return appService.getProjectMembers(id,authUser.getId());
    }

    @PostMapping
    public Project createProject(@Valid @RequestBody CreateProjectRequest request,
                                 @AuthenticationPrincipal AuthUser authUser) {
        if (authUser == null) {
            // Bạn có thể throw một UnauthorizedException tùy chỉnh ở đây
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bạn cần đăng nhập để thực hiện thao tác này");
        }
        return appService.createProject(authUser.getId(),
                request.getName(), request.getDescription(), request.getFinishedAt());
    }

    @PutMapping("/{id}")
    public Project updateProject(@PathVariable UUID id,
                                 @Valid @RequestBody CreateProjectRequest request,
                                 @AuthenticationPrincipal AuthUser authUser) {
        return appService.updateProject(authUser.getId(), id,
                request.getName(), request.getDescription(), request.getFinishedAt());
    }
    @DeleteMapping("/id")
    public void deleteProject(@PathVariable UUID id,
                              @AuthenticationPrincipal AuthUser authUser) {
        appService.deleteProject(authUser.getId(), id);
    }

}
