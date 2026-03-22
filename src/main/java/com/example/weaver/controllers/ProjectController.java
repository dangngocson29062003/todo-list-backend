package com.example.weaver.controllers;

import com.example.weaver.dtos.others.AuthUser;
import com.example.weaver.dtos.requests.ProjectRequest;
import com.example.weaver.dtos.responses.ProjectResponse;
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

    @GetMapping("/{id}")
    public ProjectResponse getProject(@PathVariable UUID id,
                                      @AuthenticationPrincipal AuthUser authUser) {
        return appService.getProject(id,authUser.getId());
    }
    @GetMapping
    public List<ProjectResponse> getAllProjects(@AuthenticationPrincipal AuthUser authUser) {
        return appService.getProjectsByUserId(authUser.getId());
    }

    @GetMapping("/{id}/all-members")
    public ProjectResponse getProjectWithMember(@PathVariable UUID id,
                                                @AuthenticationPrincipal AuthUser authUser) {
        return appService.getProjectWithMembers(id);
    }

//    @PostMapping
//    public ProjectResponse createProject(@Valid @RequestBody ProjectRequest request,
//                                 @AuthenticationPrincipal AuthUser authUser) {
//        return appService.createProject(authUser.getId(),
//                request.getName().trim(),
//                request.getDescription()!=null? request.getDescription().trim():null,
//                request.getFinishedAt()!=null? request.getFinishedAt():null);
//    }

//    @PutMapping("/{id}")
//    public ProjectResponse updateProject(@PathVariable UUID id,
//                                 @Valid @RequestBody ProjectRequest request,
//                                 @AuthenticationPrincipal AuthUser authUser) {
//        return appService.updateProject(id,authUser.getId(),
//                request.getName().trim(), request.getDescription().trim(), request.getFinishedAt());
//    }
    @DeleteMapping("/{id}")
    public void deleteProject(@PathVariable UUID id,
                              @AuthenticationPrincipal AuthUser authUser) {
        appService.deleteProject(id,authUser.getId());
    }

}
