package com.example.weaver.controllers;

import com.example.weaver.dtos.others.AuthUser;
import com.example.weaver.dtos.requests.ProjectMemberRequest;
import com.example.weaver.dtos.requests.UpdateFavoriteRequest;
import com.example.weaver.dtos.requests.UpdateProjectMemberRoleRequest;
import com.example.weaver.dtos.responses.ProjectMemberResponse;
import com.example.weaver.models.ProjectMember;
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
@RequestMapping("/projects/{projectId}/members")
public class ProjectMemberController {
    private final AppService appService;

    @PostMapping("")
    public ProjectMemberResponse addProjectMember(@AuthenticationPrincipal AuthUser authUser,
                                                  @PathVariable UUID projectId,
                                                  @RequestBody ProjectMemberRequest request){
        return appService.addProjectMember(authUser.getId(),projectId,request);
    }
    @PutMapping("/{userId}")
    public ProjectMemberResponse updateProjectMemberRole(@AuthenticationPrincipal AuthUser authUser,
                                                 @PathVariable UUID projectId,
                                                 @PathVariable UUID userId,
                                                 @Valid @RequestBody UpdateProjectMemberRoleRequest request){
        return appService.updateProjectMemberRole(authUser.getId(),projectId,userId,request.getNewRole());
    }
    @DeleteMapping("/{userId}")
    public void removeProjectMember(@AuthenticationPrincipal AuthUser authUser,
                                    @PathVariable UUID projectId,
                                    @PathVariable UUID userId){
        appService.removeProjectMember(authUser.getId(),projectId,userId);
    }

    @PostMapping("/{userId}/pin")
    public void updateProjectPinStatus(@PathVariable UUID projectId,
                                   @PathVariable UUID userId){
        appService.updateProjectPinStatus(projectId,userId);
    }

    @PatchMapping("/{userId}/favorite")
    public void updateProjectFavorite(@PathVariable UUID projectId,
                                      @PathVariable UUID userId, @RequestBody UpdateFavoriteRequest request) {
        appService.updateProjectFavorite(projectId, userId, request.getFavorite());
    }

    @PostMapping("/{userId}/last-access")
    public void updateProjectLastAccess(@PathVariable UUID projectId,
                                   @PathVariable UUID userId){
        appService.updateProjectLastAccess(projectId,userId);
    }
}
