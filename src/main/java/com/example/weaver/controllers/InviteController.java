package com.example.weaver.controllers;

import com.example.weaver.dtos.others.AuthUser;
import com.example.weaver.services.AppService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class InviteController {
    private final AppService appService;

//    @GetMapping("/invite")
//    public List<InviteLinksResponse> getInviteLinks(@AuthenticationPrincipal AuthUser authUser) {
//
//    }

    @PostMapping("/invite")
    public String createInviteLink(@AuthenticationPrincipal AuthUser authUser,
                                   @RequestBody UUID projectId) {
        return appService.createProjectInviteLink(projectId, authUser.getId());
    }

    @PostMapping("/invite/validate")
    public void validateInviteLink(@AuthenticationPrincipal AuthUser authUser,
                                   @RequestBody String token){
        appService.verifyProjectInviteLink(authUser.getId(), token);
    }
}
