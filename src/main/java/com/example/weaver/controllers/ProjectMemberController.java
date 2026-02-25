package com.example.weaver.controllers;

import com.example.weaver.dtos.AuthUser;
import com.example.weaver.dtos.requests.ProjectMemberRequest;
import com.example.weaver.models.ProjectMember;
import com.example.weaver.services.AppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class ProjectMemberController {
    private final AppService appService;

    @PostMapping
    public ProjectMember addProjectMember(@AuthenticationPrincipal AuthUser authUser,
                                          @Valid @RequestBody ProjectMemberRequest request){
        return appService.addProjectMember(authUser.getId(),request.getProjectId(),request.getUserId());
    }
    @DeleteMapping
    public void removeProjectMember(@AuthenticationPrincipal AuthUser authUser,
                                    @Valid @RequestBody ProjectMemberRequest request){
        appService.removeProjectMember(authUser.getId(),request.getProjectId(),request.getUserId());
    }
}
